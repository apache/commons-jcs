package org.apache.jcs.auxiliary.remote.server;

import java.util.*;

//import org.apache.jcs.auxiliary.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;

import org.apache.jcs.auxiliary.remote.RemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.RemoteCacheFactory;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWait;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWaitFacade;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
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
