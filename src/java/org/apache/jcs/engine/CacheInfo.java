package org.apache.jcs.engine;

import java.rmi.dgc.VMID;

/**
 * A shared static variable holder for the lateral cache
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheInfo
{

    // shouldn't be instantiated
    /** Constructor for the CacheInfo object */
    private CacheInfo() { }


    /**
     * Shouldn't be used till after reconneting, after setting = thread safe
     * Used to identify a client, so we can run multiple clients off one host.
     * Need since there is no way to identify a client other than by host in
     * rmi. TODO: may have some trouble in failover mode if the cache keeps its
     * old id. We may need to reset this when moving into failover.
     */
    protected static VMID vmid = new VMID();
    /** Description of the Field */
    public static byte listenerId = ( byte ) vmid.hashCode();

}
