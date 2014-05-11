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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.ChannelFactory;
import org.jgroups.JChannelFactory;

/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever.
 *
 * @version $Id: JGSocketOpener.java 234393 2005-08-22 00:12:06Z asmuts $
 */
public class JGSocketOpener
    implements Runnable
{

    private static final Log log = LogFactory.getLog( JGSocketOpener.class );

    private ILateralCacheAttributes lca;

    private Channel javagroups;

    private String groupName;

    /**
     * Constructor for the <code>SocketOpener</code> object.
     * @param lca
     * @param timeOut
     * @param groupName
     * @return
     */
    public static Channel openSocket( ILateralCacheAttributes lca, int timeOut, String groupName )
    {
        JGSocketOpener opener = new JGSocketOpener( lca, groupName );
        Thread t = new Thread( opener );
        t.start();
        try
        {
            t.join( timeOut );
        }
        catch ( InterruptedException ire )
        {
            log.error( "Failed of connect in within timout of " + timeOut, ire );
        }
        return opener.getSocket();
    }

    /**
     * Constructor for the SocketOpener object
     * @param lca
     * @param groupName
     *
     * @param host
     * @param port
     */
    public JGSocketOpener( ILateralCacheAttributes lca, String groupName )
    {
        this.javagroups = null;
        this.lca = lca;
        this.groupName = groupName;
    }

    /**
     * Main processing method for the <code>SocketOpener</code> object.
     */
    public void run()
    {
        try
        {

            ChannelFactory factory = new JChannelFactory();

            // Create a channel based on 'channelProperties' from the config
            Channel channel = factory.createChannel( lca.getJGChannelProperties() );

            javagroups = channel; //new JChannel( lca.getJGChannelProperties()
                                  // );
            // don't send local
            javagroups.setOpt( Channel.LOCAL, Boolean.FALSE );

            javagroups.connect( groupName );

            if ( log.isInfoEnabled() )
            {
                log.info( "Is Connected = " + javagroups.isConnected() );
            }

        }
        catch ( Exception e )
        {
            log.error( "Problem connecting to channel.", e );
        }
    }

    /**
     * Gets the socket attribute of the <code>SocketOpener</code> object.
     */
    public Channel getSocket()
    {
        return javagroups;
    }
}
