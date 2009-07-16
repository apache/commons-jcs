package org.apache.jcs.utils.discovery;

import java.rmi.dgc.VMID;

/**
 * Provides info for the udp discovery service.
 * <p>
 * @author Aaron Smuts
 */
public class UDPDiscoveryInfo
{
    /**
     * jvm unique identifier.
     */
    protected static VMID vmid = new VMID();

    /**
     * Identifies the listener, so we don't add ourselves to the list of known services.
     */
    public static long listenerId = vmid.hashCode();
}
