package org.apache.jcs.auxiliary.lateral.javagroups;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Vector;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;

import org.apache.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.javagroups.Channel;
import org.javagroups.JChannel;
import org.javagroups.Message;

/**
 * Separate thread run when a command comes into the LateralJGReceiver.
 *
 * @version $Id: LateralJGReceiverConnection.java,v 1.7 2002/02/15 04:33:37
 *      jtaylor Exp $
 */
public class LateralJGReceiverConnection implements Runnable
{

    private final static Log log =
        LogFactory.getLog( LateralJGReceiverConnection.class );

    private Channel javagroups;
    private Message mes;


    private ILateralCacheJGListener ilcl;

    private int puts = 0;

//    /**
//     * Constructor for the LateralJGReceiverConnection object
//     *
//     * @param socket
//     * @param ilcl
//     */
    /**
     * Constructor for the LateralJGReceiverConnection object
     *
     * @param ilcl
     */
    public LateralJGReceiverConnection( Message mes, ILateralCacheJGListener ilcl )
    {
        this.ilcl = ilcl;
        this.mes = mes;
//        this.javagroups = javagroups;

    }

    //        this.socket = socket;
//        this.led = led;
//
//        try
//        {
//            ois = new ObjectInputStream( socket.getInputStream() );
//        }
//        catch ( Exception e )
//        {
//            log.error( "Could not open ObjectInputStream to " + socket, e );
//        }
//    }


    /**
     * Main processing method for the LateralJGReceiverConnection object
     *
     * @return
     * @param led
     */
    public void run( )
    {
        Serializable obj = null;
//        Object obj;

        try
        {
//            while ( true )
//            {
//                obj = ois.readObject();
//                LateralElementDescriptor led = ( LateralElementDescriptor ) obj;
                LateralElementDescriptor led = ( LateralElementDescriptor ) mes.getObject();
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

//        try
//        {
//            ois.close();
//        }
//        catch ( Exception e )
//        {
//            log.error( "Could not close connection", e );
//        }
        //return obj;
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

//        ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
//
//        if ( oos != null )
//        {
//            try
//            {
//                oos.writeObject( obj );
//                oos.flush();
//            }
//            catch ( IOException e )
//            {
//                oos = null;
//                log.error( "Detected problem with connection", e );
//                throw e;
//            }
//        }
        return obj;
    }
}
