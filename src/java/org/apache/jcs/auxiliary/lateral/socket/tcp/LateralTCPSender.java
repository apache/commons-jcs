package org.apache.jcs.auxiliary.lateral.socket.tcp;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.utils.SocketOpener;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This class is based on the log4j SocketAppender class. I'm using a differnet repair structure, so
 * it is significantly different.
 */
public class LateralTCPSender
{
    private final static Log log = LogFactory.getLog( LateralTCPSender.class );

    private ITCPLateralCacheAttributes tcpLateralCacheAttributes;

    private String remoteHost;

    private InetAddress address;

    int port = 1111;

    private ObjectOutputStream oos;

    private Socket socket;

    int counter = 0;

    private int sendCnt = 0;

    // reset the ObjectOutputStream every 70 calls
    // private static final int RESET_FREQUENCY = 70;
    // Perhaps we need to resett every time until we move to jdk 1.4
    // then we can call writeUnshared to make sure
    // that the object definetely gets across and not
    // a stream cached version.
    // I can't replicate an issue that was reported, so I'm not changing the
    // reset frequency for now.
    private final static int RESET_FREQUENCY = 70;

    /**
     * Only block for 1 second before timing out on a read. TODO: make configurable. The default 1
     * is way too long.
     */
    private final static int timeOut = 1000;

    /** Only block for 5 seconds before timing out on startup. */
    private final static int openTimeOut = 5000;

    /** Use to synchronize multiple threads that may be trying to get. */
    private Object getLock = new int[0];

    /**
     * Constructor for the LateralTCPSender object.
     * <p>
     * @param lca
     * @exception IOException
     */
    public LateralTCPSender( ITCPLateralCacheAttributes lca )
        throws IOException
    {
        this.setTcpLateralCacheAttributes( lca );

        String p1 = lca.getTcpServer();
        if ( p1 != null )
        {
            String h2 = p1.substring( 0, p1.indexOf( ":" ) );
            int po = Integer.parseInt( p1.substring( p1.indexOf( ":" ) + 1 ) );
            if ( log.isDebugEnabled() )
            {
                log.debug( "h2 = " + h2 );
                log.debug( "po = " + po );
            }

            if ( h2 == null )
            {
                throw new IOException( "Cannot connect to invalid address [" + h2 + ":" + po + "]" );
            }

            init( h2, po );
        }
    }

    /**
     * Creates a connection to a TCP server.
     * <p>
     * @param host
     * @param port
     * @throws IOException
     */
    protected void init( String host, int port )
        throws IOException
    {
        this.port = port;
        this.address = getAddressByName( host );
        this.setRemoteHost( host );

        try
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Attempting connection to [" + address.getHostName() + "]" );
            }

            // have time out socket open do this for us
            socket = SocketOpener.openSocket( host, port, openTimeOut );

            if ( socket == null )
            {
                throw new IOException( "Socket is null, cannot connect to " + host + ":" + port );
            }

            socket.setSoTimeout( LateralTCPSender.timeOut );
            synchronized ( this )
            {
                oos = new ObjectOutputStream( socket.getOutputStream() );
            }
        }
        catch ( java.net.ConnectException e )
        {
            log.debug( "Remote host [" + address.getHostName() + "] refused connection." );
            throw e;
        }
        catch ( IOException e )
        {
            log.debug( "Could not connect to [" + address.getHostName() + "]. Exception is " + e );
            throw e;
        }
    }

    /**
     * Gets the addressByName attribute of the LateralTCPSender object.
     * <p>
     * @param host
     * @return The addressByName value
     * @throws IOException
     */
    private InetAddress getAddressByName( String host )
        throws IOException
    {
        try
        {
            return InetAddress.getByName( host );
        }
        catch ( Exception e )
        {
            log.error( "Could not find address of [" + host + "] ", e );
            throw new IOException( "Could not find address of [" + host + "] " + e.getMessage() );
        }
    }

    /**
     * Sends commands to the lateral cache listener.
     * <p>
     * @param led
     * @throws IOException
     */
    public void send( LateralElementDescriptor led )
        throws IOException
    {
        sendCnt++;
        if ( log.isInfoEnabled() )
        {
            if ( sendCnt % 100 == 0 )
            {
                log.info( "Send Count (port " + port + ") = " + sendCnt );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "sending LateralElementDescriptor" );
        }

        if ( led == null )
        {
            return;
        }

        if ( address == null )
        {
            throw new IOException( "No remote host is set for LateralTCPSender." );
        }

        if ( oos != null )
        {
            synchronized ( this.getLock )
            {
                try
                {
                    oos.writeObject( led );
                    oos.flush();
                    if ( ++counter >= RESET_FREQUENCY )
                    {
                        counter = 0;
                        // Failing to reset the object output stream every now and
                        // then creates a serious memory leak.
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Doing oos.reset()" );
                        }
                        oos.reset();
                    }
                }
                catch ( IOException e )
                {
                    oos = null;
                    log.error( "Detected problem with connection: " + e );
                    throw e;
                }
            }
        }
    }

    /**
     * Sends commands to the lateral cache listener and gets a response. I'm afraid that we could
     * get into a pretty bad blocking situation here. This needs work. I just wanted to get some
     * form of get working. However, get is not recommended for performance reasons. If you have 10
     * laterals, then you have to make 10 failed gets to find out none of the caches have the item.
     * <p>
     * @param led
     * @return
     * @throws IOException
     */
    public ICacheElement sendAndReceive( LateralElementDescriptor led )
        throws IOException
    {
        ICacheElement ice = null;

        if ( led == null )
        {
            return null;
        }

        if ( address == null )
        {
            throw new IOException( "No remote host is set for LateralTCPSender." );
        }

        if ( oos != null )
        {
            // Synchronized to insure that the get requests to server from this
            // sender and the responses are processed in order, else you could
            // return the wrong item from the cache.
            // This is a big block of code. May need to rethink this strategy.
            // This may not be necessary.
            // Normal puts, etc to laterals do not have to be synchronized.
            synchronized ( this.getLock )
            {
                try
                {
                    try
                    {
                        // clean up input stream, nothing should be there yet.
                        if ( socket.getInputStream().available() > 0 )
                        {
                            socket.getInputStream().read( new byte[socket.getInputStream().available()] );
                        }
                    }
                    catch ( IOException ioe )
                    {
                        log.error( "Problem cleaning socket before send " + socket, ioe );
                        throw ioe;
                    }

                    // write object to listener
                    oos.writeObject( led );
                    oos.flush();

                    try
                    {
                        // TODO make configurable
                        // socket.setSoTimeout( 2000 );
                        ObjectInputStream ois = new ObjectInputStream( socket.getInputStream() );
                        Object obj = ois.readObject();
                        ice = (ICacheElement) obj;
                        if ( ice == null )
                        {
                            // p( "ice is null" );
                            // TODO: count misses
                        }
                    }
                    catch ( IOException ioe )
                    {
                        String message = "Could not open ObjectInputStream to " + socket;
                        if ( socket != null )
                        {
                            message += " SoTimeout [" + socket.getSoTimeout() + "]";
                            // this is 1.4 specific -- Connected [" + socket.isConnected() + "]";
                        }
                        log.error( message, ioe );
                        throw ioe;
                    }
                    catch ( Exception e )
                    {
                        log.error( e );
                    }

                    if ( ++counter >= RESET_FREQUENCY )
                    {
                        counter = 0;
                        // Failing to reset the object output stream every now
                        // and
                        // then creates a serious memory leak.
                        log.info( "Doing oos.reset()" );
                        oos.reset();
                    }
                }
                catch ( IOException e )
                {
                    oos = null;
                    log.error( "Detected problem with connection: " + e );
                    throw e;
                }
            }
        }

        return ice;
    }

    /**
     * Closes connection used by all LateralTCPSenders for this lateral conneciton. Dispose request
     * should come into the facade and be sent to all lateral cache sevices. The lateral cache
     * service will then call this method.
     * <p>
     * @param cache
     * @throws IOException
     */
    public void dispose( String cache )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Dispose called for cache [" + cache + "]" );
        }
        // WILL CLOSE CONNECTION USED BY ALL
        oos.close();
    }

    /**
     * @param tcpLateralCacheAttributes The tcpLateralCacheAttributes to set.
     */
    public void setTcpLateralCacheAttributes( ITCPLateralCacheAttributes tcpLateralCacheAttributes )
    {
        this.tcpLateralCacheAttributes = tcpLateralCacheAttributes;
    }

    /**
     * @return Returns the tcpLateralCacheAttributes.
     */
    public ITCPLateralCacheAttributes getTcpLateralCacheAttributes()
    {
        return tcpLateralCacheAttributes;
    }

    /**
     * @param remoteHost The remoteHost to set.
     */
    public void setRemoteHost( String remoteHost )
    {
        this.remoteHost = remoteHost;
    }

    /**
     * @return Returns the remoteHost.
     */
    public String getRemoteHost()
    {
        return remoteHost;
    }

    /**
     * This is a Testing Method. It should be moved to a unit test.
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            LateralTCPSender lur = null;
            // new LateralTCPSender( "localhost", 1111 );

            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

            while ( notDone )
            {
                System.out.println( "enter mesage:" );
                message = br.readLine();
                CacheElement ce = new CacheElement( "test", "test", message );
                LateralElementDescriptor led = new LateralElementDescriptor( ce );
                lur.send( led );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
    }
}
