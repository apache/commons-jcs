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

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;

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


    /**
     * Constructor for the LateralJGReceiverConnection object
     *
     * @param ilcl
     */
    public LateralJGReceiverConnection( Message mes, ILateralCacheJGListener ilcl )
    {
        this.ilcl = ilcl;
        this.mes = mes;
     }



    /**
     * Main processing method for the LateralJGReceiverConnection object
     *
     * @return
     * @param led
     */
    public void run( )
    {
        Serializable obj = null;

        try
        {

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

        return obj;
    }
}
