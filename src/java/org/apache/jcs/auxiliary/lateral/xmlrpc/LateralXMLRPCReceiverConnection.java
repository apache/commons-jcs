package org.apache.jcs.auxiliary.lateral.xmlrpc;

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
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Vector;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;

import org.apache.jcs.auxiliary.lateral.xmlrpc.behavior.ILateralCacheXMLRPCListener;
import org.apache.jcs.auxiliary.lateral.xmlrpc.behavior.IXMLRPCConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlrpc.XmlRpcHandler;

/**
 * Separate thread run when a command comes into the LateralXMLRPCReceiver.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @version $Id: LateralXMLRPCReceiverConnection.java,v 1.7 2002/02/15 04:33:37
 *      jtaylor Exp $
 */
public class LateralXMLRPCReceiverConnection implements XmlRpcHandler, IXMLRPCConstants
{//implements Runnable

    private final static Log log =
        LogFactory.getLog( LateralXMLRPCReceiverConnection.class );

    private ILateralCacheXMLRPCListener ilcl;

    private int puts = 0;

     /**
     * Constructor for the LateralXMLRPCReceiverConnection object
     *
     * @param ilcl
     */
    public LateralXMLRPCReceiverConnection( ILateralCacheXMLRPCListener ilcl )
    {
        this.ilcl = ilcl;

    }

    /**
     * Description of the Method
     *
     * @return
     * @param method
     * @param params
     */
    public Object execute( String method, Vector params )
    {
        // do nothing with method name for now, later the action code can be
        // the method name
        LateralElementDescriptor led = null;
        try
        {
            // get the LED out of the params
            byte[] data = ( byte[] ) params.firstElement();
            ByteArrayInputStream bais = new ByteArrayInputStream( data );
            BufferedInputStream bis = new BufferedInputStream( bais );
            ObjectInputStream ois = new ObjectInputStream( bis );
            try
            {
                led = ( LateralElementDescriptor ) ois.readObject();
            }
            finally
            {
                ois.close();
            }
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        // this should start a thread fo non gets?
        return executeImpl( led );
    }


    /**
     * Main processing method for the LateralXMLRPCReceiverConnection object
     *
     * @return
     * @param led
     */
    public Serializable executeImpl( LateralElementDescriptor led )
    {
        Serializable obj = "junk return value";

        try
        {
            if ( led == null )
            {
                log.debug( "LateralElementDescriptor is null" );
                //continue;
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
                    obj = getAndRespond( led.ce.getCacheName(), led.ce.getKey() );
                    //ilcl.handleGet( led.ce.getCacheName(), led.ce.getKey() );
                }
            }
//            }
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

        return obj;
    }


    /**
     * Send back the object if found.
     *
     * @return The {3} value
     * @param cacheName
     * @param key
     * @exception Exception
     */
    private Serializable getAndRespond( String cacheName, Serializable key )
        throws Exception
    {
        Serializable obj = ilcl.handleGet( cacheName, key );

        if ( log.isDebugEnabled() )
        {
            log.debug( "obj = " + obj );
        }

        if ( obj == null )
        {
            obj = IXMLRPCConstants.NO_RESULTS;
        }
        return obj;
    }
}
