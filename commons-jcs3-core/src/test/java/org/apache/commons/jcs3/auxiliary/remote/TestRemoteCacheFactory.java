package org.apache.commons.jcs3.auxiliary.remote;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;

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
/**
 * Test RemoteCache factory that skips actual connection attempt
 */
public class TestRemoteCacheFactory extends RemoteCacheFactory
{
    // Mock
    public class TestRemoteCacheManager extends RemoteCacheManager
    {
        protected TestRemoteCacheManager(final IRemoteCacheAttributes cattr, final ICompositeCacheManager cacheMgr, final RemoteCacheMonitor monitor, final ICacheEventLogger cacheEventLogger,
                final IElementSerializer elementSerializer)
        {
            super(cattr, cacheMgr, monitor, cacheEventLogger, elementSerializer);
        }

        @Override
        protected void lookupRemoteService() throws IOException
        {
            // Skip
        }

        @Override
        public void removeRemoteCacheListener(final IRemoteCacheAttributes cattr) throws IOException
        {
            // Skip
        }
    }

    /** Contains mappings of RemoteLocation instance to RemoteCacheManager instance. */
    protected ConcurrentMap<RemoteLocation, RemoteCacheManager> managers;

    /**
     * @see org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory#dispose()
     */
    @Override
    public void dispose()
    {
        managers.values().forEach(RemoteCacheManager::release);
        managers.clear();
    }

    /**
     * Returns an instance of RemoteCacheManager for the given connection parameters.
     * <p>
     * Host and Port uniquely identify a manager instance.
     * <p>
     * @param cattr
     *
     * @return The instance value or null if no such manager exists
     */
    @Override
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
    @Override
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

        return managers.computeIfAbsent(rca.getRemoteLocation(), key -> new TestRemoteCacheManager(rca, cacheMgr, null, cacheEventLogger, elementSerializer));
    }

    /**
     * @see org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory#initialize()
     */
    @Override
    public void initialize()
    {
        managers = new ConcurrentHashMap<>();
    }
}
