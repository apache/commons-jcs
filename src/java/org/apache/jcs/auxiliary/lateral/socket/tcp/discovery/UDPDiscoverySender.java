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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;

/**
 * This is a generic sender for the UDPDiscovery process.
 *
 * @author Aaron Smuts
 *
 */
public class UDPDiscoverySender
{
    private final static Log log = LogFactory.getLog( UDPDiscoverySender.class );

    private MulticastSocket m_localSocket;

    private InetAddress m_multicastAddress;

    private int m_multicastPort;

    /**
     * Constructor for the UDPDiscoverySender object
     * <p>
     * This sender can be used to send multiple messages.
     * <p>
     * When you are done sending, you should destroy the socket sender.
     *
     * @param host
     * @param port
     *
     * @exception IOException
     */
    public UDPDiscoverySender( String host, int port )
        throws IOException
    {
        try
        {
            m_localSocket = new MulticastSocket();

            // Remote address.
            m_multicastAddress = InetAddress.getByName( host );
        }
        catch ( IOException e )
        {
            log.error( "Could not bind to multicast address [" + host + "]", e );

            throw e;
        }

        m_multicastPort = port;
    }

    /**
     * Closes the socket connection.
     *
     */
    public void destroy()
    {
        try
        {
            // TODO when we move to jdk 1.4 reinstate the isClosed check
            if ( this.m_localSocket != null )
            // && !this.m_localSocket.isClosed() )
            {
                this.m_localSocket.close();
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem destrying sender", e );
        }
    }

    /**
     * Just being careful about closing the socket.
     *
     * @throws Throwable
     */
    public void finalize()
        throws Throwable
    {
        super.finalize();
        destroy();
    }

    /**
     * Send messages.
     *
     * @param message
     * @throws IOException
     */
    public void send( UDPDiscoveryMessage message )
        throws IOException
    {
        if ( this.m_localSocket == null )
        {
            throw new IOException( "Socket is null, cannot send message." );
        }

        // TODO when we move to jdk 1.4 reinstate the isClosed check
        // if (this.m_localSocket.isClosed() )
        // {
        // throw new IOException( "Socket is closed, cannot send message." );
        // }

        if ( log.isDebugEnabled() )
        {
            log.debug( "sending UDPDiscoveryMessage, message = " + message );
        }

        try
        {
            // write the object to a byte array.
            final MyByteArrayOutputStream byteStream = new MyByteArrayOutputStream();
            final ObjectOutputStream objectStream = new ObjectOutputStream( byteStream );
            objectStream.writeObject( message );
            objectStream.flush();
            final byte[] bytes = byteStream.getBytes();

            // put the byte array in a packet
            final DatagramPacket packet = new DatagramPacket( bytes, bytes.length, m_multicastAddress, m_multicastPort );

            m_localSocket.send( packet );
        }
        catch ( IOException e )
        {
            log.error( "Error sending message", e );
            throw e;
        }
    }

    /**
     * Ask other to broadcast their info the the multicast address. If a lateral
     * is non receiving it can use this. This is also called on startup so we
     * can get info.
     *
     * @throws IOException
     */
    public void requestBroadcast()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "sending requestBroadcast " );
        }

        UDPDiscoveryMessage message = new UDPDiscoveryMessage();
        message.setRequesterId( LateralCacheInfo.listenerId );
        message.setMessageType( UDPDiscoveryMessage.REQUEST_BROADCAST );
        send( message );
    }

    /**
     * This sends a message braodcasting our that the host and port is available
     * for connections.
     * <p>
     * It uses the vmid as the requesterDI
     *
     * @param host
     * @param port
     * @param cacheNames
     * @throws IOException
     */
    public void passiveBroadcast( String host, int port, ArrayList cacheNames )
        throws IOException
    {
        passiveBroadcast( host, port, cacheNames, LateralCacheInfo.listenerId );
    }

    /**
     * This allows you to set the sender id. This is mainly for testing.
     *
     * @param host
     * @param port
     * @param cacheNames
     * @param listenerId
     * @throws IOException
     */
    protected void passiveBroadcast( String host, int port, ArrayList cacheNames, long listenerId )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "sending passiveBroadcast " );
        }

        UDPDiscoveryMessage message = new UDPDiscoveryMessage();
        message.setHost( host );
        message.setPort( port );
        message.setCacheNames( cacheNames );
        message.setRequesterId( listenerId );
        message.setMessageType( UDPDiscoveryMessage.PASSIVE_BROADCAST );
        send( message );
    }
}

/**
 * This allows us to get the byte array from an output stream.
 *
 * @author asmuts
 * @created January 15, 2002
 */

class MyByteArrayOutputStream
    extends ByteArrayOutputStream
{
    /**
     * Gets the bytes attribute of the MyByteArrayOutputStream object
     *
     * @return The bytes value
     */
    public byte[] getBytes()
    {
        return buf;
    }
}
// end class
