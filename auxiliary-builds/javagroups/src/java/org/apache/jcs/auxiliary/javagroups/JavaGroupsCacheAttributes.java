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

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

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
 * @author <a href="james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class JavaGroupsCacheAttributes implements AuxiliaryCacheAttributes
{
    private String cacheName;
    private String name;

    private String channelFactoryClassName = "org.javagroups.JChannelFactory";
    private String channelProperties = null;

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

