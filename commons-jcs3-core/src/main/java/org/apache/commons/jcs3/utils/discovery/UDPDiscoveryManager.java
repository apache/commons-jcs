package org.apache.commons.jcs3.utils.discovery;

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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.behavior.IProvideScheduler;
import org.apache.commons.jcs3.log.Log;

/**
 * This manages UDPDiscovery Services. We should end up with one service per Lateral Cache Manager
 * Instance. One service works for multiple regions. We don't want a connection for each region.
 */
public class UDPDiscoveryManager
{
    /** The logger */
    private static final Log log = Log.getLog( UDPDiscoveryManager.class );

    /** Singleton instance */
    private static final UDPDiscoveryManager INSTANCE = new UDPDiscoveryManager();

    /**
     * Singleton
     *
     * @return UDPDiscoveryManager
     */
    public static UDPDiscoveryManager getInstance()
    {
        return INSTANCE;
    }

    /** Known services */
    private final ConcurrentMap<String, UDPDiscoveryService> services = new ConcurrentHashMap<>();

    /** Private for singleton */
    private UDPDiscoveryManager()
    {
        // noopt
    }

    /**
     * Creates a service for the address and port if one doesn't exist already.
     * <p>
     * We need to key this using the listener port too.
     * TODO think of making one discovery service work for multiple types of clients.
     *
     * @param discoveryAddress
     * @param discoveryPort
     * @param serviceAddress
     * @param servicePort
     * @param updTTL
     * @param cacheMgr
     * @param serializer
     * @return UDPDiscoveryService
     * @since 3.1
     */
    public UDPDiscoveryService getService( final String discoveryAddress, final int discoveryPort,
            final String serviceAddress, final int servicePort, final int updTTL,
            final ICompositeCacheManager cacheMgr, final IElementSerializer serializer )
    {
        final String key = String.join(":", discoveryAddress, String.valueOf(discoveryPort), String.valueOf(servicePort));

        final UDPDiscoveryService service = services.computeIfAbsent(key, k -> {
            log.info( "Creating service for address:port:servicePort [{0}]", key );

            final UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
            attributes.setUdpDiscoveryAddr(discoveryAddress);
            attributes.setUdpDiscoveryPort(discoveryPort);
            attributes.setServiceAddress(serviceAddress);
            attributes.setServicePort(servicePort);
            attributes.setUdpTTL(updTTL);

            final UDPDiscoveryService newService = new UDPDiscoveryService(attributes, serializer);

            // register for shutdown notification
            cacheMgr.registerShutdownObserver( newService );

            // inject scheduler
            if ( cacheMgr instanceof IProvideScheduler)
            {
                newService.setScheduledExecutorService(((IProvideScheduler)cacheMgr)
                        .getScheduledExecutorService());
            }

            newService.startup();
            return newService;
        });

        log.debug( "Returning service [{0}] for key [{1}]", service, key );

        return service;
    }
}
