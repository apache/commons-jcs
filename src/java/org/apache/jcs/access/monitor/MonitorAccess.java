package org.apache.jcs.access.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.control.group.GroupCacheHub;
import org.apache.jcs.engine.CacheConstants;

/**
 * Exposes the simple monitoring methods to the public in a simple manner.
 *
 * @author asmuts
 * @created February 12, 2002
 */
public class MonitorAccess implements Serializable
{
    private final static Log log =
        LogFactory.getLog( MonitorAccess.class );

    /** Description of the Field */
    protected CacheHub cacheMgr;

    /** Constructor for the MonitorAccess object */
    public MonitorAccess()
    {

        if ( cacheMgr == null )
        {
            synchronized ( GroupCacheAccess.class )
            {
                if ( cacheMgr == null )
                {
                    cacheMgr = GroupCacheHub.getInstance();
                }
            }
        }
    }

    /** Description of the Method */
    public String delete( String cacheName, String key )
    {

        // some junk to return for a synchronous call
        String result = "";

        try
        {

            ICache cache = cacheMgr.getCache( cacheName );

            if ( key != null )
            {
                if ( key.toUpperCase().equals( "ALL" ) )
                {
                    cache.removeAll();

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Removed all elements from " + cacheName );
                    }
                    result = "key = " + key;
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "key = " + key );
                    }
                    result = "key = " + key;
                    StringTokenizer toke = new StringTokenizer( key, "_" );

                    while ( toke.hasMoreElements() )
                    {
                        String temp = ( String ) toke.nextElement();
                        cache.remove( key );

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Removed " + temp + " from " + cacheName );
                        }
                    }
                }
            }
            else
            {
                result = "key is null";
            }

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        return result;
    }

    /** Description of the Method */
    public ArrayList overview()
    {

        ArrayList data = new ArrayList();

        String[] list = cacheMgr.getCacheNames();
        Arrays.sort( list );
        for ( int i = 0; i < list.length; i++ )
        {
            Hashtable ht = new Hashtable();
            String name = list[ i ];
            ht.put( "name", name );

            ICache cache = cacheMgr.getCache( name );
            int size = cache.getSize();
            ht.put( "size", Integer.toString( size ) );

            int status = cache.getStatus();
            String stat = status == CacheConstants.STATUS_ALIVE ? "ALIVE"
                : status == CacheConstants.STATUS_DISPOSED ? "DISPOSED"
                : status == CacheConstants.STATUS_ERROR ? "ERROR"
                : "UNKNOWN";
            ht.put( "stat", stat );

            data.add( ht );
        }
        return data;
    }

}
