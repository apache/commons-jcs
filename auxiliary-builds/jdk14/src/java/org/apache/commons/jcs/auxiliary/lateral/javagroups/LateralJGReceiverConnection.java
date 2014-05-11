package org.apache.commons.jcs.auxiliary.lateral.javagroups;

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

import org.apache.commons.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.commons.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Message;

import java.io.Serializable;

/**
 * Separate thread run when a command comes into the LateralJGReceiver.
 *
 * @version $Id: LateralJGReceiverConnection.java,v 1.7 2002/02/15 04:33:37
 *          jtaylor Exp $
 */
public class LateralJGReceiverConnection
    implements Runnable
{

    private static final Log log = LogFactory.getLog( LateralJGReceiverConnection.class );

    //private Channel javagroups;
    private Message mes;

    private ILateralCacheJGListener ilcl;

    /**
     * Constructor for the LateralJGReceiverConnection object
     *
     * @param mes
     *            The JGroups message
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
    public void run()
    {
        Object obj = null;
        try
        {
            obj = mes.getObject();
            LateralElementDescriptor led = (LateralElementDescriptor)obj;
            if ( led == null )
            {
                log.warn( "LateralElementDescriptor is null! Can't do anything." );
            }
            else
            {
                if ( led.requesterId == LateralCacheInfo.listenerId )
                {
                    log.debug( "from self" );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "receiving LateralElementDescriptor from another, led = " + ", led = " + led
                            + ", led.command = " + led.command + ", led.ce = " + led.ce + ", ilcl = " + ilcl );
                    }
                    if ( led.command == LateralElementDescriptor.UPDATE )
                    {
                        ilcl.handlePut( led.ce );
                    }
                    else if ( led.command == LateralElementDescriptor.REMOVE )
                    {
                        ilcl.handleRemove( led.ce.getCacheName(), led.ce.getKey() );
                    }
                    else if ( led.command == LateralElementDescriptor.GET )
                    {
                        /* Serializable obj = */getAndRespond( led.ce.getCacheName(), led.ce.getKey() );

                    }
                }
            }

        }
        catch ( java.io.EOFException e )
        {
            log.info( "Caught java.io.EOFException closing connection." );
        }
        catch ( java.net.SocketException e )
        {
            log.info( "Caught java.net.SocketException closing connection." );
        }
        catch ( Exception e )
        {
            log.error( "Unexpected exception. obj = " + obj, e );
        }
    }

    /**
     * Send back the object if found.
     *
     * @return The {3} value
     * @param cacheName
     * @param key
     * @throws Exception
     */
    private Serializable getAndRespond( String cacheName, K key )
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
