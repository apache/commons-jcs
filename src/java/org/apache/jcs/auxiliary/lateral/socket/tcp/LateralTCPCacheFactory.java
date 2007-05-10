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
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryManager;
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryService;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * can have multiple caches. The remote relationships are consolidated and
 * restored via these managers.
 * <p>
 * The facade provides a front to the composite cache so the implementation is
 * transparent.
 *
 */
public class LateralTCPCacheFactory
    extends LateralCacheAbstractFactory
{
    private final static Log log = LogFactory.getLog( LateralTCPCacheFactory.class );

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.jcs.auxiliary.AuxiliaryCacheAttributes,
     *      org.apache.jcs.engine.behavior.ICompositeCacheManager)
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr )
    {
        ITCPLateralCacheAttributes lac = (ITCPLateralCacheAttributes) iaca;
        ArrayList noWaits = new ArrayList();

        // pars up the tcp servers and set the tcpServer value and
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
                LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lacC, cacheMgr );
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
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), (ILateralCacheAttributes)iaca );

        // create udp discovery if available.
        createDiscoveryService( lac, lcnwf, cacheMgr );

        return lcnwf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.LateralCacheAbstractFactory#createListener(org.apache.jcs.auxiliary.lateral.LateralCacheAttributes,
     *      org.apache.jcs.engine.behavior.ICompositeCacheManager)
     */
    public void createListener( ILateralCacheAttributes lac, ICompositeCacheManager cacheMgr )
    {
        ITCPLateralCacheAttributes attr = (ITCPLateralCacheAttributes) lac;
        // don't create a listener if we are not receiving.
        if ( attr.isReceive() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Creating listener for " + lac );
            }

            try
            {
                // make a listener. if one doesn't exist
                LateralTCPListener.getInstance( attr, cacheMgr );
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
     * Creates the discovery service. Only creates this for tcp laterals right
     * now.
     *
     * @param lac
     *            ITCPLateralCacheAttributes
     * @param lcnwf
     * @param cacheMgr
     * @return null if none is created.
     */
    private UDPDiscoveryService createDiscoveryService( ITCPLateralCacheAttributes lac, LateralCacheNoWaitFacade lcnwf,
                                                       ICompositeCacheManager cacheMgr )
    {
        UDPDiscoveryService discovery = null;

        // create the UDP discovery for the TCP lateral
        if ( lac.isUdpDiscoveryEnabled() )
        {
            // need a factory for this so it doesn't
            // get dereferenced, also we don't want one for every region.
            discovery = UDPDiscoveryManager.getInstance().getService( lac, cacheMgr );

            discovery.addNoWaitFacade( lcnwf, lac.getCacheName() );

            if ( log.isInfoEnabled() )
            {
                log.info( "Created UDPDiscoveryService for TCP lateral cache." );
            }
        }
        return discovery;
    }
}
