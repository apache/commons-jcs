package org.apache.jcs.auxiliary.remote.server;

import java.rmi.Naming;
import java.rmi.Remote;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.behavior.ICacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// may not be necessary.  cloning the remote cache framework for the cluster
// connections.  may be able to use old, but it is probably better to move away
// from the cache or regionally defined framework to a cluster connection.
// this restore might be useless since the remote cacehs never put to each other
// as a service; rather they communicate to listeners.  This does give us the
// option to do that later though.  It seems like an ugly copy and paste job though.

/**
 * Used to repair the remote caches managed by the associated instance of
 * RemoteCacheManager.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheClusterRestore implements ICacheRestore
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheClusterRestore.class );

    private final RemoteCacheClusterManager rcm;
    //private final IAuxiliaryCacheManager rcm;
    private boolean canFix = true;

    private Object remoteObj;


    /**
     * Constructs with the given instance of RemoteCacheManager.
     *
     * @param rcm
     */
    public RemoteCacheClusterRestore( RemoteCacheClusterManager rcm )
    {
        //public RemoteCacheRestore(IAuxiliaryCacheManager rcm) {
        this.rcm = rcm;
    }


    /**
     * Returns true if the connection to the remote host for the corresponding
     * cache manager can be successfully re-established.
     */
    public boolean canFix()
    {
        if ( !canFix )
        {
            return canFix;
        }
        String registry = "//" + rcm.host + ":" + rcm.port + "/" + rcm.service;
        log.info( "looking up server " + registry );
        try
        {
            remoteObj = Naming.lookup( registry );
            log.info( "looking up server " + registry );
        }
        catch ( Exception ex )
        {
            log.error( "host=" + rcm.host + "; port" + rcm.port + "; service=" + rcm.service, ex );
            canFix = false;
        }
        return canFix;
    }


    /**
     * Fixes up all the caches managed by the associated cache manager.
     */
    public void fix()
    {
        if ( !canFix )
        {
            return;
        }
        rcm.fixCaches( ( IRemoteCacheService ) remoteObj, ( IRemoteCacheObserver ) remoteObj );
        String msg = "Remote connection to " + "//" + rcm.host + ":" + rcm.port + "/" + rcm.service + " resumed.";
        log.info( msg );
    }
}
