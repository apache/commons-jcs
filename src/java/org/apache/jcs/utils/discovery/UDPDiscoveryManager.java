package org.apache.jcs.utils.discovery;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IShutdownObservable;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * This manages UDPDiscovery Services. We should end up with one service per Lateral Cache Manager
 * Instance. One service works for multiple regions. We don't want a connection for each region.
 * <p>
 * @author Aaron Smuts
 */
public class UDPDiscoveryManager
{
    /** The logger */
    private final static Log log = LogFactory.getLog( UDPDiscoveryManager.class );

    /** Singleton instance */
    private static UDPDiscoveryManager INSTANCE = new UDPDiscoveryManager();

    /** Known services */
    private Map services = new HashMap();

    /** private for singleton */
    private UDPDiscoveryManager()
    {
        // noopt
    }

    /**
     * Singelton
     * <p>
     * @return UDPDiscoveryManager
     */
    public static UDPDiscoveryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a service for the address and port if one doesn't exist already.
     * <p>
     * We need to key this using the listener port too. TODO think of making one discovery service
     * work for mutliple types of clients.
     * <p>
     * @param discoveryAddress
     * @param discoveryPort
     * @param servicePort
     * @param cacheMgr
     * @param cacheEventLogger
     * @return UDPDiscoveryService
     */
    public synchronized UDPDiscoveryService getService( String discoveryAddress, int discoveryPort, int servicePort,
                                                        ICompositeCacheManager cacheMgr,
                                                        ICacheEventLogger cacheEventLogger )
    {
        String key = discoveryAddress + ":" + discoveryPort + ":" + servicePort;

        UDPDiscoveryService service = (UDPDiscoveryService) services.get( key );
        if ( service == null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Creating service for address:port:servicePort [" + key + "]" );
            }

            UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
            attributes.setUdpDiscoveryAddr( discoveryAddress );
            attributes.setUdpDiscoveryPort( discoveryPort );
            attributes.setServicePort( servicePort );

            service = new UDPDiscoveryService( attributes, cacheEventLogger );
            
            // register for shutdown notification
            ( (IShutdownObservable) cacheMgr ).registerShutdownObserver( service );

            services.put( key, service );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Returning service [" + service + "] for key [" + key + "]" );
        }

        return service;
    }
}
