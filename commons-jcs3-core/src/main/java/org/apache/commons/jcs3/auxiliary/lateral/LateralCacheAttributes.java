package org.apache.commons.jcs3.auxiliary.lateral;

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

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheAttributes;

/**
 * This class stores attributes for all of the available lateral cache auxiliaries.
 */
public class LateralCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements ILateralCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -3408449508837393660L;

    /** Default receive setting */
    private static final boolean DEFAULT_RECEIVE = true;

    /** Indicates the lateral type, this needs to change */
    private Type transmissionType = Type.UDP;

    /** The http servers */
    private String httpServers;

    /** Used to identify the service that this manager will be operating on */
    private String httpServer = "";

    /** This needs to change */
    private String udpMulticastAddr = "228.5.6.7";

    /** This needs to change */
    private int udpMulticastPort = 6789;

    /** This needs to change */
    private int httpListenerPort = 8080;

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
     * Gets the httpListenerPort attribute of the ILateralCacheAttributes object
     * @return The httpListenerPort value
     */
    @Override
    public int getHttpListenerPort()
    {
        return this.httpListenerPort;
    }

    /**
     * Gets the httpServer attribute of the LateralCacheAttributes object
     * @return The httpServer value
     */
    @Override
    public String getHttpServer()
    {
        return httpServer;
    }

    /**
     * Gets the httpSrvers attribute of the LateralCacheAttributes object
     * @return The httpServers value
     */
    @Override
    public String getHttpServers()
    {
        return httpServers;
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
     * Gets the transmissionType attribute of the LateralCacheAttributes object
     * @return The transmissionType value
     */
    @Override
    public Type getTransmissionType()
    {
        return this.transmissionType;
    }

    /**
     * Gets the udpMulticastAddr attribute of the LateralCacheAttributes object
     * @return The udpMulticastAddr value
     */
    @Override
    public String getUdpMulticastAddr()
    {
        return udpMulticastAddr;
    }

    /**
     * Gets the udpMulticastPort attribute of the LateralCacheAttributes object
     * @return The udpMulticastPort value
     */
    @Override
    public int getUdpMulticastPort()
    {
        return udpMulticastPort;
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
     * Sets the httpListenerPort attribute of the ILateralCacheAttributes object
     * @param val The new tcpListenerPort value
     */
    @Override
    public void setHttpListenerPort( final int val )
    {
        this.httpListenerPort = val;
    }

    /**
     * Sets the httpServer attribute of the LateralCacheAttributes object
     *
     * @param val The new httpServer value
     */
    @Override
    public void setHttpServer( final String val )
    {
        httpServer = val;
    }

    /**
     * Sets the httpServers attribute of the LateralCacheAttributes object
     * @param val The new httpServers value
     */
    @Override
    public void setHttpServers( final String val )
    {
        httpServers = val;
    }

    /**
     * Sets the outgoingOnlyMode attribute of the ILateralCacheAttributes. When this is true the
     * lateral cache will only issue put and remove order and will not try to retrieve elements from
     * other lateral caches.
     * @param val The new transmissionTypeName value
     */
    @Override
    public void setPutOnlyMode( final boolean val )
    {
        this.putOnlyMode = val;
    }

    /**
     * @param receive The receive to set.
     */
    @Override
    public void setReceive( final boolean receive )
    {
        this.receive = receive;
    }

    /**
     * Sets the transmissionType attribute of the LateralCacheAttributes object
     * @param val The new transmissionType value
     */
    @Override
    public void setTransmissionType( final Type val )
    {
        this.transmissionType = val;
    }

    /**
     * Sets the udpMulticastAddr attribute of the LateralCacheAttributes object
     * @param val The new udpMulticastAddr value
     */
    @Override
    public void setUdpMulticastAddr( final String val )
    {
        udpMulticastAddr = val;
    }

    /**
     * Sets the udpMulticastPort attribute of the LateralCacheAttributes object
     * @param val The new udpMulticastPort value
     */
    @Override
    public void setUdpMulticastPort( final int val )
    {
        udpMulticastPort = val;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    @Override
    public void setZombieQueueMaxSize( final int zombieQueueMaxSize )
    {
        this.zombieQueueMaxSize = zombieQueueMaxSize;
    }

    /**
     * @return debug string.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        //buf.append( "cacheName=" + cacheName + "\n" );
        //buf.append( "putOnlyMode=" + putOnlyMode + "\n" );
        //buf.append( "transmissionTypeName=" + transmissionTypeName + "\n" );
        //buf.append( "transmissionType=" + transmissionType + "\n" );
        //buf.append( "tcpServer=" + tcpServer + "\n" );
        buf.append( transmissionType.toString() + httpServer + udpMulticastAddr + String.valueOf( udpMulticastPort ) );
        return buf.toString();
    }
}
