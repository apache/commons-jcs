package org.apache.commons.jcs4.utils.discovery;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior.ILateralTCPCacheAttributes;

/**
 * Configuration properties for UDP discover service.
 * <p>
 * The service will allow applications to find each other.
 * </p>
 */
public record UDPDiscoveryAttributes(
        /** Service address */
        String serviceAddress,

        /** Service port */
        int servicePort,

        /** Udp discovery address */
        String udpDiscoveryAddr,

        /** Udp discovery network interface */
        String udpDiscoveryInterface,

        /** Udp discovery port */
        int udpDiscoveryPort,

        /** Udp datagram TTL */
        int udpTTL,

        /** Amount of time before we remove services that we haven't heard from */
        int maxIdleTimeSec
)
{
    /** Default udp discovery address */
    private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.4.5.6";

    /** Default udp discovery port */
    private static final int DEFAULT_UDP_DISCOVERY_PORT = 5678;

    /** Default amount of time before we remove services that we haven't heard from */
    private static final int DEFAULT_MAX_IDLE_TIME_SEC = 180;

    /** Record with all defaults set */
    private static final UDPDiscoveryAttributes DEFAULT = new UDPDiscoveryAttributes(
            null,
            -1,
            DEFAULT_UDP_DISCOVERY_ADDRESS,
            null,
            DEFAULT_UDP_DISCOVERY_PORT,
            0,
            DEFAULT_MAX_IDLE_TIME_SEC
            );

    /**
     * @return an object containing the default settings
     */
    public static UDPDiscoveryAttributes defaults()
    {
        return DEFAULT;
    }

    /**
     * Constructor from ILateralTCPCacheAttributes
     *
     * @param lac lateral cache configuration object
     */
    public UDPDiscoveryAttributes(ILateralTCPCacheAttributes lac)
    {
        this(lac.getTcpListenerHost(), lac.getTcpListenerPort(), lac.getUdpDiscoveryAddr(),
            lac.getUdpDiscoveryInterface(), lac.getUdpDiscoveryPort(), lac.getUdpTTL(),
            defaults().maxIdleTimeSec());
    }

    /**
     * Constructor with necessary fields only
     *
     * @param servicePort
     * @param udpDiscoveryAddr
     * @param udpDiscoveryPort
     * @param udpTTL
     */
    public UDPDiscoveryAttributes(int servicePort, String udpDiscoveryAddr, int udpDiscoveryPort,
        int udpTTL)
    {
        this(defaults().serviceAddress(),
            servicePort,
            udpDiscoveryAddr,
            defaults().udpDiscoveryInterface(),
            udpDiscoveryPort,
            udpTTL,
            defaults().maxIdleTimeSec()
            );
    }

    /**
     * Get a new record with the given serviceAddress
     *
     * @param serviceAddress new serviceAddress
     * @return new record with the given serviceAddress
     */
    public UDPDiscoveryAttributes withServiceAddress(String serviceAddress)
    {
        return new UDPDiscoveryAttributes(
                serviceAddress,
                servicePort(),
                udpDiscoveryAddr(),
                udpDiscoveryInterface(),
                udpDiscoveryPort(),
                udpTTL(),
                maxIdleTimeSec()
                );
    }

    /**
     * @return string for debugging purposes.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("\n UDPDiscoveryAttributes");
        buf.append("\n ServiceAddress = [").append(serviceAddress()).append("]");
        buf.append("\n ServicePort = [").append(servicePort()).append("]");
        buf.append("\n UdpDiscovery = [").append(udpDiscoveryAddr()).append("]");
        buf.append("\n UdpDiscoveryPort = [").append(udpDiscoveryPort()).append("]");
        buf.append("\n MaxIdleTimeSec = [").append(maxIdleTimeSec()).append("]");
        return buf.toString();
    }
}
