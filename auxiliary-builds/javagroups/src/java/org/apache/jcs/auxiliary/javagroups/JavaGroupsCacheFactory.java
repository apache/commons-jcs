package org.apache.jcs.auxiliary.javagroups;

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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javagroups.ChannelFactory;
import org.javagroups.Channel;

/**
 * AuxiliaryCacheFactory for creating instances of {@link JavaGroupsCache}
 * for a particular CompositeCache and {@link JavaGroupsCacheAttributes}.
 *
 * @author <a href="james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class JavaGroupsCacheFactory implements AuxiliaryCacheFactory
{
    private final static Log log =
        LogFactory.getLog( JavaGroupsCacheFactory.class );

    private String name;

    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca,
                                       CompositeCache cache )
    {
        try
        {
            // Cast provided attributes to JavaGroupsCacheAttributes

            JavaGroupsCacheAttributes attributes =
                ( JavaGroupsCacheAttributes ) iaca;

            // Create a ChannelFactory using the classname specified in the
            // config as 'channelFactoryClassName'

            ChannelFactory factory = ( ChannelFactory ) Class.forName(
                attributes.getChannelFactoryClassName() ).newInstance();

            // Create a channel based on 'channelProperties' from the config

            Channel channel =
                factory.createChannel( attributes.getChannelProperties() );

            // Return a new JavaGroupsCache for the new channel.

            return new JavaGroupsCache( cache, channel );
        }
        catch ( Exception e )
        {
            log.error( "Failed to create JavaGroupsCache", e );

            return null;
        }
    }

    /**
     * Accessor for name property
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Mutator for name property
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
