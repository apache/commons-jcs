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

/**
 * Configuration properties for UDP discover service.
 * <p>
 * The service will allow out applications to find each other.
 * </p>
 */
public final class UDPDiscoveryAttributes
    implements Cloneable
{
    /** Default udp discovery address */
    private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.4.5.6";

    /** Default udp discovery port */
    private static final int DEFAULT_UDP_DISCOVERY_PORT = 5678;

    /** Default amount of time before we remove services that we haven't heard from */
    private static final int DEFAULT_MAX_IDLE_TIME_SEC = 180;

    /** Service address */
    private String serviceAddress;

    /** Service port */
    private int servicePort;

    /** Udp discovery address */
    private String udpDiscoveryAddr = DEFAULT_UDP_DISCOVERY_ADDRESS;

    /** Udp discovery network interface */
    private String udpDiscoveryInterface;

    /** Udp discovery port */
    private int udpDiscoveryPort = DEFAULT_UDP_DISCOVERY_PORT;

    /** Udp datagram TTL */
    private int udpTTL;

    /** Amount of time before we remove services that we haven't heard from */
    private int maxIdleTimeSec = DEFAULT_MAX_IDLE_TIME_SEC;

    /** @return a clone of this object */
    @Override
    public UDPDiscoveryAttributes clone()
    {
        final UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
        attributes.setMaxIdleTimeSec( getMaxIdleTimeSec() );
        attributes.setServicePort( getServicePort() );
        attributes.setUdpDiscoveryAddr( getUdpDiscoveryAddr() );
        attributes.setUdpDiscoveryPort( getUdpDiscoveryPort() );
        return attributes;
    }

    /**
     * @return the maxIdleTimeSec.
     */
    public int getMaxIdleTimeSec()
    {
        return maxIdleTimeSec;
    }

    /**
     * @return the serviceAddress.
     */
    public String getServiceAddress()
    {
        return serviceAddress;
    }

    /**
     * @return the servicePort.
     */
    public int getServicePort()
    {
        return servicePort;
    }

    /**
     * @return the udpDiscoveryAddr.
     */
    public String getUdpDiscoveryAddr()
    {
        return udpDiscoveryAddr;
    }

    /**
     * @return the udpDiscoveryInterface.
     */
    public String getUdpDiscoveryInterface()
    {
        return udpDiscoveryInterface;
    }

    /**
     * @return the udpDiscoveryPort.
     */
    public int getUdpDiscoveryPort()
    {
        return udpDiscoveryPort;
    }

    /**
     * @return the udpTTL.
     */
    public int getUdpTTL()
    {
        return udpTTL;
    }

    /**
     * @param maxIdleTimeSec The maxIdleTimeSec to set.
     */
    public void setMaxIdleTimeSec( final int maxIdleTimeSec )
    {
        this.maxIdleTimeSec = maxIdleTimeSec;
    }

    /**
     * @param serviceAddress The serviceAddress to set.
     */
    public void setServiceAddress( final String serviceAddress )
    {
        this.serviceAddress = serviceAddress;
    }

    /**
     * @param servicePort The servicePort to set.
     */
    public void setServicePort( final int servicePort )
    {
        this.servicePort = servicePort;
    }

    /**
     * @param udpDiscoveryAddr The udpDiscoveryAddr to set.
     */
    public void setUdpDiscoveryAddr( final String udpDiscoveryAddr )
    {
        this.udpDiscoveryAddr = udpDiscoveryAddr;
    }

    /**
     * @param udpDiscoveryInterface The udpDiscoveryInterface to set.
     */
    public void setUdpDiscoveryInterface( final String udpDiscoveryInterface )
    {
        this.udpDiscoveryInterface = udpDiscoveryInterface;
    }

    /**
     * @param udpDiscoveryPort The udpDiscoveryPort to set.
     */
    public void setUdpDiscoveryPort( final int udpDiscoveryPort )
    {
        this.udpDiscoveryPort = udpDiscoveryPort;
    }

    /**
     * @param udpTTL The udpTTL to set.
     */
    public void setUdpTTL( final int udpTTL )
    {
        this.udpTTL = udpTTL;
    }

    /**
     * @return string for debugging purposes.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n UDPDiscoveryAttributes" );
        buf.append( "\n ServiceAddress = [" + getServiceAddress() + "]" );
        buf.append( "\n ServicePort = [" + getServicePort() + "]" );
        buf.append( "\n UdpDiscoveryAddr = [" + getUdpDiscoveryAddr() + "]" );
        buf.append( "\n UdpDiscoveryPort = [" + getUdpDiscoveryPort() + "]" );
        buf.append( "\n MaxIdleTimeSec = [" + getMaxIdleTimeSec() + "]" );
        return buf.toString();
    }
}
