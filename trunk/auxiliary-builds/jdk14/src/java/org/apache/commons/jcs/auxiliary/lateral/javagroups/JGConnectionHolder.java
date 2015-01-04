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

import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.utils.JGRpcOpener;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.utils.JGSocketOpener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.blocks.RpcDispatcher;

import java.io.IOException;
import java.util.HashMap;

/**
 * This holds connections, or channels, for jgroups.
 */
public class JGConnectionHolder
{

    private static final Log log = LogFactory.getLog( JGConnectionHolder.class );

    private Channel jg;

    private RpcDispatcher disp;

    private ILateralCacheAttributes ilca;

    /**
     * Description of the Field
     */
    protected static final HashMap instances = new HashMap();

    /**
     * Gets the instance attribute of the LateralGroupCacheJGListener class
     *
     * @return The instance value
     * @param ilca
     */
    public static JGConnectionHolder getInstance( ILateralCacheAttributes ilca )
    {
        //throws IOException, NotBoundException
        //JGConnectionHolder ins = (JGConnectionHolder) instances.get( ilca.getJGChannelProperties() );
        JGConnectionHolder ins = (JGConnectionHolder) instances.get( ilca.getCacheName() );
        try
        {
            synchronized ( JGConnectionHolder.class )
            {
                if ( ins == null )
                {
                    ins = new JGConnectionHolder( ilca );
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "created new listener " + ilca.getJGChannelProperties() );
                }
                instances.put( ilca.getJGChannelProperties(), ins );
            }
        }
        catch ( Exception e )
        {
            log.error( "trouble intializing", e );
        }
        return ins;
    }

    /**
     * Constructor for the JGConnectionHolder object
     *
     * @param lca
     * @param ilca
     */
    private JGConnectionHolder( ILateralCacheAttributes ilca )
    {
        this.ilca = ilca;
    }

    /**
     * Creates a channel.
     *
     * @return channel
     * @throws IOException
     */
    public synchronized Channel getChannel()
        throws IOException
    {
        if ( jg == null )
        {
            //jg = JGSocketOpener.openSocket( ilca, 5000, IJGConstants.DEFAULT_JG_GROUP_NAME );
            jg = JGSocketOpener.openSocket( ilca, 5000, this.ilca.getCacheName() );
            if ( log.isInfoEnabled() )
            {
                log.info( "Created channel " + jg + " for region name " + this.ilca.getCacheName() );
                if ( jg != null )
                {
                    log.info( "Channel connection status; Connected = " + jg.isConnected() + " Open = " + jg.isOpen() );
                }
            }
        }
        return jg;
    }

    /**
     * Gets the Dispatcher attribute of the JGConnectionHolder object
     *
     * @return The Dispatcher value
     * @throws IOException
     */
    public synchronized RpcDispatcher getDispatcher()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating Dispatcher, jgroups group name " + IJGConstants.RPC_JG_GROUP_NAME );
        }

        try
        {
            if ( disp == null )
            {
                synchronized ( JGConnectionHolder.class )
                {
                    if ( disp == null )
                    {
                        disp = JGRpcOpener.openSocket( (ILateralCacheJGListener) LateralGroupCacheJGListener
                            .getInstance( ilca ), ilca, 5000, IJGConstants.RPC_JG_GROUP_NAME );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem creating dispatcher", e );
        }
        return disp;
    }

}
