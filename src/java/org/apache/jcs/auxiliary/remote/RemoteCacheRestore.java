package org.apache.jcs.auxiliary.remote;

import java.rmi.Naming;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.behavior.ICacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to repair the remote caches managed by the associated instance of
 * RemoteCacheManager. When there is an error the monitor kicks off. The
 * Failover runner starts looks for a manager with a connection to a remote
 * cache that is not in error. If a manager's connection to a remote cache is
 * found to be in error, the restorer kicks off and tries to reconnect. When it
 * is succesful, the status of the manager changes. When the failover runner
 * finds that the primary is in good shape, it will switch back.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheRestore implements ICacheRestore
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheRestore.class );

    private final RemoteCacheManager rcm;
    //private final IAuxiliaryCacheManager rcm;
    private boolean canFix = true;

    private Object remoteObj;


    /**
     * Constructs with the given instance of RemoteCacheManager.
     *
     * @param rcm
     */
    public RemoteCacheRestore( RemoteCacheManager rcm )
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
            //log.error(ex, "host=" + rcm.host + "; port" + rcm.port + "; service=" + rcm.service );
            log.error( "host=" + rcm.host + "; port" + rcm.port + "; service=" + rcm.service );
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
