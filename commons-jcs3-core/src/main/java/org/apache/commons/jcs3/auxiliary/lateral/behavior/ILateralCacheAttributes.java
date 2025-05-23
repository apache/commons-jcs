package org.apache.commons.jcs3.auxiliary.lateral.behavior;

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

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;

/**
 * This interface defines configuration options common to lateral cache plugins.
 * <p>
 * TODO it needs to be trimmed down. The old version had features for every lateral. Now, the
 * individual laterals have their own specific attributes interfaces.
 */
public interface ILateralCacheAttributes
    extends AuxiliaryCacheAttributes
{
    enum Type
    {
        /** HTTP type */
        HTTP("HTTP"), // 1

        /** UDP type */
        UDP("UDP"), // 2

        /** TCP type */
        TCP("TCP"), // 3

        /** XMLRPC type */
        XMLRPC("XMLRPC"); // 4

        private final String typeName;

        Type(final String typeName)
        {
            this.typeName = typeName;
        }

        /**
         * @since 3.1
         */
        @Override
        public String toString()
        {
            return typeName;
        }
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     */
    int DEFAULT_ZOMBIE_QUEUE_MAX_SIZE = 1000;

    /**
     * Gets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The httpListenerPort value
     */
    int getHttpListenerPort();

    /**
     * Gets the httpServer attribute of the ILateralCacheAttributes object
     *
     * @return The httpServer value
     */
    String getHttpServer();

    /**
     * Gets the httpSrvers attribute of the LateralCacheAttributes object
     *
     * @return The httpServers value
     */
    String getHttpServers();

    /**
     * @return The outgoingOnlyMode value. Stops gets from going remote.
     */
    boolean getPutOnlyMode();

    /**
     * Gets the transmissionType attribute of the ILateralCacheAttributes object
     *
     * @return The transmissionType value
     */
    Type getTransmissionType();

    /**
     * Gets the udpMulticastAddr attribute of the ILateralCacheAttributes object
     *
     * @return The udpMulticastAddr value
     */
    String getUdpMulticastAddr();

    /**
     * Gets the udpMulticastPort attribute of the ILateralCacheAttributes object
     *
     * @return The udpMulticastPort value
     */
    int getUdpMulticastPort();

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

    /**
     * Sets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    void setHttpListenerPort( int val );

    /**
     * Sets the httpServer attribute of the ILateralCacheAttributes object
     *
     * @param val The new httpServer value
     */
    void setHttpServer( String val );

    /**
     * Sets the httpServers attribute of the LateralCacheAttributes object
     *
     * @param val The new httpServers value
     */
    void setHttpServers( String val );

    /**
     * Sets the putOnlyMode attribute of the ILateralCacheAttributes. When this is true the lateral
     * cache will only issue put and remove order and will not try to retrieve elements from other
     * lateral caches.
     *
     * @param val The new transmissionTypeName value
     */
    void setPutOnlyMode( boolean val );

    /**
     * @param receive The receive to set.
     */
    void setReceive( boolean receive );

    /**
     * Sets the transmissionType attribute of the ILateralCacheAttributes object
     *
     * @param val The new transmissionType value
     */
    void setTransmissionType( Type val );

    /**
     * Sets the udpMulticastAddr attribute of the ILateralCacheAttributes object
     *
     * @param val The new udpMulticastAddr value
     */
    void setUdpMulticastAddr( String val );

    /**
     * Sets the udpMulticastPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new udpMulticastPort value
     */
    void setUdpMulticastPort( int val );

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    void setZombieQueueMaxSize( int zombieQueueMaxSize );
}
