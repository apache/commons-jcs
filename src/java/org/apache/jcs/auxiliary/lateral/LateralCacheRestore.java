package org.apache.jcs.auxiliary.lateral;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPService;

import org.apache.jcs.auxiliary.lateral.socket.udp.LateralUDPService;

import org.apache.jcs.auxiliary.lateral.javagroups.LateralJGService;

import org.apache.jcs.auxiliary.lateral.xmlrpc.LateralXMLRPCService;

import org.apache.jcs.engine.behavior.ICacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to repair the lateral caches managed by the associated instance of
 * LateralCacheManager.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheRestore implements ICacheRestore
{
    private final static Log log =
        LogFactory.getLog( LateralCacheRestore.class );

    private final LateralCacheManager lcm;
    private boolean canFix = true;

    private Object lateralObj;


    /**
     * Constructs with the given instance of LateralCacheManager.
     *
     * @param lcm
     */
    public LateralCacheRestore( LateralCacheManager lcm )
    {
        this.lcm = lcm;
    }


    /**
     * Returns true iff the connection to the lateral host for the corresponding
     * cache manager can be successfully re-established.
     */
    public boolean canFix()
    {
        if ( !canFix )
        {
            return canFix;
        }

        try
        {

            // restore based on type.  Only the tcp scoket type really needs restoring.
            if ( lcm.lca.getTransmissionType() == lcm.lca.UDP )
            {
                lateralObj = new LateralUDPService( lcm.lca );
            }
            else
                if ( lcm.lca.getTransmissionType() == lcm.lca.JAVAGROUPS )
            {
                lateralObj = new LateralJGService( lcm.lca );
            }
            else
                if ( lcm.lca.getTransmissionType() == lcm.lca.XMLRPC )
            {
                lateralObj = new LateralXMLRPCService( lcm.lca );
            }
            else
                if ( lcm.lca.getTransmissionType() == lcm.lca.TCP )
            {
                lateralObj = new LateralTCPService( lcm.lca );
            }
            else
                if ( lcm.lca.getTransmissionType() == lcm.lca.HTTP )
            {

            }
        }
        catch ( Exception ex )
        {
            log.error( "Can't fix " + ex.getMessage() );
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
        lcm.fixCaches( ( ILateralCacheService ) lateralObj, ( ILateralCacheObserver ) lateralObj );
        String msg = "Lateral connection resumed.";
        log.info( msg );
        log.debug( msg );
    }
}
