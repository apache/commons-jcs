package org.apache.jcs.auxiliary.lateral;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.javagroups.LateralCacheJGListener;
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPListener;
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryManager;
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryService;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCache;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * canl have multiple caches. The remote relationships are consolidated and
 * restored via these managers. The facade provides a front to the composite
 * cache so the implmenetation is transparent.
 *  
 */
public class LateralCacheFactory
    implements AuxiliaryCacheFactory
{
    private final static Log log = LogFactory.getLog( LateralCacheFactory.class );

    private String name;

    /**
     * Interface method. Allows classforname construction, making caches
     * pluggable.
     * 
     * @return AuxiliaryCache
     * @param iaca
     * @param cache
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, CompositeCache cache )
    {
        LateralCacheAttributes lac = (LateralCacheAttributes) iaca;
        ArrayList noWaits = new ArrayList();

        if ( lac.getTransmissionType() == LateralCacheAttributes.UDP )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }
        else if ( lac.getTransmissionType() == LateralCacheAttributes.JAVAGROUPS )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }

        // for each server listed get the manager for that server.
        // from that manager get the cache for this region name.
        else if ( lac.getTransmissionType() == LateralCacheAttributes.TCP )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            StringTokenizer it = new StringTokenizer( lac.tcpServers, "," );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Configured for " + it.countTokens() + "  servers." );
            }
            while ( it.hasMoreElements() )
            {
                String server = (String) it.nextElement();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "tcp server = " + server );
                }
                LateralCacheAttributes lacC = (LateralCacheAttributes) lac.copy();
                lacC.setTcpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lacC );
                ICache ic = lcm.getCache( lacC.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.debug( "noWait is null, no lateral connection made" );
                }
            }
        }
        else if ( lac.getTransmissionType() == LateralCacheAttributes.XMLRPC )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            //Iterator it = lac.tcpServers.iterator();
            //while( it.hasNext() ) {

            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = (String) it.nextElement();
                //p( "tcp server = " + server );
                lac.setHttpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.warn( "noWait is null" );
                }
            }

        }
        else if ( lac.getTransmissionType() == LateralCacheAttributes.HTTP )
        {
            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                String server = (String) it.nextElement();
                lac.setHttpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
            }
        }

        createListener( lac );

        // create the no wait facade.
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), iaca.getCacheName() );

        createDiscoveryService( lac, lcnwf );

        return lcnwf;
    }

    /**
     * Makes sure a listener gets created. It will get monitored as soon as it
     * is used.
     * 
     * @param lac
     */
    private void createListener( LateralCacheAttributes lac )
    {
        // don't create a listener if we are not receiving.
        if ( lac.isReceive() )
        {
            try
            {
                if ( lac.getTransmissionType() == ILateralCacheAttributes.TCP )
                {
                    // make a listener. if one doesn't exist
                    LateralTCPListener.getInstance( lac );
                }
                else if ( lac.getTransmissionType() == ILateralCacheAttributes.JAVAGROUPS )
                {
                    LateralCacheJGListener.getInstance( lac );
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
     * Creates the discovery service. Only creates this for tcp laterals right
     * now.
     * 
     * @param lac
     * @param lcnwf
     * @return null if none is created.
     */
    private UDPDiscoveryService createDiscoveryService( LateralCacheAttributes lac, LateralCacheNoWaitFacade lcnwf )
    {
        UDPDiscoveryService discovery = null;

        //      create the UDP discovery for the TCP lateral
        if ( lac.isUdpDiscoveryEnabled() )
        {
            if ( lac.getTransmissionType() != LateralCacheAttributes.TCP )
            {
                log
                    .warn( "UdpDiscoveryEnabled is set to true, but the Lateral cache type is not TCP.  Discovery will not be enabled." );
            }

            // need a factory for this so it doesn't
            // get dereferenced, also we don't want one for every region.
            discovery = UDPDiscoveryManager.getInstance().getService( lac );

            discovery.addNoWaitFacade( lcnwf, lac.getCacheName() );

            if ( log.isInfoEnabled() )
            {
                log.info( "Created UDPDiscoveryService for TCP lateral cache." );
            }
        }
        return discovery;
    }

    /**
     * Gets the name attribute of the LateralCacheFactory object
     * 
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the LateralCacheFactory object
     * 
     * @param name
     *            The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
