package org.apache.jcs.auxiliary.lateral.javagroups.utils;


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

import java.io.IOException;
import java.io.InterruptedIOException;

import org.javagroups.JChannel;
import org.javagroups.Channel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

/**
 * Socket openere that will timeout on the initial connect rather than block
 * forever. Technique from core java II.
 *
 * @version $Id$
 */
public class JGSocketOpener implements Runnable
{

    private final static Log log =
        LogFactory.getLog( JGSocketOpener.class );


    private ILateralCacheAttributes lca;
    private Channel javagroups;

    private String groupName;

    /**
     * Constructor for the <code>SocketOpener</code> object.
     */
    public static Channel openSocket( ILateralCacheAttributes lca,
                                      int timeOut, String groupName )
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
    public JGSocketOpener( ILateralCacheAttributes lca, String groupName )
    {
        this.javagroups = null;
        this.lca = lca;
        this.groupName = groupName;
    }


    /**
     * Main processing method for the <code>SocketOpener</code>
     * object.
     */
    public void run()
    {
        try
        {
            // make configurable
            String props="UDP(mcast_addr=" + lca.getUdpMulticastAddr() + ";mcast_port=" + lca.getUdpMulticastPort()+ "):PING:MERGE2(min_interval=5000;max_interval=10000):FD:STABLE:NAKACK:UNICAST:" +
             "FRAG:FLUSH:GMS:VIEW_ENFORCER:STATE_TRANSFER:QUEUE";

            javagroups = new JChannel(props);
            javagroups.setOpt(javagroups.LOCAL, Boolean.FALSE);
            // could have a channel per region
            //javagroups.connect(IJGConstants.DEFAULT_JG_GROUP_NAME);
            javagroups.connect(groupName);

        }
        catch ( Exception e )
        {
            log.error(e);
        }
    }

    /**
     * Gets the socket attribute of the <code>SocketOpener</code>
     * object.
     */
    public Channel getSocket()
    {
        return javagroups;
    }
}
