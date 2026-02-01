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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs4.auxiliary.lateral.behavior.ILateralCacheAttributes;

/**
 * This interface defines functions that are particular to the TCP Lateral Cache
 * plugin. It extends the generic LateralCacheAttributes interface which in turn
 * extends the AuxiliaryCache interface.
 */
public interface ITCPLateralCacheAttributes
    extends ILateralCacheAttributes
{
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
}
