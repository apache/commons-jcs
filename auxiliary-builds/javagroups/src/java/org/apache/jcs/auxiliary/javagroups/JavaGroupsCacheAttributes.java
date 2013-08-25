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

import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Attributes used by {@link JavaGroupsCacheFactory#createCache} to configure
 * an instance of {@link JavaGroupsCache}.
 *
 * <h3> Configurable Properties: </h3>
 *
 * <dl>
 *   <dt>channelFactoryClassName</dt>
 *   <dd>
 *     Name of an {@link org.javagroups.ChannelFactory} implementation which
 *     will be used to create the channel for the instance. Defaults to
 *     {@link org.javagroups.JChannelFactory}.
 *   </dd>
 *   <dt>channelProperties</dt>
 *   <dd>
 *     A JavaGroups properties object which will be used by the channel to
 *     create the protocol stack. Either a properties string, or the URL of
 *     a file containing the properties in XML form is valid. Defaults to null
 *     which causes the Channel implementation to use its defaults.
 *   </dd>
 * </dl>
 *
 * @version $Id$
 */
public class JavaGroupsCacheAttributes implements AuxiliaryCacheAttributes
{
    private String cacheName;
    private String name;

    private String channelFactoryClassName = "org.javagroups.JChannelFactory";
    private String channelProperties = null;
    private boolean getFromPeers = false;

    public String getChannelFactoryClassName()
    {
        return channelFactoryClassName;
    }

    public void setChannelFactoryClassName( String channelFactoryClassName )
    {
        this.channelFactoryClassName = channelFactoryClassName;
    }

    public String getChannelProperties()
    {
        return channelProperties;
    }

    public void setChannelProperties( String channelProperties )
    {
        this.channelProperties = channelProperties;
    }

    public boolean isGetFromPeers()
    {
        return getFromPeers;
    }

    public void setGetFromPeers( boolean getFromPeers )
    {
        this.getFromPeers = getFromPeers;
    }

    // ----------------------------------------------- AuxiliaryCacheAttributes

    /**
     * Accessor for cacheName property.
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Mutator for cacheName property.
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }

    /**
     * Accessor for name property.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Mutator for name property.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Return a copy of this JavaGroupsCacheAttributes, cast to an
     * AuxiliaryCacheAttributes
     */
    public AuxiliaryCacheAttributes copy()
    {
        return ( AuxiliaryCacheAttributes ) this.clone();
    }

    /**
     * Return a clone of this JavaGroupsCacheAttributes
     */
    public Object clone()
    {
        JavaGroupsCacheAttributes copy = new JavaGroupsCacheAttributes();

        copy.cacheName = this.cacheName;
        copy.name = this.name;

        copy.channelFactoryClassName = this.channelFactoryClassName;
        copy.channelProperties = this.channelProperties;

        return copy;
    }
}

