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
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.ChannelFactory;

/**
 * AuxiliaryCacheFactory for creating instances of {@link JavaGroupsCache}for a
 * particular CompositeCache and {@link JavaGroupsCacheAttributes}.
 *
 * @version $Id: JavaGroupsCacheFactory.java,v 1.2 2004/06/12 02:34:13 asmuts
 *          Exp $
 */
public class JavaGroupsCacheFactory
    implements AuxiliaryCacheFactory
{
    private static final Log log = LogFactory.getLog( JavaGroupsCacheFactory.class );

    private String name;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes,
     *      org.apache.commons.jcs.engine.behavior.ICompositeCacheManager)
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr )
    {
        // ignore the maanger

        try
        {
            // Cast provided attributes to JavaGroupsCacheAttributes

            JavaGroupsCacheAttributes attributes = (JavaGroupsCacheAttributes) iaca;

            // Create a ChannelFactory using the classname specified in the
            // config as 'channelFactoryClassName'

            ChannelFactory factory = (ChannelFactory) Class.forName( attributes.getChannelFactoryClassName() )
                .newInstance();

            // Create a channel based on 'channelProperties' from the config

            Channel channel = factory.createChannel( attributes.getJGChannelProperties() );

            // Return a new JavaGroupsCache for the new channel.

            return new JavaGroupsCache( cacheMgr, attributes.getCacheName(), channel, attributes.isGetFromPeers() );
        }
        catch ( Exception e )
        {
            log.error( "Failed to create JavaGroupsCache", e );

            return null;
        }
    }

    /**
     * Accessor for name property
     * @return String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Mutator for name property
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
