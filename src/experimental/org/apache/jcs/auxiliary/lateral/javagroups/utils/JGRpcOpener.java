package org.apache.jcs.auxiliary.lateral.javagroups.utils;

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
import java.io.IOException;
import java.io.InterruptedIOException;

import org.javagroups.JChannel;
import org.javagroups.Channel;
import org.javagroups.blocks.RpcDispatcher;
import org.javagroups.blocks.GroupRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;

/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id$
 */
public class JGRpcOpener implements Runnable
{

    private final static Log log =
        LogFactory.getLog( JGRpcOpener.class );


    private String host;
    private int port;
    //private Socket socket;
    private Channel rpcCh;
    private RpcDispatcher disp;

    private String groupName;
    private ILateralCacheJGListener ilcl;
    private ILateralCacheAttributes ilca;

    /** Constructor for the SocketOpener object */
    public static RpcDispatcher openSocket( ILateralCacheJGListener ilcl, ILateralCacheAttributes ilca, int timeOut, String groupName )
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
            log.error(ire);
        }
        return opener.getSocket();
    }


    /**
     * Constructor for the SocketOpener object
     *
     * @param host
     * @param port
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

            String props="UDP(mcast_addr=" + ilca.getUdpMulticastAddr() + ";mcast_port=" + ilca.getUdpMulticastPort()+ "):PING:MERGE2(min_interval=5000;max_interval=10000):FD:STABLE:NAKACK:UNICAST:FLUSH:GMS:VIEW_ENFORCER:QUEUE";
            rpcCh = new JChannel(props);
            rpcCh.setOpt(rpcCh.LOCAL, Boolean.FALSE);
            disp = new RpcDispatcher( rpcCh, null, null, ilcl );
            rpcCh.connect(groupName);

        }
        catch ( Exception e )
        {
            log.error(e);
        }
    }

    /** Gets the socket attribute of the SocketOpener object */
    public RpcDispatcher getSocket()
    {
        return disp;
    }
}
