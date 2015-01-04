package org.apache.commons.jcs.auxiliary.lateral.javagroups.utils;

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

import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @version $Id: JGRpcOpener.java 234393 2005-08-22 00:12:06Z asmuts $
 */
public class JGRpcOpener
    implements Runnable
{

    private static final Log log = LogFactory.getLog( JGRpcOpener.class );

    //private Socket socket;
    private Channel rpcCh;

    private RpcDispatcher disp;

    private String groupName;

    private ILateralCacheJGListener ilcl;

    private ILateralCacheAttributes ilca;

    /**
     * Constructor for the SocketOpener object
     *
     * @param ilcl
     * @param ilca
     * @param timeOut
     * @param groupName
     * @return
     */
    public static RpcDispatcher openSocket( ILateralCacheJGListener ilcl, ILateralCacheAttributes ilca, int timeOut,
                                           String groupName )
    {
        JGRpcOpener opener = new JGRpcOpener( ilcl, ilca, groupName );
        Thread t = new Thread( opener );
        t.start();
        try
        {
            t.join( timeOut );
        }
        catch ( InterruptedException ire )
        {
            log.error( ire );
        }
        return opener.getSocket();
    }

    /**
     * Constructor for the SocketOpener object
     *
     * @param ilcl
     * @param ilca
     * @param groupName
     *
     */
    public JGRpcOpener( ILateralCacheJGListener ilcl, ILateralCacheAttributes ilca, String groupName )
    {
        this.rpcCh = null;
        this.ilcl = ilcl;
        this.ilca = ilca;
        this.groupName = groupName;
    }

    /** Main processing method for the SocketOpener object */
    public void run()
    {
        try
        {

            //String props="UDP(mcast_addr=" + ilca.getUdpMulticastAddr() +
            // ";mcast_port=" + ilca.getUdpMulticastPort()+
            // "):PING:MERGE2(min_interval=5000;max_interval=10000):FD:STABLE:NAKACK:UNICAST:FLUSH:GMS:VIEW_ENFORCER:QUEUE";
            rpcCh = new JChannel( ilca.getJGChannelProperties() );
            rpcCh.setOpt( Channel.LOCAL, Boolean.FALSE );
            disp = new RpcDispatcher( rpcCh, null, null, ilcl );
            rpcCh.connect( groupName );

            if ( log.isInfoEnabled() )
            {
                log.info( "Is Connected = " + rpcCh.isConnected() );
            }

        }
        catch ( Exception e )
        {
            log.error( "Problem getting dispatcher.", e );
        }
    }

    /**
     * Gets the socket attribute of the SocketOpener object
     *
     * @return
     */
    public RpcDispatcher getSocket()
    {
        return disp;
    }
}
