package org.apache.jcs.auxiliary.remote.server;


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


import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;

import org.apache.jcs.auxiliary.remote.RemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWait;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWaitFacade;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 */
public class RemoteCacheClusterFactory implements AuxiliaryCacheFactory
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheClusterFactory.class );

    private String name;

    /**
     * Interface method. Allows classforname construction, making caches
     * pluggable.
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca,
                                       CompositeCache cache )
    {
        ArrayList noWaits = new ArrayList();

        RemoteCacheAttributes rca = ( RemoteCacheAttributes ) iaca;

        // create "SYSTEM_CLUSTER" caches for potential use
        if ( rca.getCacheName() == null )
        {
            rca.setCacheName( "SYSTEM_CLUSTER" );
        }
        StringTokenizer it = new StringTokenizer( rca.getClusterServers(), "," );
        while ( it.hasMoreElements() )
        {
            //String server = (String)it.next();
            String server = ( String ) it.nextElement();
            //p( "tcp server = " +  server );
            rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
            rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
            RemoteCacheClusterManager rcm = RemoteCacheClusterManager.getInstance( rca );
            ICache ic = rcm.getCache( rca.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
            else
            {
                //p( "noWait is null" );
            }
        }

        RemoteCacheNoWaitFacade rcnwf = new RemoteCacheNoWaitFacade( ( RemoteCacheNoWait[] ) noWaits.toArray( new RemoteCacheNoWait[0] ), rca );

        return rcnwf;
    }
    // end createCache

    /**
     * Gets the name attribute of the RemoteCacheClusterFactory object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the RemoteCacheClusterFactory object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
