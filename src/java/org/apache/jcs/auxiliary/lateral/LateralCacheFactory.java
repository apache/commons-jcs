package org.apache.jcs.auxiliary.lateral;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCache;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * canl have multiple caches. The remote relationships are consolidated and
 * restored via these managers. The facade provides a front to the composite
 * cache so the implmenetation is transparent.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheFactory implements AuxiliaryCacheFactory
{
    private final static Log log =
        LogFactory.getLog( LateralCacheFactory.class );

    private String name;

    /**
     * Interface method. Allows classforname construction, making caches
     * pluggable.
     *
     * @return
     * @param iaca
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca,
                                       CompositeCache cache )
    {
        LateralCacheAttributes lac = ( LateralCacheAttributes ) iaca;
        ArrayList noWaits = new ArrayList();

        if ( lac.getTransmissionType() == lac.UDP )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }
        else if ( lac.getTransmissionType() == lac.JAVAGROUPS )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }
        else if ( lac.getTransmissionType() == lac.TCP )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            //Iterator it = lac.tcpServers.iterator();
            //while( it.hasNext() ) {

            StringTokenizer it = new StringTokenizer( lac.tcpServers, "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = ( String ) it.nextElement();
                if ( log.isDebugEnabled() )
                {
                  log.debug( "tcp server = " +  server );
                }
                lac.setTcpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    //p( "noWait is null" );
                }
            }

        }
        else if ( lac.getTransmissionType() == lac.XMLRPC )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            //Iterator it = lac.tcpServers.iterator();
            //while( it.hasNext() ) {

            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = ( String ) it.nextElement();
                //p( "tcp server = " +  server );
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
        else if ( lac.getTransmissionType() == lac.HTTP )
        {
            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                String server = ( String ) it.nextElement();
                lac.setHttpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
            }
        }

        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( ( LateralCacheNoWait[] ) noWaits.toArray( new LateralCacheNoWait[ 0 ] ), iaca.getCacheName() );

        return lcnwf;
    }
    // end createCache

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
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
