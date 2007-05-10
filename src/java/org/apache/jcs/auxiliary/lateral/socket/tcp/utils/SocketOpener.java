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
import java.net.Socket;

/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @version $Id$
 */
public class SocketOpener
    implements Runnable
{

    private String host;

    private int port;

    private Socket socket;

    /**
     * Opens a socket with a connection timeout value. Joins against a backgroud
     * thread that does the openeing.
     *
     * @param host
     * @param port
     * @param timeOut
     * @return Socket
     */
    public static Socket openSocket( String host, int port, int timeOut )
    {
        SocketOpener opener = new SocketOpener( host, port );
        Thread t = new Thread( opener );
        t.start();
        try
        {
            t.join( timeOut );
        }
        catch ( InterruptedException ire )
        {
            // swallow
        }
        return opener.getSocket();
    }

    /**
     * Constructor for the SocketOpener object
     *
     * @param host
     * @param port
     */
    public SocketOpener( String host, int port )
    {
        this.socket = null;
        this.host = host;
        this.port = port;
    }

    /** Main processing method for the SocketOpener object */
    public void run()
    {
        try
        {
            socket = new Socket( host, port );
        }
        catch ( IOException ioe )
        {
            // swallow
        }
    }

    /**
     *
     * @return The opened socket
     */
    public Socket getSocket()
    {
        return socket;
    }
}
