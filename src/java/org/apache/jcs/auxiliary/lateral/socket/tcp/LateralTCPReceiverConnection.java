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
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;

import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ILateralCacheTCPListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Separate thread run when a command comes into the LateralTCPReceiver.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @version $Id: LateralTCPReceiverConnection.java,v 1.7 2002/02/15 04:33:37
 *      jtaylor Exp $
 */
public class LateralTCPReceiverConnection implements Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralTCPReceiverConnection.class );

    private Socket socket;
    private ObjectInputStream ois;

    private ILateralCacheTCPListener ilcl;

    private int puts = 0;

    /**
     * Constructor for the LateralTCPReceiverConnection object
     *
     * @param socket
     * @param ilcl
     */
    public LateralTCPReceiverConnection( Socket socket,
                                         ILateralCacheTCPListener ilcl )
    {
        this.ilcl = ilcl;
        this.socket = socket;

        try
        {
            ois = new ObjectInputStream( socket.getInputStream() );
        }
        catch ( Exception e )
        {
            log.error( "Could not open ObjectInputStream to " + socket, e );
        }
    }


    /**
     * Main processing method for the LateralTCPReceiverConnection object
     */
    public void run()
    {
        Object obj;

        try
        {
            while ( true )
            {
                obj = ois.readObject();
                LateralElementDescriptor led = ( LateralElementDescriptor ) obj;
                if ( led == null )
                {
                    log.debug( "LateralElementDescriptor is null" );
                    continue;
                }
                if ( led.requesterId == LateralCacheInfo.listenerId )
                {
                    log.debug( "from self" );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "receiving LateralElementDescriptor from another, led = "
                             + ", led = " + led
                             + ", led.command = " + led.command
                             + ", led.ce = " + led.ce
                             + ", ilcl = " + ilcl );
                    }
                    if ( led.command == led.UPDATE )
                    {
                        puts++;
                        if ( log.isDebugEnabled() )
                        {
                            if ( puts % 100 == 0 )
                            {
                                log.debug( "puts = " + puts );
                            }
                        }
                        ilcl.handlePut( led.ce );
                    }
                    else
                        if ( led.command == led.REMOVE )
                    {
                        ilcl.handleRemove( led.ce.getCacheName(), led.ce.getKey() );
                    }
                    else
                        if ( led.command == led.GET )
                    {
                        getAndRespond( led.ce.getCacheName(), led.ce.getKey() );
                        //ilcl.handleGet( led.ce.getCacheName(), led.ce.getKey() );
                    }
                }
            }
        }
        catch ( java.io.EOFException e )
        {
            log.info( "Caught java.io.EOFException closing conneciton." );
        }
        catch ( java.net.SocketException e )
        {
            log.info( "Caught java.net.SocketException closing conneciton." );
        }
        catch ( Exception e )
        {
            log.error( "Unexpected exception. Closing conneciton", e );
        }

        try
        {
            ois.close();
        }
        catch ( Exception e )
        {
            log.error( "Could not close connection", e );
        }
    }


    /** Send back the object if found. */
    private Serializable getAndRespond( String cacheName, Serializable key )
        throws Exception
    {
        Serializable obj = ilcl.handleGet( cacheName, key );

        if ( log.isDebugEnabled() )
        {
            log.debug( "obj = " + obj );
        }

        ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );

        if ( oos != null )
        {
            try
            {
                oos.writeObject( obj );
                oos.flush();
            }
            catch ( IOException e )
            {
                oos = null;
                log.error( "Detected problem with connection", e );
                throw e;
            }
        }
        return obj;
    }
}
