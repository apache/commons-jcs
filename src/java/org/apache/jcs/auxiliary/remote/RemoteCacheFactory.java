package org.apache.jcs.auxiliary.remote;

import java.util.*;

//import org.apache.jcs.auxiliary.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheFactory;

import org.apache.jcs.engine.behavior.ICache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheFactory implements IAuxiliaryCacheFactory
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheFactory.class );

    private static String name;

    // store reference of facades to initiate failover
    /** Description of the Field */
    public final static HashMap facades = new HashMap();


    /** Constructor for the RemoteCacheFactory object */
    public RemoteCacheFactory() { }


    /**
     * Interface method. Allows classforname construction, making caches
     * pluggable. Should be able to make this work for clusters and local caches
     */
    public ICache createCache( IAuxiliaryCacheAttributes iaca )
    {

        RemoteCacheAttributes rca = ( RemoteCacheAttributes ) iaca;

        ArrayList noWaits = new ArrayList();

        // if LOCAL
        if ( rca.getRemoteType() == rca.LOCAL )
        {

            // a list toi be turned into an array of failover server information
            ArrayList failovers = new ArrayList();

            // not necessary if a failover list is defined
            // REGISTER PRIMARY LISTENER
            // if it is a primary
            boolean primayDefined = false;
            if ( rca.getRemoteHost() != null )
            {
                primayDefined = true;

                failovers.add( rca.getRemoteHost() + ":" + rca.getRemotePort() );

                RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca );
                ICache ic = rcm.getCache( rca );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    //p( "noWait is null" );
                }
            }

            // GET HANDLE BUT DONT REGISTER A LISTENER FOR FAILOVERS
            String failoverList = rca.getFailoverServers();
            if ( failoverList != null )
            {
                StringTokenizer fit = new StringTokenizer( failoverList, "," );
                int fCnt = 0;
                while ( fit.hasMoreElements() )
                {
                    fCnt++;

                    String server = ( String ) fit.nextElement();
                    failovers.add( server );

                    rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                    rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                    RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca );
                    // add a listener if there are none, need to tell rca what number it is at
                    if ( ( !primayDefined && fCnt == 1 ) || noWaits.size() <= 0 )
                    {
                        ICache ic = rcm.getCache( rca );
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
                // end while
            }
            // end if failoverList != null

            rca.setFailovers( ( String[] ) failovers.toArray( new String[0] ) );

            // if CLUSTER
        }
        else
            if ( rca.getRemoteType() == rca.CLUSTER )
        {

            // REGISTER LISTENERS FOR EACH SYSTEM CLUSTERED CACHEs
            StringTokenizer it = new StringTokenizer( rca.getClusterServers(), "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = ( String ) it.nextElement();
                //p( "tcp server = " +  server );
                rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca );
                rca.setRemoteType( rca.CLUSTER );
                ICache ic = rcm.getCache( rca );
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
        // end if CLUSTER

        //RemoteCacheNoWaitFacade rcnwf = new RemoteCacheNoWaitFacade( (RemoteCacheNoWait[])noWaits.toArray(new RemoteCacheNoWait[0]), iaca.getCacheName() );
        RemoteCacheNoWaitFacade rcnwf = new RemoteCacheNoWaitFacade( ( RemoteCacheNoWait[] ) noWaits.toArray( new RemoteCacheNoWait[0] ), rca );

        facades.put( rca.getCacheName(), rcnwf );

        return rcnwf;
    }
    // end createCache

    /**
     * Gets the name attribute of the RemoteCacheFactory object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the RemoteCacheFactory object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
