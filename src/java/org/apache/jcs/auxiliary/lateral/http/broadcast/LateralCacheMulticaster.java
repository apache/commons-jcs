package org.apache.jcs.auxiliary.lateral.http.broadcast;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jcs.auxiliary.lateral.http.broadcast.LateralCacheUnicaster;

import org.apache.jcs.engine.behavior.ICacheElement;

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
public class LateralCacheMulticaster
{
    private final static Log log =
        LogFactory.getLog( LateralCacheMulticaster.class );

    private final static String servlet = "/cache/cache/LateralCacheServletReceiver";
    private final ICacheElement ice;
    private final ArrayList servers;

    /**
     * Constructor for the LateralCacheMulticaster object
     *
     * @param ice
     * @param servers
     */
    public LateralCacheMulticaster( ICacheElement ice, ArrayList servers )
    {
        this.servers = servers;
        this.ice = ice;

        if ( log.isDebugEnabled() )
        {
            log.debug( "In DistCacheMulticaster" );
        }
    }
    // end constructor

    /** Multi-casts the cache changes to the distributed servers. */
    public void multicast()
    {

        ThreadPoolManager tpm = ThreadPoolManager.getInstance();
        Iterator it = servers.iterator();
        while ( it.hasNext() )
        {
            tpm.runIt( new LateralCacheUnicaster( ice, ( String ) it.next() + servlet ) );
        }
        return;
    }
    // end run

}
// end class


