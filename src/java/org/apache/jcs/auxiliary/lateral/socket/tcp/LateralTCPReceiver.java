package org.apache.jcs.auxiliary.lateral.socket.tcp;

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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ILateralCacheTCPListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes commands from the server socket.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id$
 */
public class LateralTCPReceiver implements Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralTCPReceiver.class );

    private int port;

    private ILateralCacheTCPListener ilcl;

    /** How long the server will block on an accept(). 0 is infinte. */
    private final static int sTimeOut = 0;


    /** Main processing method for the LateralTCPReceiver object */
    public void run()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Listening on port " + port );
            }
            log.info( "Listening on port " + port );
            ServerSocket serverSocket = new ServerSocket( port );
            serverSocket.setSoTimeout( this.sTimeOut );
            while ( true )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Waiting for clients to connect " );
                }
                log.info( "Waiting for clients to client " );
                Socket socket = serverSocket.accept();
                InetAddress inetAddress = socket.getInetAddress();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Connected to client at " + inetAddress );
                }
                log.info( "Connected to client at " + inetAddress );
                log.info( "Starting new socket node." );
                new Thread( new LateralTCPReceiverConnection( socket, ilcl ) ).start();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    /**
     * Constructor for the LateralTCPReceiver object
     *
     * @param lca
     * @param ilcl
     */
    public LateralTCPReceiver( ILateralCacheAttributes lca, ILateralCacheTCPListener ilcl )
    {
        this.port = lca.getTcpListenerPort();
        this.ilcl = ilcl;
        if ( log.isDebugEnabled() )
        {
            log.debug( "ilcl = " + ilcl );
        }
    }
}
