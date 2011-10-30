package org.apache.jcs.auxiliary.lateral.socket.tcp;

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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheAbstractFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.behavior.IShutdownObservable;
import org.apache.jcs.engine.behavior.IShutdownObserver;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.discovery.UDPDiscoveryManager;
import org.apache.jcs.utils.discovery.UDPDiscoveryService;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each lateral service / local
 * relationship is managed by one manager. This manager can have multiple caches. The remote
 * relationships are consolidated and restored via these managers.
 * <p>
 * The facade provides a front to the composite cache so the implementation is transparent.
 */
public class LateralTCPCacheFactory
    extends LateralCacheAbstractFactory
{
    /** The logger */
    private final static Log log = LogFactory.getLog( LateralTCPCacheFactory.class );

    /** Non singleton manager. Used by this instance of the factory. */
    private LateralTCPDiscoveryListenerManager lateralTCPDiscoveryListenerManager;

    /**
     * Creates a TCP lateral.
     * <p>
     * @param iaca
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return AuxiliaryCache
     */
    @Override
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr,
                                       ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        ITCPLateralCacheAttributes lac = (ITCPLateralCacheAttributes) iaca;
        ArrayList<ICache> noWaits = new ArrayList<ICache>();

        // pairs up the tcp servers and set the tcpServer value and
        // get the manager and then get the cache
        // no servers are required.
        if ( lac.getTcpServers() != null )
        {
            StringTokenizer it = new StringTokenizer( lac.getTcpServers(), "," );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Configured for [" + it.countTokens() + "]  servers." );
            }
            while ( it.hasMoreElements() )
            {
                String server = (String) it.nextElement();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "tcp server = " + server );
                }
                ITCPLateralCacheAttributes lacC = (ITCPLateralCacheAttributes) lac.copy();
                lacC.setTcpServer( server );
                LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lacC, cacheMgr, cacheEventLogger,
                                                                                 elementSerializer );

                // register for shutdown notification
                if (cacheMgr instanceof IShutdownObservable )
                {
                    ( (IShutdownObservable) cacheMgr ).registerShutdownObserver( lcm );
                }

                ICache ic = lcm.getCache( lacC.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "noWait is null, no lateral connection made" );
                    }
                }
            }
        }

        createListener( (ILateralCacheAttributes) iaca, cacheMgr );

        // create the no wait facade.
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( noWaits
            .toArray( new LateralCacheNoWait[0] ), (ILateralCacheAttributes) iaca );

        // create udp discovery if available.
        createDiscoveryService( lac, lcnwf, cacheMgr, cacheEventLogger, elementSerializer );

        return lcnwf;
    }

    /**
     * @param lac
     * @param cacheMgr
     */
    @Override
    public void createListener( ILateralCacheAttributes lac, ICompositeCacheManager cacheMgr )
    {
        ITCPLateralCacheAttributes attr = (ITCPLateralCacheAttributes) lac;
        // don't create a listener if we are not receiving.
        if ( attr.isReceive() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Getting listener for " + lac );
            }

            try
            {
                // make a listener. if one doesn't exist
                ICacheListener listener = LateralTCPListener.getInstance( attr, cacheMgr );

                // register for shutdown notification
                if ( listener instanceof IShutdownObserver && cacheMgr instanceof IShutdownObservable )
                {
                    ( (IShutdownObservable) cacheMgr ).registerShutdownObserver( (IShutdownObserver) listener );
                }
            }
            catch ( Exception e )
            {
                log.error( "Problem creating lateral listener", e );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Not creating a listener since we are not receiving." );
            }
        }
    }

    /**
     * Creates the discovery service. Only creates this for tcp laterals right now.
     * <p>
     * @param lac ITCPLateralCacheAttributes
     * @param lcnwf
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return null if none is created.
     */
    private synchronized UDPDiscoveryService createDiscoveryService( ITCPLateralCacheAttributes lac,
                                                                     LateralCacheNoWaitFacade lcnwf,
                                                                     ICompositeCacheManager cacheMgr,
                                                                     ICacheEventLogger cacheEventLogger,
                                                                     IElementSerializer elementSerializer )
    {
        UDPDiscoveryService discovery = null;

        // create the UDP discovery for the TCP lateral
        if ( lac.isUdpDiscoveryEnabled() )
        {
            if ( lateralTCPDiscoveryListenerManager == null )
            {
                lateralTCPDiscoveryListenerManager = new LateralTCPDiscoveryListenerManager();
            }

            // One can be used for all regions
            LateralTCPDiscoveryListener discoveryListener = lateralTCPDiscoveryListenerManager
                .getDiscoveryListener( lac, cacheMgr, cacheEventLogger, elementSerializer );

            discoveryListener.addNoWaitFacade( lac.getCacheName(), lcnwf );

            // need a factory for this so it doesn't
            // get dereferenced, also we don't want one for every region.
            discovery = UDPDiscoveryManager.getInstance().getService( lac.getUdpDiscoveryAddr(),
                                                                      lac.getUdpDiscoveryPort(),
                                                                      lac.getTcpListenerPort(), cacheMgr,
                                                                      cacheEventLogger );

            discovery.addParticipatingCacheName( lac.getCacheName() );
            discovery.addDiscoveryListener( discoveryListener );

            if ( log.isInfoEnabled() )
            {
                log.info( "Registered TCP lateral cache [" + lac.getCacheName() + "] with UDPDiscoveryService." );
            }
        }
        return discovery;
    }
}
