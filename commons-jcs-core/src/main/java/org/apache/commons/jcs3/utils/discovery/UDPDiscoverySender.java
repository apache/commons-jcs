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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;

import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryMessage.BroadcastType;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * This is a generic sender for the UDPDiscovery process.
 * <p>
 * @author Aaron Smuts
 */
public class UDPDiscoverySender implements AutoCloseable
{
    /** The logger. */
    private static final Log log = LogManager.getLog( UDPDiscoverySender.class );

    /** The socket */
    private final MulticastSocket localSocket;

    /** The address */
    private final InetAddress multicastAddress;

    /** The port */
    private final int multicastPort;

    /** Used to serialize messages */
    private final IElementSerializer serializer;

    /**
     * Constructor for the UDPDiscoverySender object
     * <p>
     * This sender can be used to send multiple messages.
     * <p>
     * When you are done sending, you should destroy the socket sender.
     * <p>
     * @param host
     * @param port
     * @param udpTTL the Datagram packet time-to-live
     * @throws IOException
     * @deprecated Specify serializer implementation explicitly
     */
    @Deprecated
    public UDPDiscoverySender( final String host, final int port, final int udpTTL )
        throws IOException
    {
        this(null, host, port, udpTTL, new StandardSerializer());
    }

    /**
     * Constructor for the UDPDiscoverySender object
     * <p>
     * This sender can be used to send multiple messages.
     * <p>
     * When you are done sending, you should destroy the socket sender.
     * <p>
     * @param udpDiscoveryAttributes configuration object
     * @param serializer the Serializer to use when sending messages
     * @throws IOException
     * @since 3.1
     */
    public UDPDiscoverySender(final UDPDiscoveryAttributes udpDiscoveryAttributes, final IElementSerializer serializer)
        throws IOException
    {
        this(udpDiscoveryAttributes.getUdpDiscoveryInterface(),
            udpDiscoveryAttributes.getUdpDiscoveryAddr(),
            udpDiscoveryAttributes.getUdpDiscoveryPort(),
            udpDiscoveryAttributes.getUdpTTL(),
            serializer);
    }

    /**
     * Constructor for the UDPDiscoverySender object
     * <p>
     * This sender can be used to send multiple messages.
     * <p>
     * When you are done sending, you should destroy the socket sender.
     * <p>
     * @param mcastInterface the Multicast interface name to use, if null, try to autodetect
     * @param host
     * @param port
     * @param udpTTL the Datagram packet time-to-live
     * @param serializer the Serializer to use when sending messages
     * @throws IOException
     * @since 3.1
     */
    public UDPDiscoverySender(final String mcastInterface, final String host,
            final int port, final int udpTTL, IElementSerializer serializer)
        throws IOException
    {
        try
        {
            log.debug( "Constructing socket for sender on port [{0}]", port );
            localSocket = new MulticastSocket( port );

            if (udpTTL > 0)
            {
                log.debug( "Setting datagram TTL to [{0}]", udpTTL );
                localSocket.setTimeToLive(udpTTL);
            }

            // Remote address.
            multicastAddress = InetAddress.getByName( host );

            // Use dedicated interface if specified
            NetworkInterface multicastInterface = null;
            if (mcastInterface != null)
            {
                multicastInterface = NetworkInterface.getByName(mcastInterface);
            }
            else
            {
                multicastInterface = HostNameUtil.getMulticastNetworkInterface();
            }
            if (multicastInterface != null)
            {
                log.info("Sending multicast via network interface {0}",
                        multicastInterface::getDisplayName);
                localSocket.setNetworkInterface(multicastInterface);
            }
        }
        catch ( final IOException e )
        {
            log.error( "Could not bind to multicast address [{0}]", host, e );
            throw e;
        }

        this.multicastPort = port;
        this.serializer = serializer;
    }

    /**
     * Closes the socket connection.
     */
    @Override
    public void close()
    {
        if ( this.localSocket != null && !this.localSocket.isClosed() )
        {
            this.localSocket.close();
        }
    }

    /**
     * Send messages.
     * <p>
     * @param message
     * @throws IOException
     */
    public void send( final UDPDiscoveryMessage message )
        throws IOException
    {
        if ( this.localSocket == null )
        {
            throw new IOException( "Socket is null, cannot send message." );
        }

        if ( this.localSocket.isClosed() )
        {
            throw new IOException( "Socket is closed, cannot send message." );
        }

        log.debug( "sending UDPDiscoveryMessage, address [{0}], port [{1}], "
                + "message = {2}", multicastAddress, multicastPort, message );

        final byte[] bytes = serializer.serialize( message );

        // put the byte array in a packet
        final DatagramPacket packet = new DatagramPacket( bytes, bytes.length, multicastAddress, multicastPort );

        log.debug( "Sending DatagramPacket with {0} bytes to {1}:{2}",
                bytes.length, multicastAddress, multicastPort );

        localSocket.send( packet );
    }

    /**
     * Ask other to broadcast their info the the multicast address. If a lateral is non receiving it
     * can use this. This is also called on startup so we can get info.
     * <p>
     * @throws IOException
     */
    public void requestBroadcast()
        throws IOException
    {
        log.debug( "sending requestBroadcast" );

        final UDPDiscoveryMessage message = new UDPDiscoveryMessage();
        message.setHost(multicastAddress.getHostAddress());
        message.setPort(multicastPort);
        message.setRequesterId( CacheInfo.listenerId );
        message.setMessageType( BroadcastType.REQUEST );
        send( message );
    }

    /**
     * This sends a message broadcasting out that the host and port is available for connections.
     * <p>
     * It uses the vmid as the requesterDI
     * @param host
     * @param port
     * @param cacheNames
     * @throws IOException
     */
    public void passiveBroadcast( final String host, final int port, final ArrayList<String> cacheNames )
        throws IOException
    {
        passiveBroadcast( host, port, cacheNames, CacheInfo.listenerId );
    }

    /**
     * This allows you to set the sender id. This is mainly for testing.
     * <p>
     * @param host
     * @param port
     * @param cacheNames names of the cache regions
     * @param listenerId
     * @throws IOException
     */
    protected void passiveBroadcast( final String host, final int port, final ArrayList<String> cacheNames, final long listenerId )
        throws IOException
    {
        log.debug( "sending passiveBroadcast" );

        final UDPDiscoveryMessage message = new UDPDiscoveryMessage();
        message.setHost( host );
        message.setPort( port );
        message.setCacheNames( cacheNames );
        message.setRequesterId( listenerId );
        message.setMessageType( BroadcastType.PASSIVE );
        send( message );
    }

    /**
     * This sends a message broadcasting our that the host and port is no longer available.
     * <p>
     * It uses the vmid as the requesterID
     * <p>
     * @param host host
     * @param port port
     * @param cacheNames names of the cache regions
     * @throws IOException on error
     */
    public void removeBroadcast( final String host, final int port, final ArrayList<String> cacheNames )
        throws IOException
    {
        removeBroadcast( host, port, cacheNames, CacheInfo.listenerId );
    }

    /**
     * This allows you to set the sender id. This is mainly for testing.
     * <p>
     * @param host host
     * @param port port
     * @param cacheNames names of the cache regions
     * @param listenerId listener ID
     * @throws IOException on error
     */
    protected void removeBroadcast( final String host, final int port, final ArrayList<String> cacheNames, final long listenerId )
        throws IOException
    {
        log.debug( "sending removeBroadcast" );

        final UDPDiscoveryMessage message = new UDPDiscoveryMessage();
        message.setHost( host );
        message.setPort( port );
        message.setCacheNames( cacheNames );
        message.setRequesterId( listenerId );
        message.setMessageType( BroadcastType.REMOVE );
        send( message );
    }
}

/**
 * This allows us to get the byte array from an output stream.
 * <p>
 * @author asmuts
 * @created January 15, 2002
 * @deprecated No longer used
 */
@Deprecated
class MyByteArrayOutputStream
    extends ByteArrayOutputStream
{
    /**
     * Gets the bytes attribute of the MyByteArrayOutputStream object
     * @return The bytes value
     */
    public byte[] getBytes()
    {
        return buf;
    }
}
