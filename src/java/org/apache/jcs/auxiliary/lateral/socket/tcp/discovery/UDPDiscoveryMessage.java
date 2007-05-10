package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Aaron Smuts
 *
 */
public class UDPDiscoveryMessage
    implements Serializable
{

    private static final long serialVersionUID = -5332377899560951794L;

    /**
     * This is the periodic broadcast of a servers location. This type of
     * message is also sent in response to a REQUEST_BROADCAST.
     */
    public static final int PASSIVE_BROADCAST = 0;

    /**
     * This asks recipients to broadcast their location. This is used on
     * startup.
     */
    public static final int REQUEST_BROADCAST = 1;

    private int messageType = PASSIVE_BROADCAST;

    private int port = 6789;

    private String host = "228.5.6.7";

    /** Description of the Field */
    private long requesterId;

    private ArrayList cacheNames = new ArrayList();

    /**
     * @param port
     *            The port to set.
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    /**
     * @return Returns the port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param host
     *            The host to set.
     */
    public void setHost( String host )
    {
        this.host = host;
    }

    /**
     * @return Returns the host.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @param requesterId
     *            The requesterId to set.
     */
    public void setRequesterId( long requesterId )
    {
        this.requesterId = requesterId;
    }

    /**
     * @return Returns the requesterId.
     */
    public long getRequesterId()
    {
        return requesterId;
    }

    /**
     * @param messageType
     *            The messageType to set.
     */
    public void setMessageType( int messageType )
    {
        this.messageType = messageType;
    }

    /**
     * @return Returns the messageType.
     */
    public int getMessageType()
    {
        return messageType;
    }

    /**
     * @param cacheNames
     *            The cacheNames to set.
     */
    public void setCacheNames( ArrayList cacheNames )
    {
        this.cacheNames = cacheNames;
    }

    /**
     * @return Returns the cacheNames.
     */
    public ArrayList getCacheNames()
    {
        return cacheNames;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n host = [" + host + "]" );
        buf.append( "\n port = [" + port + "]" );
        buf.append( "\n requesterId = [" + requesterId + "]" );
        buf.append( "\n messageType = [" + messageType + "]" );

        buf.append( "\n Cache Names" );
        Iterator it = cacheNames.iterator();
        while ( it.hasNext() )
        {
            String name = (String) it.next();
            buf.append( " cacheName = [" + name + "]" );
        }
        return buf.toString();
    }

}
