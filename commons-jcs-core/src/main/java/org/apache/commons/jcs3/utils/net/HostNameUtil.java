package org.apache.commons.jcs3.utils.net;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
/*
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.util.List;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Simple utility for getting the local host name.
 * <p>
 * @author Aaron Smuts
 */
public class HostNameUtil
{
    /** The logger. */
    private static final Log log = LogManager.getLog( HostNameUtil.class );

    /**
     * Gets the address for the local machine.
     * <p>
     * @return InetAddress.getLocalHost().getHostAddress()
     * @throws UnknownHostException
     */
    public static String getLocalHostAddress() throws UnknownHostException
    {
        final String hostAddress = getLocalHostLANAddress().getHostAddress();
        log.debug( "hostAddress = [{0}]", hostAddress );
        return hostAddress;
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's
     * LAN IP address.
     * <p>
     * This method is intended for use as a replacement of JDK method
     * <code>InetAddress.getLocalHost</code>, because that method is ambiguous on Linux systems.
     * Linux systems enumerate the loopback network interface the same way as regular LAN network
     * interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not specify the
     * algorithm used to select the address returned under such circumstances, and will often return
     * the loopback address, which is not valid for network communication. Details <a
     * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p>
     * This method will scan all IP addresses on all network interfaces on the host machine to
     * determine the IP address most likely to be the machine's LAN address. If the machine has
     * multiple IP addresses, this method will prefer a site-local IP address (e.g. 192.168.x.x or
     * 10.10.x.x, usually IPv4) if the machine has one (and will return the first site-local address
     * if the machine has more than one), but if the machine does not hold a site-local address,
     * this method will return simply the first non-loopback address found (IPv4 or IPv6).</p>
     * <p>
     * If this method cannot find a non-loopback address using this selection algorithm, it will
     * fall back to calling and returning the result of JDK method
     * <code>InetAddress.getLocalHost</code>.
     * <p>
     * <a href="http://issues.apache.org/jira/browse/JCS-40">JIR ISSUE JCS-40</a>
     * <p>
     * @return InetAddress
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     * @since 3.1
     */
    public static InetAddress getLocalHostLANAddress()
        throws UnknownHostException
    {
        return getLocalHostLANAddresses().stream().findFirst().orElse(null);
    }

    /**
     * Returns all <code>InetAddress</code> objects encapsulating what are most likely the machine's
     * LAN IP addresses.
     * <p>
     * This method will scan all IP addresses on all network interfaces on the host machine to
     * determine the IP addresses most likely to be the machine's LAN addresses.
     * <p>
     * @return List<InetAddress>
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    public static List<InetAddress> getLocalHostLANAddresses()
        throws UnknownHostException
    {
        List<InetAddress> addresses = new ArrayList<>();

        try
        {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while ( ifaces.hasMoreElements() )
            {
                final NetworkInterface iface = ifaces.nextElement();

                // Skip loopback interfaces
                if (iface.isLoopback() || !iface.isUp())
                {
                    continue;
                }

                // Iterate all IP addresses assigned to each card...
                for ( final Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); )
                {
                    final InetAddress inetAddr = inetAddrs.nextElement();
                    if ( !inetAddr.isLoopbackAddress() )
                    {
                        if (inetAddr.isSiteLocalAddress())
                        {
                            // Found non-loopback site-local address.
                            addresses.add(inetAddr);
                        }
                        if ( candidateAddress == null )
                        {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null && addresses.isEmpty())
            {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                addresses.add(candidateAddress);
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            if (addresses.isEmpty())
            {
                final InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                if ( jdkSuppliedAddress == null )
                {
                    throw new UnknownHostException( "The JDK InetAddress.getLocalHost() method unexpectedly returned null." );
                }
                addresses.add(jdkSuppliedAddress);
            }
        }
        catch ( final SocketException e )
        {
            final UnknownHostException unknownHostException = new UnknownHostException( "Failed to determine LAN address: "
                + e );
            unknownHostException.initCause( e );
            throw unknownHostException;
        }

        return addresses;
    }

    /**
     * On systems with multiple network interfaces and mixed IPv6/IPv4 get a valid network
     * interface for binding to multicast
     *
     * @return a network interface suitable for multicast
     * @throws SocketException if a problem occurs while reading the network interfaces
     */
    public static NetworkInterface getMulticastNetworkInterface() throws SocketException
    {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements())
        {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            final Enumeration<InetAddress> addressesFromNetworkInterface = networkInterface.getInetAddresses();
            while (addressesFromNetworkInterface.hasMoreElements())
            {
                final InetAddress inetAddress = addressesFromNetworkInterface.nextElement();
                if (inetAddress.isSiteLocalAddress()
                        && !inetAddress.isAnyLocalAddress()
                        && !inetAddress.isLinkLocalAddress()
                        && !inetAddress.isLoopbackAddress()
                        && !inetAddress.isMulticastAddress())
                {
                    return networkInterface;
                }
            }
        }

        return null;
    }
}
