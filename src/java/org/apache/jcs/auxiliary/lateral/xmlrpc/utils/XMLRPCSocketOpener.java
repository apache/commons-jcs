package org.apache.jcs.auxiliary.lateral.xmlrpc.utils;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.IOException;
import java.io.InterruptedIOException;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id$
 */
public class XMLRPCSocketOpener implements Runnable
{

    private final static Log log =
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
