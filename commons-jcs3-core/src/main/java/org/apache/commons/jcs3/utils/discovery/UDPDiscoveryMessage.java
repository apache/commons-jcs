package org.apache.commons.jcs3.utils.discovery;

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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The message sent by the discovery mechanism.
 */
public class UDPDiscoveryMessage
    implements Serializable
{
    public enum BroadcastType
    {
        /**
         * This is the periodic broadcast of a servers location. This type of message is also sent in
         * response to a REQUEST_BROADCAST.
         */
        PASSIVE,

        /**
         * This asks recipients to broadcast their location. This is used on startup.
         */
        REQUEST,

        /**
         * This message instructs the receiver to remove this service from its list.
         */
        REMOVE
    }

    /** Don't change */
    private static final long serialVersionUID = -5332377899560951793L;

    /** The message type */
    private BroadcastType messageType = BroadcastType.PASSIVE;

    /** Udp port */
    private int port = 6789;

    /** UDP host */
    private String host = "228.5.6.7";

    /** Id of the requester, allows self-filtration */
    private long requesterId;

    /** Names of regions */
    private ArrayList<String> cacheNames = new ArrayList<>();

    /**
     * @return the cacheNames.
     */
    public ArrayList<String> getCacheNames()
    {
        return cacheNames;
    }

    /**
     * @return the host.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the messageType.
     */
    public BroadcastType getMessageType()
    {
        return messageType;
    }

    /**
     * @return the port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @return the requesterId.
     */
    public long getRequesterId()
    {
        return requesterId;
    }

    /**
     * @param cacheNames The cacheNames to set.
     */
    public void setCacheNames( final ArrayList<String> cacheNames )
    {
        this.cacheNames = cacheNames;
    }

    /**
     * @param host The host to set.
     */
    public void setHost( final String host )
    {
        this.host = host;
    }

    /**
     * @param messageType The messageType to set.
     */
    public void setMessageType( final BroadcastType messageType )
    {
        this.messageType = messageType;
    }

    /**
     * @param port The port to set.
     */
    public void setPort( final int port )
    {
        this.port = port;
    }

    /**
     * @param requesterId The requesterId to set.
     */
    public void setRequesterId( final long requesterId )
    {
        this.requesterId = requesterId;
    }

    /**
     * @return debugging string
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n host = [" + host + "]" );
        buf.append( "\n port = [" + port + "]" );
        buf.append( "\n requesterId = [" + requesterId + "]" );
        buf.append( "\n messageType = [" + messageType + "]" );
        buf.append( "\n Cache Names" );
        for (final String name : cacheNames)
        {
            buf.append( " cacheName = [" + name + "]" );
        }
        return buf.toString();
    }
}
