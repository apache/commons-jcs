package org.apache.jcs.auxiliary.lateral.xmlrpc;

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
