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


import java.util.HashMap;
import java.io.IOException;

import org.jgroups.JChannel;
import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.blocks.GroupRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.jcs.auxiliary.lateral.javagroups.utils.JGRpcOpener;
import org.apache.jcs.auxiliary.lateral.javagroups.utils.JGSocketOpener;

/**
 * Description of the Class
 */
public class JGConnectionHolder
{

    private final static Log log =
        LogFactory.getLog( JGConnectionHolder.class );

    private Channel jg;
    private RpcDispatcher disp;

    private ILateralCacheAttributes ilca;

    /**
     * Description of the Field
     */
    protected final static HashMap instances = new HashMap();


    /**
     * Gets the instance attribute of the LateralGroupCacheJGListener class
     *
     * @return The instance value
     * @param ilca
     */
    public static JGConnectionHolder getInstance( ILateralCacheAttributes ilca )
    {
        //throws IOException, NotBoundException
        JGConnectionHolder ins = ( JGConnectionHolder ) instances.get( ilca.getJGChannelProperties() );
        try
        {
            if ( ins == null )
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
     * Gets the {3} attribute of the JGConnectionHolder object
     *
     * @return The {3} value
     * @exception IOException
     */
    public Channel getChannel()
        throws IOException
    {
        if ( jg == null )
        {
            synchronized ( JGConnectionHolder.class )
            {
                if ( jg == null )
                {
                    jg = JGSocketOpener.openSocket( ilca, 5000, IJGConstants.DEFAULT_JG_GROUP_NAME );
                }
            }
        }
        return jg;
    }

    /**
     * Gets the {3} attribute of the JGConnectionHolder object
     *
     * @return The {3} value
     * @exception IOException
     */
    public RpcDispatcher getDispatcher()
        throws IOException
    {
        try
        {
            if ( disp == null )
            {
                synchronized ( JGConnectionHolder.class )
                {
                    if ( disp == null )
                    {
                        disp = JGRpcOpener.openSocket( ( ILateralCacheJGListener ) LateralGroupCacheJGListener.getInstance( ilca ), ilca, 5000, IJGConstants.RPC_JG_GROUP_NAME );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        return disp;
    }

}
