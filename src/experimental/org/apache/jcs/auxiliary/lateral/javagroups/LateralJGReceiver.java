package org.apache.jcs.auxiliary.lateral.javagroups;

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
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
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
