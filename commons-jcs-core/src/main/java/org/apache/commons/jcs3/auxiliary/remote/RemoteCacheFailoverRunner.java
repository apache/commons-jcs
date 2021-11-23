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

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheMonitor;

/**
 * The RemoteCacheFailoverRunner tries to establish a connection with a failover
 * server, if any are defined. Once a failover connection is made, it will
 * attempt to replace the failover with the primary remote server.
 * <p>
 * It works by switching out the RemoteCacheNoWait inside the Facade.
 * <p>
 * Client (i.e.) the CompositeCache has reference to a RemoteCacheNoWaitFacade.
 * This facade is created by the RemoteCacheFactory. The factory maintains a set
 * of managers, one for each remote server. Typically, there will only be one
 * manager.
 * <p>
 * If you use multiple remote servers, you may want to set one or more as
 * failovers. If a local cache cannot connect to the primary server, or looses
 * its connection to the primary server, it will attempt to restore that
 * Connection in the background. If failovers are defined, the Failover runner
 * will try to connect to a failover until the primary is restored.
 *
 * @deprecated Functionality moved to RemoteCacheNoWaitFacade
 */
@Deprecated
public class RemoteCacheFailoverRunner<K, V> extends AbstractAuxiliaryCacheMonitor
{
    /** The facade returned to the composite cache. */
    private final RemoteCacheNoWaitFacade<K, V> facade;

    /**
     * Constructor for the RemoteCacheFailoverRunner object. This allows the
     * FailoverRunner to modify the facade that the CompositeCache references.
     *
     * @param facade the facade the CompositeCache talks to.
     * @param cacheFactory the cache factory instance
     */
    public RemoteCacheFailoverRunner( final RemoteCacheNoWaitFacade<K, V> facade, final RemoteCacheFactory cacheFactory )
    {
        super("JCS-RemoteCacheFailoverRunner");
        this.facade = facade;
        setIdlePeriod(20000L);
    }

    /**
     * Clean up all resources before shutdown
     */
    @Override
    protected void dispose()
    {
        // empty
    }

    /**
     * do actual work
     */
    @Override
    protected void doWork()
    {
        // empty
    }


    /**
     * Main processing method for the RemoteCacheFailoverRunner object.
     * <p>
     * If we do not have a connection with any failover server, this will try to
     * connect one at a time. If no connection can be made, it goes to sleep for
     * a while (20 seconds).
     * <p>
     * Once a connection with a failover is made, we will try to reconnect to
     * the primary server.
     * <p>
     * The primary server is the first server defines in the FailoverServers
     * list.
     */
    @Override
    public void run()
    {
        // start the main work of connecting to a failover and then restoring
        // the primary.
        facade.connectAndRestore();
    }

}
