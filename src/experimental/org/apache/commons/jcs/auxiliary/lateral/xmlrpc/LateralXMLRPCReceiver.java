package org.apache.commons.jcs.auxiliary.lateral.xmlrpc;

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

import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.xmlrpc.behavior.ILateralCacheXMLRPCListener;
import org.apache.commons.jcs.auxiliary.lateral.xmlrpc.behavior.IXMLRPCConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.WebServer;

/**
 * Processes commands from the server socket.
 *
 * @version $Id: LateralXMLRPCReceiver.java,v 1.8 2002/02/16 02:37:19 jtaylor
 *      Exp $
 */
public class LateralXMLRPCReceiver implements IXMLRPCConstants
{
    private static final Log log =
        LogFactory.getLog( LateralXMLRPCReceiver.class );

    private int port;

    private ILateralCacheXMLRPCListener ilcl;

    /**
     * How long the server will block on an accept(). 0 is infinte.
     */
    private static final int sTimeOut = 0;


    /**
     * Main processing method for the LateralXMLRPCReceiver object
     */
    public void init()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Listening on port " + port );
            }
            log.info( "Listening on port " + port );

//            ServerSocket serverSocket = new ServerSocket( port );
//            serverSocket.setSoTimeout( this.sTimeOut );
//            while ( true )
//            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Waiting for clients to connect " );
                }
                log.info( "Waiting for clients to client " );

                LateralXMLRPCReceiverConnection handler = new LateralXMLRPCReceiverConnection( ilcl );
                try
                {
                    WebServer server = new WebServer( port );
                    server.addHandler( this.HANDLERNAME, handler );
                    server.setParanoid( false );
                }
                catch ( Exception ioe )
                {
                    log.error( ioe );
                }

//                Socket socket = serverSocket.accept();
//                InetAddress inetAddress = socket.getInetAddress();
//                if ( log.isDebugEnabled() )
//                {
//                    log.debug( "Connected to client at " + inetAddress );
//                }
//                log.info( "Connected to client at " + inetAddress );
//                log.info( "Starting new socket node." );
//                new Thread( new LateralXMLRPCReceiverConnection( socket, ilcl ) ).start();
//            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    /**
     * Constructor for the LateralXMLRPCReceiver object
     *
     * @param lca
     * @param ilcl
     */
    public LateralXMLRPCReceiver( ILateralCacheAttributes lca, ILateralCacheXMLRPCListener ilcl )
    {
        this.port = lca.getTcpListenerPort();
        this.ilcl = ilcl;
        if ( log.isDebugEnabled() )
        {
            log.debug( "ilcl = " + ilcl );
        }
        init();
    }
}
