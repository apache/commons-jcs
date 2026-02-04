package org.apache.commons.jcs4.auxiliary.lateral.socket.tcp;

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

import org.apache.commons.jcs4.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior.ILateralTCPCacheAttributes;

/**
 * This interface defines functions that are particular to the TCP Lateral Cache plugin. It extends
 * the generic LateralCacheAttributes interface which in turn extends the AuxiliaryCache interface.
 */
public class LateralTCPCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements ILateralTCPCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = 1077889204513905220L;

    /** Default */
    private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.5.6.7";

    /** Default */
    private static final int DEFAULT_UDP_DISCOVERY_PORT = 6789;

    /** Default */
    private static final boolean DEFAULT_UDP_DISCOVERY_ENABLED = true;

    /** Default */
    private static final boolean DEFAULT_ALLOW_GET = true;

    /** Default */
    private static final boolean DEFAULT_ALLOW_PUT = true;

    /** Default */
    private static final boolean DEFAULT_ISSUE_REMOVE_FOR_PUT = false;

    /** Default */
    private static final boolean DEFAULT_FILTER_REMOVE_BY_HASH_CODE = true;

    /** Default - Only block for 1 second before timing out on a read.*/
    private static final int DEFAULT_SOCKET_TIME_OUT = 1000;

    /** Default - Only block for 2 seconds before timing out on startup.*/
    private static final int DEFAULT_OPEN_TIMEOUT = 2000;

    /** TCP -------------------------------------------- */
    private String tcpServers = "";

    /** Used to identify the service that this manager will be operating on */
    private String tcpServer = "";

    /** The port */
    private int tcpListenerPort;

    /** The host */
    private String tcpListenerHost = "";

    /** Udp discovery for tcp server */
    private String udpDiscoveryAddr = DEFAULT_UDP_DISCOVERY_ADDRESS;

    /** Udp discovery network interface */
    private String udpDiscoveryInterface = null;

    /** Discovery port */
    private int udpDiscoveryPort = DEFAULT_UDP_DISCOVERY_PORT;

    /** Discovery switch */
    private boolean udpDiscoveryEnabled = DEFAULT_UDP_DISCOVERY_ENABLED;

    /** Udp datagram TTL */
    private int udpTTL;

    /** Can we put */
    private boolean allowPut = DEFAULT_ALLOW_GET;

    /** Can we go laterally for a get */
    private boolean allowGet = DEFAULT_ALLOW_PUT;

    /** Call remove when there is a put */
    private boolean issueRemoveOnPut = DEFAULT_ISSUE_REMOVE_FOR_PUT;

    /** Don't remove it the hash code is the same */
    private boolean filterRemoveByHashCode = DEFAULT_FILTER_REMOVE_BY_HASH_CODE;

    /** Only block for socketTimeOut seconds before timing out on a read.  */
    private int socketTimeOut = DEFAULT_SOCKET_TIME_OUT;

    /** Only block for openTimeOut seconds before timing out on startup. */
    private int openTimeOut = DEFAULT_OPEN_TIMEOUT;


    /** Default receive setting */
    private static final boolean DEFAULT_RECEIVE = true;

    /** Disables gets from laterals */
    private boolean putOnlyMode = true;

    /**
     * do we receive and broadcast or only broadcast this is useful when you don't want to get any
     * notifications
     */
    private boolean receive = DEFAULT_RECEIVE;

    /** If the primary fails, we will queue items before reconnect.  This limits the number of items that can be queued. */
    private int zombieQueueMaxSize = DEFAULT_ZOMBIE_QUEUE_MAX_SIZE;

    /**
     * @return the openTimeOut
     */
    @Override
    public int getOpenTimeOut()
    {
        return openTimeOut;
    }

    /**
     * @return the socketTimeOut
     */
    @Override
    public int getSocketTimeOut()
    {
        return socketTimeOut;
    }

    /**
     * Gets the tcpListenerHost attribute of the ILateralCacheAttributes object
     *
     * @return The tcpListenerHost value
     */
    @Override
    public String getTcpListenerHost()
    {
        return this.tcpListenerHost;
    }

    /**
     * Gets the tcpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The tcpListenerPort value
     */
    @Override
    public int getTcpListenerPort()
    {
        return this.tcpListenerPort;
    }

    /**
     * Gets the tcpServer attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServer value
     */
    @Override
    public String getTcpServer()
    {
        return this.tcpServer;
    }

    /**
     * Gets the tcpServers attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServers value
     */
    @Override
    public String getTcpServers()
    {
        return this.tcpServers;
    }

    /**
     * The address to broadcast to if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryAddr.
     */
    @Override
    public String getUdpDiscoveryAddr()
    {
        return this.udpDiscoveryAddr;
    }

    /**
     * The UDP discovery network interface if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryInterface.
     */
    @Override
    public String getUdpDiscoveryInterface()
    {
        return this.udpDiscoveryInterface;
    }

   /**
     * The port to use if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryPort.
     */
    @Override
    public int getUdpDiscoveryPort()
    {
        return this.udpDiscoveryPort;
    }

    /**
     * The time-to-live for the UDP multicast packets
     *
     * @return the udpTTL.
     * @since 3.1
     */
    @Override
    public int getUdpTTL()
    {
        return udpTTL;
    }

    /**
     * Is the lateral allowed to try and get from other laterals.
     *
     * @return true if the lateral will try to get
     */
    @Override
    public boolean isAllowGet()
    {
        return this.allowGet;
    }

    /**
     * Is the lateral allowed to put objects to other laterals.
     *
     * @return true if puts are allowed
     */
    @Override
    public boolean isAllowPut()
    {
        return this.allowPut;
    }

    /**
     * Should the receiver try to match hash codes. If true, the receiver will see if the client
     * supplied a hash code. If it did, then it will try to get the item locally. If the item exists,
     * then it will compare the hash code. if they are the same, it will not remove. This isn't
     * perfect since different objects can have the same hash code, but it is unlikely of objects of
     * the same type.
     *
     * @return boolean
     */
    @Override
    public boolean isFilterRemoveByHashCode()
    {
        return this.filterRemoveByHashCode;
    }

    /**
     * Should the client send a remove command rather than a put when update is called. This is a
     * client option, not a receiver option. This allows you to prevent the lateral from serializing
     * objects.
     *
     * @return true if updates will result in a remove command being sent.
     */
    @Override
    public boolean isIssueRemoveOnPut()
    {
        return this.issueRemoveOnPut;
    }

    /**
     * Tests whether or not TCP laterals can try to find each other by multicast communication.
     *
     * @return the udpDiscoveryEnabled.
     */
    @Override
    public boolean isUdpDiscoveryEnabled()
    {
        return this.udpDiscoveryEnabled;
    }

    /**
     * Is the lateral allowed to try and get from other laterals.
     * <p>
     * This replaces the old putOnlyMode
     *
     * @param allowGet
     */
    public void setAllowGet( final boolean allowGet )
    {
        this.allowGet = allowGet;
    }

    /**
     * Is the lateral allowed to put objects to other laterals.
     *
     * @param allowPut
     */
    public void setAllowPut( final boolean allowPut )
    {
        this.allowPut = allowPut;
    }

    /**
     * Should the receiver try to match hash codes. If true, the receiver will see if the client
     * supplied a hash code. If it did, then it will try to get the item locally. If the item exists,
     * then it will compare the hash code. if they are the same, it will not remove. This isn't
     * perfect since different objects can have the same hash code, but it is unlikely of objects of
     * the same type.
     *
     * @param filter
     */
    public void setFilterRemoveByHashCode( final boolean filter )
    {
        this.filterRemoveByHashCode = filter;
    }

    /**
     * Should the client send a remove command rather than a put when update is called. This is a
     * client option, not a receiver option. This allows you to prevent the lateral from serializing
     * objects.
     *
     * @param issueRemoveOnPut
     */
    public void setIssueRemoveOnPut( final boolean issueRemoveOnPut )
    {
        this.issueRemoveOnPut = issueRemoveOnPut;
    }

    /**
     * @param openTimeOut the openTimeOut to set
     */
    public void setOpenTimeOut( final int openTimeOut )
    {
        this.openTimeOut = openTimeOut;
    }

    /**
     * @param socketTimeOut the socketTimeOut to set
     */
    public void setSocketTimeOut( final int socketTimeOut )
    {
        this.socketTimeOut = socketTimeOut;
    }

    /**
     * Sets the tcpListenerHost attribute of the ILateralCacheAttributes object
     *
     * @param val
     *            The new tcpListenerHost value
     */
    public void setTcpListenerHost( final String val )
    {
        this.tcpListenerHost = val;
    }

    /**
     * Sets the tcpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    public void setTcpListenerPort( final int val )
    {
        this.tcpListenerPort = val;
    }

    /**
     * Sets the tcpServer attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpServer value
     */
    public void setTcpServer( final String val )
    {
        this.tcpServer = val;
    }

    /**
     * Sets the tcpServers attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpServers value
     */
    public void setTcpServers( final String val )
    {
        this.tcpServers = val;
    }

    /**
     * Sets the address to broadcast to if UDPDiscovery is enabled.
     *
     * @param udpDiscoveryAddr The udpDiscoveryAddr to set.
     */
    public void setUdpDiscoveryAddr( final String udpDiscoveryAddr )
    {
        this.udpDiscoveryAddr = udpDiscoveryAddr;
    }

    /**
     * Can setup UDP Discovery. This only works for TCP laterals right now. It allows TCP laterals
     * to find each other by broadcasting to a multicast port.
     *
     * @param udpDiscoveryEnabled The udpDiscoveryEnabled to set.
     */
    public void setUdpDiscoveryEnabled( final boolean udpDiscoveryEnabled )
    {
        this.udpDiscoveryEnabled = udpDiscoveryEnabled;
    }

    /**
     * Sets the UDP discovery network interface if UDPDiscovery is enabled.
     *
     * @param udpDiscoveryInterface the udpDiscoveryInterface to set (symbolic name)
     */
    public void setUdpDiscoveryInterface(String udpDiscoveryInterface)
    {
        this.udpDiscoveryInterface = udpDiscoveryInterface;
    }

    /**
     * Sets the port to use if UDPDiscovery is enabled.
     *
     * @param udpDiscoveryPort The udpDiscoveryPort to set.
     */
    public void setUdpDiscoveryPort( final int udpDiscoveryPort )
    {
        this.udpDiscoveryPort = udpDiscoveryPort;
    }

    /**
     * Sets the time-to-live for the UDP multicast packet
     *
     * @param udpTTL The udpTTL to set.
     * @since 3.1
     */
    public void setUdpTTL( final int udpTTL )
    {
        this.udpTTL = udpTTL;
    }

    /**
     * @return The outgoingOnlyMode value. Stops gets from going remote.
     */
    @Override
    public boolean getPutOnlyMode()
    {
        return putOnlyMode;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @return the zombieQueueMaxSize.
     */
    @Override
    public int getZombieQueueMaxSize()
    {
        return zombieQueueMaxSize;
    }

    /**
     * @return the receive.
     */
    @Override
    public boolean isReceive()
    {
        return receive;
    }

    /**
     * Sets the outgoingOnlyMode attribute of the ILateralCacheAttributes. When this is true the
     * lateral cache will only issue put and remove order and will not try to retrieve elements from
     * other lateral caches.
     * @param val The new transmissionTypeName value
     */
    public void setPutOnlyMode( final boolean val )
    {
        this.putOnlyMode = val;
    }

    /**
     * @param receive The receive to set.
     */
    public void setReceive( final boolean receive )
    {
        this.receive = receive;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    public void setZombieQueueMaxSize( final int zombieQueueMaxSize )
    {
        this.zombieQueueMaxSize = zombieQueueMaxSize;
    }

    /**
     * Used to key the instance TODO create another method for this and use toString for debugging
     * only.
     *
     * @return String
     */
    @Override
    public String toString()
    {
        return getTcpServer() + ":" + getTcpListenerPort();
    }
}
