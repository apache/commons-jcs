package org.apache.jcs.auxiliary.lateral.socket.tcp.utils;

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
import java.net.Socket;

/**
 * Since 1.4, we can specify the timeout in the connect block, we no longer need the extra thread to
 * join against the Socket creation.
 */
public class SocketOpener
    implements Runnable
{
    /** The host */
    private String host;

    /** The port. */
    private int port;

    /** the open timeOut */
    private int timeOut;

    /** The socket */
    private Socket socket;

    /**
     * Opens a socket with a connection timeout value.
     * <p>
     * @param host
     * @param port
     * @param timeOut
     * @return Socket
     */
    public static Socket openSocket( String host, int port, int timeOut )
    {
        // TODO get rid of the extra object
        SocketOpener opener = new SocketOpener( host, port, timeOut );
        opener.connect();
        return opener.getSocket();
    }

    /**
     * Constructor for the SocketOpener object
     * @param host
     * @param port
     * @param timeout connect timeout
     */
    public SocketOpener( String host, int port, int timeout )
    {
        this.socket = null;
        this.host = host;
        this.port = port;
        this.timeOut = timeout;
    }

    /** Main processing method for the SocketOpener object */
    public void run()
    {
        connect();
    }

    /**
     * Creates an InetSocketAddress. Creates an unconnected Socket. Connects the Socket to the
     * address.
     */
    private void connect()
    {
        try
        {
            InetSocketAddress address = new InetSocketAddress( host, port );
            socket = new Socket();
            socket.connect( address, timeOut );
        }
        catch ( IOException ioe )
        {
            // swallow
        }
    }

    /**
     * @return The opened socket
     */
    public Socket getSocket()
    {
        return socket;
    }
}
