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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.jcs.auxiliary.lateral.javagroups.utils.JGSocketOpener;
import org.apache.jcs.auxiliary.lateral.javagroups.utils.JGRpcOpener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.javagroups.JChannel;
import org.javagroups.Channel;
import org.javagroups.Message;
import org.javagroups.blocks.RpcDispatcher;
import org.javagroups.ChannelNotConnectedException;

/**
 * Processes commands from the server socket.
 *
 * @version $Id$
 */
public class LateralJGReceiver implements IJGConstants, Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralJGReceiver.class );

    private int port;

    private ILateralCacheJGListener ilcl;
    private ILateralCacheAttributes ilca;
    /**
     * How long the server will block on an accept(). 0 is infinte.
     */
    private final static int sTimeOut = 5000;


    /**
     * Main processing method for the LateralJGReceiver object
     */
    public void run()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Listening on port " + port );
            }

            JGConnectionHolder holder = JGConnectionHolder.getInstance(ilca);
            Channel javagroups = holder.getChannel();
            RpcDispatcher disp = holder.getDispatcher();

            if ( javagroups == null )
            {
                log.error( "JavaGroups is null" );
                throw new IOException( "javagroups is null" );
            }


            while ( true )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Wating for messages." );
                }

                Message mes = null;
                try
                {
                    Object obj = javagroups.receive( 0 );
                    if ( obj != null && obj instanceof org.javagroups.Message )
                    {
                        mes = ( Message ) obj;
                        log.info( "Starting new socket node." );
                        new Thread( new LateralJGReceiverConnection( mes, ilcl ) ).start();
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( obj );
                        }
                    }
                }
                catch ( ChannelNotConnectedException cnce )
                {
                    log.warn(cnce);
                    // this will cycle unitl connected and eat up the processor
                    // need to throw out and recover
                    // seems to periodically require about 50 tries.
                }
                catch ( Exception e )
                {
                    log.error( "problem receiving", e );
                }

                //InetAddress inetAddress = javagroups..getInetAddress();
                //if ( log.isDebugEnabled() )
                //{
                //    log.debug( "Connected to client at " + inetAddress );
                //}
                //log.info( "Connected to client at " + inetAddress );
            }
        }
        catch ( Exception e )
        {
            log.error( "Major intialization problem", e );
        }
    }


    /**
     * Constructor for the LateralJGReceiver object
     *
     * @param lca
     * @param ilcl
     */
    public LateralJGReceiver( ILateralCacheAttributes ilca, ILateralCacheJGListener ilcl )
    {
        this.port = ilca.getTcpListenerPort();
        this.ilcl = ilcl;
        this.ilca = ilca;
        if ( log.isDebugEnabled() )
        {
            log.debug( "ilcl = " + ilcl );
        }
    }
}
