package org.apache.jcs.auxiliary.lateral.http.remove;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jcs.auxiliary.lateral.http.remove.DeleteLateralCacheUnicaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.threads.ThreadPoolManager;

/*
 * Used to multi-cast a key/val pair to the named cache on multiple servers.
 */
/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class DeleteLateralCacheMulticaster
{
    private final static Log log =
        LogFactory.getLog( DeleteLateralCacheMulticaster.class );

    // must get servletName from the props file
    private final String servlet;

    private final String hashtableName;
    private final String key;

    private final ArrayList servers;

    /**
     * Constructor for the DeleteLateralCacheMulticaster object
     *
     * @param hashtableName
     * @param key
     * @param servers
     * @param servlet
     */
    public DeleteLateralCacheMulticaster( String hashtableName, String key, ArrayList servers, String servlet )
    {
        this.hashtableName = hashtableName;
        this.key = key;
        this.servers = servers;
        this.servlet = servlet;

        if ( log.isDebugEnabled() )
        {
            log.debug( "In DistCacheMulticaster" );
        }
    }
    // end constructor

    /** Multi-casts the deltes to the distributed servers. */
    public void multicast()
    {

        ThreadPoolManager tpm = ThreadPoolManager.getInstance();
        Iterator it = servers.iterator();
        //p( "iterating through servers" );
        while ( it.hasNext() )
        {
            String url = ( String ) it.next() + servlet;
            //p( "url = " + url );
            tpm.runIt( new DeleteLateralCacheUnicaster( hashtableName, key, url ) );
        }
        return;
    }
}

