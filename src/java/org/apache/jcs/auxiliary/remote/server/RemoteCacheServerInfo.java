package org.apache.jcs.auxiliary.remote.server;

import java.rmi.dgc.VMID;

/**
 * A shared static variable holder for the server
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheServerInfo
{

    // shouldn't be instantiated
    /** Constructor for the RemoteCacheServerInfo object */
    private RemoteCacheServerInfo() { }


    /**
     * Shouldn't be used till after reconneting, after setting = thread safe
     * Used to identify a client, so we can run multiple clients off one host.
     * Need since there is no way to identify a client other than by host in
     * rmi.
     */
    protected static VMID vmid = new VMID();
    /** Description of the Field */
    public static byte listenerId = ( byte ) vmid.hashCode();

}
