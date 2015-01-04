package org.apache.commons.jcs.auxiliary.javagroups;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javagroups.Channel;
import org.javagroups.ChannelFactory;

/**
 * AuxiliaryCacheFactory for creating instances of {@link JavaGroupsCache}
 * for a particular CompositeCache and {@link JavaGroupsCacheAttributes}.
 *
 * @version $Id$
 */
public class JavaGroupsCacheFactory implements AuxiliaryCacheFactory
{
    private static final Log log =
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

            return new JavaGroupsCache( cache,
                                        channel,
                                        attributes.isGetFromPeers() );
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
