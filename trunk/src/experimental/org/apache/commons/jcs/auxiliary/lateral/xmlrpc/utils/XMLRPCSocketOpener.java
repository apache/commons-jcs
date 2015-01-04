package org.apache.commons.jcs.auxiliary.lateral.xmlrpc.utils;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClientLite;

import java.io.IOException;


/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @version $Id$
 */
public class XMLRPCSocketOpener implements Runnable
{

    private static final Log log =
        LogFactory.getLog( XMLRPCSocketOpener.class );


    private String host;
    private int port;
    //private Socket socket;
    private XmlRpcClientLite xmlrpc;

    /** Constructor for the SocketOpener object */
    public static XmlRpcClientLite openSocket( String host, int port, int timeOut )
    {
        XMLRPCSocketOpener opener = new XMLRPCSocketOpener( host, port );
        Thread t = new Thread( opener );
        t.start();
        try
        {
            t.join( timeOut );
        }
        catch ( InterruptedException ire )
        {
            log.error( "Trouble opening socket", ire);
        }
        return opener.getSocket();
    }


    /**
     * Constructor for the SocketOpener object
     *
     * @param host
     * @param port
     */
    public XMLRPCSocketOpener( String host, int port )
    {
        this.xmlrpc = null;
        this.host = host;
        this.port = port;
    }


    /** Main processing method for the SocketOpener object */
    public void run()
    {
        try
        {
            //socket = new Socket( host, port );
            xmlrpc = new XmlRpcClientLite ( "http://" + host + ":" + port + "/RPC2" );
        }
        catch ( IOException ioe )
        {
            log.error( "Trouble creating client", ioe);
        }
    }

    /** Gets the socket attribute of the SocketOpener object */
    public XmlRpcClientLite getSocket()
    {
        return xmlrpc;
    }
}
