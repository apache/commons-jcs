package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * This class is based on the log4j SocketAppender class. I'm using a different repair structure, so
 * it is significantly different.
 */
public class LateralTCPSender
{
    /** The logger */
    private static final Log log = LogManager.getLog( LateralTCPSender.class );

    /** Config */
    private final int socketOpenTimeOut;
    private final int socketSoTimeOut;

    /** The serializer. */
    private final IElementSerializer serializer;

    /** The client connection with the server. */
    private AsynchronousSocketChannel client;

    /** how many messages sent */
    private int sendCnt;

    /** Use to synchronize multiple threads that may be trying to get. */
    private final Lock lock = new ReentrantLock(true);

    /**
     * Constructor for the LateralTCPSender object.
     * <p>
     * @param lca
     * @throws IOException
     * @deprecated Specify serializer
     */
    @Deprecated
    public LateralTCPSender( final ITCPLateralCacheAttributes lca )
        throws IOException
    {
        this(lca, new StandardSerializer());
    }

    /**
     * Constructor for the LateralTCPSender object.
     * <p>
     * @param lca the configuration object
     * @param serializer the serializer to use when sending
     * @throws IOException
     * @since 3.1
     */
    public LateralTCPSender( final ITCPLateralCacheAttributes lca, final IElementSerializer serializer )
        throws IOException
    {
        this.socketOpenTimeOut = lca.getOpenTimeOut();
        this.socketSoTimeOut = lca.getSocketTimeOut();

        this.serializer = serializer;

        final String p1 = lca.getTcpServer();
        if ( p1 == null )
        {
            throw new IOException( "Invalid server (null)" );
        }

        final int colonPosition = p1.lastIndexOf(':');

        if ( colonPosition < 0 )
        {
            throw new IOException( "Invalid address [" + p1 + "]" );
        }

        final String h2 = p1.substring( 0, colonPosition );
        final int po = Integer.parseInt( p1.substring( colonPosition + 1 ) );
        log.debug( "h2 = {0}, po = {1}", h2, po );

        if ( h2.isEmpty() )
        {
            throw new IOException( "Cannot connect to invalid address [" + h2 + ":" + po + "]" );
        }

        init( h2, po );
    }

    /**
     * Creates a connection to a TCP server.
     * <p>
     * @param host
     * @param port
     * @throws IOException
     */
    protected void init( final String host, final int port )
        throws IOException
    {
        log.info( "Attempting connection to [{0}:{1}]", host, port );

        try
        {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(host, port);
            Future<Void> future = client.connect(hostAddress);

            future.get(this.socketOpenTimeOut, TimeUnit.MILLISECONDS);
        }
        catch (final IOException | InterruptedException | ExecutionException | TimeoutException ioe)
        {
            throw new IOException( "Cannot connect to " + host + ":" + port, ioe );
        }
    }

    /**
     * Sends commands to the lateral cache listener.
     * <p>
     * @param led
     * @throws IOException
     */
    public <K, V> void send( final LateralElementDescriptor<K, V> led )
        throws IOException
    {
        sendCnt++;
        if ( log.isInfoEnabled() && sendCnt % 100 == 0 )
        {
            log.info( "Send Count {0} = {1}", client.getRemoteAddress(), sendCnt );
        }

        log.debug( "sending LateralElementDescriptor" );

        if ( led == null )
        {
            return;
        }

        lock.lock();
        try
        {
            serializer.serializeTo(led, client, socketSoTimeOut);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Sends commands to the lateral cache listener and gets a response. I'm afraid that we could
     * get into a pretty bad blocking situation here. This needs work. I just wanted to get some
     * form of get working. However, get is not recommended for performance reasons. If you have 10
     * laterals, then you have to make 10 failed gets to find out none of the caches have the item.
     * <p>
     * @param led
     * @return ICacheElement
     * @throws IOException
     */
    public <K, V> Object sendAndReceive( final LateralElementDescriptor<K, V> led )
        throws IOException
    {
        if ( led == null )
        {
            return null;
        }

        // Synchronized to insure that the get requests to server from this
        // sender and the responses are processed in order, else you could
        // return the wrong item from the cache.
        // This is a big block of code. May need to re-think this strategy.
        // This may not be necessary.
        // Normal puts, etc to laterals do not have to be synchronized.
        Object response = null;

        lock.lock();
        try
        {
            // write object to listener
            send(led);
            response = serializer.deSerializeFrom(client, socketSoTimeOut, null);
        }
        catch ( final IOException | ClassNotFoundException ioe )
        {
            final String message = "Could not open channel to " +
                client.getRemoteAddress() + " SoTimeout [" + socketSoTimeOut +
                "] Connected [" + client.isOpen() + "]";
            log.error( message, ioe );
            throw new IOException(message, ioe);
        }
        finally
        {
            lock.unlock();
        }

        return response;
    }

    /**
     * Closes connection used by all LateralTCPSenders for this lateral connection. Dispose request
     * should come into the facade and be sent to all lateral cache services. The lateral cache
     * service will then call this method.
     * <p>
     * @throws IOException
     */
    public void dispose()
        throws IOException
    {
        log.info( "Dispose called" );
        client.close();
    }
}
