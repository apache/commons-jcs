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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.serialization.SerializationConversionUtil;

/** Shared listener base. */
public abstract class AbstractRemoteCacheListener<K, V>
    implements IRemoteCacheListener<K, V>
{
    /** The logger */
    private static final Log log = Log.getLog( AbstractRemoteCacheListener.class );

    /** The cached name of the local host. The remote server gets this for logging purposes. */
    private static String localHostName;

    /**
     * The cache manager used to put items in different regions. This is set lazily and should not
     * be sent to the remote server.
     */
    private final ICompositeCacheManager cacheMgr;

    /** The remote cache configuration object. */
    private final IRemoteCacheAttributes irca;

    /** This is set by the remote cache server. */
    private long listenerId;

    /** Custom serializer. */
    private final IElementSerializer elementSerializer;

    /**
     * Only need one since it does work for all regions, just reference by multiple region names.
     * <p>
     * The constructor exports this object, making it available to receive incoming calls. The
     * callback port is anonymous unless a local port value was specified in the configuration.
     *
     * @param irca cache configuration
     * @param cacheMgr the cache hub
     * @param elementSerializer a custom serializer
     */
    public AbstractRemoteCacheListener( final IRemoteCacheAttributes irca, final ICompositeCacheManager cacheMgr, final IElementSerializer elementSerializer )
    {
        this.irca = irca;
        this.cacheMgr = cacheMgr;
        this.elementSerializer = elementSerializer;
    }

    /**
     * Gets the cacheManager attribute of the RemoteCacheListener object. This is one of the few
     * places that force the cache to be a singleton.
     */
    protected ICompositeCacheManager getCacheManager()
    {
        return cacheMgr;
    }

    /**
     * Gets the listenerId attribute of the RemoteCacheListener object. This is stored in the
     * object. The RemoteCache object contains a reference to the listener and get the id this way.
     *
     * @return The listenerId value
     * @throws IOException
     */
    @Override
    public long getListenerId()
        throws IOException
    {
        log.debug( "get listenerId = [{0}]", listenerId );
        return listenerId;

    }

    /**
     * This is for debugging. It allows the remote server to log the address of clients.
     *
     * @return String
     * @throws IOException
     */
    @Override
    public synchronized String getLocalHostAddress()
        throws IOException
    {
        if ( localHostName == null )
        {
            try
            {
                localHostName = HostNameUtil.getLocalHostAddress();
            }
            catch ( final UnknownHostException uhe )
            {
                localHostName = "unknown";
            }
        }
        return localHostName;
    }

    /**
     * Gets the remoteType attribute of the RemoteCacheListener object
     *
     * @return The remoteType value
     * @throws IOException
     */
    @Override
    public RemoteType getRemoteType()
        throws IOException
    {
        log.debug( "getRemoteType = [{0}]", irca::getRemoteType);
        return irca.getRemoteType();
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleDispose( final String cacheName )
        throws IOException
    {
        log.debug( "handleDispose> cacheName={0}", cacheName );
        // TODO consider what to do here, we really don't want to
        // dispose, we just want to disconnect.
        // just allow the cache to go into error recovery mode.
        // getCacheManager().freeCache( cacheName, true );
    }

    /**
     * If this is configured to remove on put, then remove the element since it has been updated
     * elsewhere. cd should be incomplete for faster transmission. We don't want to pass data only
     * invalidation. The next time it is used the local cache will get the new version from the
     * remote store.
     * <p>
     * If remove on put is not configured, then update the item.
     * @param cb
     * @throws IOException
     */
    @Override
    public void handlePut( ICacheElement<K, V> cb )
        throws IOException
    {
        if ( irca.getRemoveUponRemotePut() )
        {
            log.debug( "PUTTING ELEMENT FROM REMOTE, (  invalidating ) " );
            handleRemove( cb.getCacheName(), cb.getKey() );
        }
        else
        {
            log.debug( "PUTTING ELEMENT FROM REMOTE, ( updating ) " );
            log.debug( "cb = {0}", cb );

            // Eventually the instance of will not be necessary.
            if ( cb instanceof ICacheElementSerialized )
            {
                log.debug( "Object needs to be deserialized." );
                try
                {
                    cb = SerializationConversionUtil.getDeSerializedCacheElement(
                            (ICacheElementSerialized<K, V>) cb, this.elementSerializer );
                    log.debug( "Deserialized result = {0}", cb );
                }
                catch ( final IOException e )
                {
                    throw e;
                }
                catch ( final ClassNotFoundException e )
                {
                    log.error( "Received a serialized version of a class that we don't know about.", e );
                }
            }

            getCacheManager().<K, V>getCache( cb.getCacheName() ).localUpdate( cb );
        }
    }

    /**
     * Calls localRemove on the CompositeCache.
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void handleRemove( final String cacheName, final K key )
        throws IOException
    {
        log.debug( "handleRemove> cacheName={0}, key={1}", cacheName, key );

        getCacheManager().<K, V>getCache( cacheName ).localRemove( key );
    }

    /**
     * Calls localRemoveAll on the CompositeCache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleRemoveAll( final String cacheName )
        throws IOException
    {
        log.debug( "handleRemoveAll> cacheName={0}", cacheName );

        getCacheManager().<K, V>getCache( cacheName ).localRemoveAll();
    }

    /**
     * Let the remote cache set a listener_id. Since there is only one listener for all the regions
     * and every region gets registered? the id shouldn't be set if it isn't zero. If it is we
     * assume that it is a reconnect.
     *
     * @param id The new listenerId value
     * @throws IOException
     */
    @Override
    public void setListenerId( final long id )
        throws IOException
    {
        listenerId = id;
        log.info( "set listenerId = [{0}]", id );
    }

    /**
     * For easier debugging.
     *
     * @return Basic info on this listener.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n AbstractRemoteCacheListener: " )
           .append( "\n RemoteHost = ").append(irca.getRemoteLocation())
           .append( "\n ListenerId = ").append(listenerId);
        return buf.toString();
    }
}
