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

import java.util.HashMap;
import java.io.IOException;

import org.javagroups.JChannel;
import org.javagroups.Channel;
import org.javagroups.Message;
import org.javagroups.blocks.RpcDispatcher;
import org.javagroups.util.RspList;
import org.javagroups.blocks.GroupRequest;

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
        JGConnectionHolder ins = ( JGConnectionHolder ) instances.get( String.valueOf( ilca.getUdpMulticastAddr() ) );
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
                        log.debug( "created new listener " + ilca.getUdpMulticastAddr() );
                    }
                    instances.put( String.valueOf( ilca.getUdpMulticastAddr() ), ins );
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
