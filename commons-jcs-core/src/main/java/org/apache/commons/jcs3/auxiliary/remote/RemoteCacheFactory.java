package org.apache.commons.jcs3.auxiliary.remote;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;

/**
 * The RemoteCacheFactory creates remote caches for the cache hub. It returns a no wait facade which
 * is a wrapper around a no wait. The no wait object is either an active connection to a remote
 * cache or a balking zombie if the remote cache is not accessible. It should be transparent to the
 * clients.
 */
public class RemoteCacheFactory
    extends AbstractAuxiliaryCacheFactory
{
    /** Monitor thread */
    private RemoteCacheMonitor monitor;

    /** Contains mappings of RemoteLocation instance to RemoteCacheManager instance. */
    private ConcurrentMap<RemoteLocation, RemoteCacheManager> managers;

    /**
     * For LOCAL clients we get a handle to all the failovers, but we do not register a listener
     * with them. We create the RemoteCacheManager, but we do not get a cache.
     * <p>
     * The failover runner will get a cache from the manager. When the primary is restored it will
     * tell the manager for the failover to deregister the listener.
     * <p>
     * @param iaca
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return AuxiliaryCache
     */
    @Override
    public <K, V> AuxiliaryCache<K, V> createCache(
            final AuxiliaryCacheAttributes iaca, final ICompositeCacheManager cacheMgr,
           final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer )
    {
        final RemoteCacheAttributes rca = (RemoteCacheAttributes) iaca;

        final ArrayList<RemoteCacheNoWait<K,V>> noWaits = new ArrayList<>();

        switch (rca.getRemoteType())
        {
            case LOCAL:
                // a list to be turned into an array of failover server information
                final ArrayList<RemoteLocation> failovers = new ArrayList<>();

                // not necessary if a failover list is defined
                // REGISTER PRIMARY LISTENER
                // if it is a primary
                if ( rca.getRemoteLocation() != null )
                {
                    failovers.add( rca.getRemoteLocation() );
                    final RemoteCacheManager rcm = getManager( rca, cacheMgr, cacheEventLogger, elementSerializer );
                    noWaits.add(rcm.getCache(rca));
                }

                // GET HANDLE BUT DONT REGISTER A LISTENER FOR FAILOVERS
                final String failoverList = rca.getFailoverServers();
                if ( failoverList != null )
                {
                    final String[] failoverServers = failoverList.split("\\s*,\\s*");
                    for (String server : failoverServers)
                    {
                        final RemoteLocation location = RemoteLocation.parseServerAndPort(server);

                        if (location != null)
                        {
                            failovers.add( location );
                            final RemoteCacheAttributes frca = (RemoteCacheAttributes) rca.clone();
                            frca.setRemoteLocation(location);
                            final RemoteCacheManager rcm = getManager( frca, cacheMgr, cacheEventLogger, elementSerializer );

                            // add a listener if there are none, need to tell rca what
                            // number it is at
                            if (noWaits.isEmpty())
                            {
                                frca.setFailoverIndex(0);
                                noWaits.add(rcm.getCache(frca));
                            }
                        }
                    }
                    // end for
                }
                // end if failoverList != null

                rca.setFailovers( failovers );
                break;

            case CLUSTER:
                // REGISTER LISTENERS FOR EACH SYSTEM CLUSTERED CACHEs
                final String[] clusterServers = rca.getClusterServers().split("\\s*,\\s*");
                for (String server: clusterServers)
                {
                    final RemoteLocation location = RemoteLocation.parseServerAndPort(server);

                    if (location != null)
                    {
                        final RemoteCacheAttributes crca = (RemoteCacheAttributes) rca.clone();
                        crca.setRemoteLocation(location);
                        final RemoteCacheManager rcm = getManager( crca, cacheMgr, cacheEventLogger, elementSerializer );
                        crca.setRemoteType( RemoteType.CLUSTER );
                        noWaits.add(rcm.getCache(crca));
                    }
                }
                break;
        }

        return new RemoteCacheNoWaitFacade<>(noWaits, rca, cacheEventLogger, elementSerializer, this);
    }
    // end createCache

    /**
     * Returns an instance of RemoteCacheManager for the given connection parameters.
     * <p>
     * Host and Port uniquely identify a manager instance.
     * <p>
     * @param cattr
     *
     * @return The instance value or null if no such manager exists
     */
    public RemoteCacheManager getManager( final IRemoteCacheAttributes cattr )
    {
        final RemoteCacheAttributes rca = (RemoteCacheAttributes) cattr.clone();
        if (rca.getRemoteLocation() == null)
        {
            rca.setRemoteLocation("", Registry.REGISTRY_PORT);
        }

        return managers.get(rca.getRemoteLocation());
    }

    /**
     * Returns an instance of RemoteCacheManager for the given connection parameters.
     * <p>
     * Host and Port uniquely identify a manager instance.
     * <p>
     * If the connection cannot be established, zombie objects will be used for future recovery
     * purposes.
     * <p>
     * @param cattr the cache configuration object
     * @param cacheMgr the cache manager
     * @param cacheEventLogger the event logger
     * @param elementSerializer the serializer to use for sending and receiving
     *
     * @return The instance value, never null
     */
    public RemoteCacheManager getManager( final IRemoteCacheAttributes cattr,
                                          final ICompositeCacheManager cacheMgr,
                                          final ICacheEventLogger cacheEventLogger,
                                          final IElementSerializer elementSerializer )
    {
        final RemoteCacheAttributes rca = (RemoteCacheAttributes) cattr.clone();
        if (rca.getRemoteLocation() == null)
        {
            rca.setRemoteLocation("", Registry.REGISTRY_PORT);
        }

        return managers.computeIfAbsent(rca.getRemoteLocation(), key -> {

            RemoteCacheManager manager = new RemoteCacheManager(rca, cacheMgr, monitor, cacheEventLogger, elementSerializer);
            monitor.addManager(manager);

            return manager;
        });
    }

	/**
	 * @see org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory#initialize()
	 */
	@Override
	public void initialize()
	{
		super.initialize();

		managers = new ConcurrentHashMap<>();

        monitor = new RemoteCacheMonitor();
        monitor.setDaemon(true);
	}

	/**
	 * @see org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory#dispose()
	 */
	@Override
	public void dispose()
	{
		managers.values().forEach(RemoteCacheManager::release);
		managers.clear();

        if (monitor != null)
        {
            monitor.notifyShutdown();
            try
            {
                monitor.join(5000);
            }
            catch (final InterruptedException e)
            {
                // swallow
            }
            monitor = null;
        }

		super.dispose();
	}
}
