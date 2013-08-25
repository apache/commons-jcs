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

import org.apache.commons.jcs.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Attributes used by {@link JavaGroupsCacheFactory#createCache}to configure an
 * instance of {@link JavaGroupsCache}.
 *
 * <h3>Configurable Properties:</h3>
 *
 * <dl>
 * <dt>channelFactoryClassName</dt>
 * <dd>Name of an {@link org.jgroups.ChannelFactory}implementation which will
 * be used to create the channel for the instance. Defaults to
 * {@link org.jgroups.JChannelFactory}.</dd>
 * <dt>channelProperties</dt>
 * <dd>A JavaGroups properties object which will be used by the channel to
 * create the protocol stack. Either a properties string, or the URL of a file
 * containing the properties in XML form is valid. Defaults to null which causes
 * the Channel implementation to use its defaults.</dd>
 * </dl>
 *
 * @version $Id: JavaGroupsCacheAttributes.java,v 1.2 2005/01/07 22:27:54 asmuts
 *          Exp $
 */
public class JavaGroupsCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements AuxiliaryCacheAttributes
{
    private String channelFactoryClassName = "org.jgroups.JChannelFactory";

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

    public String getJGChannelProperties()
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

    /**
     * Return a copy of this JavaGroupsCacheAttributes, cast to an
     * AuxiliaryCacheAttributes
     */
    public AuxiliaryCacheAttributes copy()
    {
        return (AuxiliaryCacheAttributes) this.clone();
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
