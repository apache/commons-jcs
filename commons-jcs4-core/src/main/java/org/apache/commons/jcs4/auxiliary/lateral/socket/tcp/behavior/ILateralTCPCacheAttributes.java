package org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior;

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

import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheAttributes;

/**
 * This interface defines functions that are particular to the TCP Lateral Cache
 * plugin. It extends the generic LateralCacheAttributes interface which in turn
 * extends the AuxiliaryCache interface.
 */
public interface ILateralTCPCacheAttributes
    extends AuxiliaryCacheAttributes
{
    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     */
    int DEFAULT_ZOMBIE_QUEUE_MAX_SIZE = 1000;

    /**
     * @return the openTimeOut
     */
    int getOpenTimeOut();

    /**
     * @return the socketTimeOut
     */
    int getSocketTimeOut();

    /**
     * Gets the tcpListenerHost attribute of the ILateralCacheAttributes object
     *
     * @return The tcpListenerHost value
     */
    String getTcpListenerHost();

    /**
     * Gets the tcpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The tcpListenerPort value
     */
    int getTcpListenerPort();

    /**
     * Gets the tcpServer attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServer value
     */
    String getTcpServer();

    /**
     * Gets the tcpServers attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServers value
     */
    String getTcpServers();

    /**
     * The address to broadcast to if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryAddr.
     */
    String getUdpDiscoveryAddr();

    /**
     * The UDP discovery network interface if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryInterface.
     */
    String getUdpDiscoveryInterface();

    /**
     * The port to use if UDPDiscovery is enabled.
     *
     * @return the udpDiscoveryPort.
     */
    int getUdpDiscoveryPort();

    /**
     * The time-to-live for the UDP multicast packets
     *
     * @return the udpTTL.
     * @since 3.1
     */
    int getUdpTTL();

    /**
     * Is the lateral allowed to try and get from other laterals.
     *
     * @return true if the lateral will try to get
     */
    boolean isAllowGet();

    /**
     * Is the lateral allowed to put objects to other laterals.
     *
     * @return true if puts are allowed
     */
    boolean isAllowPut();

    /**
     * Should the receiver try to match hash codes. If true, the receiver will
     * see if the client supplied a hash code. If it did, then it will try to get
     * the item locally. If the item exists, then it will compare the hash code.
     * if they are the same, it will not remove. This isn't perfect since
     * different objects can have the same hash code, but it is unlikely of
     * objects of the same type.
     *
     * @return boolean
     */
    boolean isFilterRemoveByHashCode();

    /**
     * Should the client send a remove command rather than a put when update is
     * called. This is a client option, not a receiver option. This allows you
     * to prevent the lateral from serializing objects.
     *
     * @return true if updates will result in a remove command being sent.
     */
    boolean isIssueRemoveOnPut();

    /**
     * Tests whether or not TCP laterals can try to find each other by multicast
     * communication.
     *
     * @return the udpDiscoveryEnabled.
     */
    boolean isUdpDiscoveryEnabled();

    /**
     * @return The outgoingOnlyMode value. Stops gets from going remote.
     */
    boolean getPutOnlyMode();

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @return the zombieQueueMaxSize.
     */
    int getZombieQueueMaxSize();

    /**
     * Should a listener be created. By default this is true.
     * <p>
     * If this is false the lateral will connect to others but it will not create a listener to
     * receive.
     * <p>
     * It is possible if two laterals are misconfigured that lateral A may have a region R1 that is
     * not configured for the lateral but another is. And if cache B has region R1 configured for
     * lateral distribution, A will get messages for R1 but not send them.
     *
     * @return true if we should have a listener connection
     */
    boolean isReceive();
}
