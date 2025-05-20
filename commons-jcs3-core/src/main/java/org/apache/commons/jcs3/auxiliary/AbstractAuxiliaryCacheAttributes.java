package org.apache.commons.jcs3.auxiliary;

import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This has common attributes used by all auxiliaries.
 */
public abstract class AbstractAuxiliaryCacheAttributes
    implements AuxiliaryCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -6594609334959187673L;

    /** CacheName */
    private String cacheName;

    /** Name */
    private String name;

    /** EventQueueType -- pooled, or single threaded */
    private ICacheEventQueue.QueueType eventQueueType;

    /** Named when pooled */
    private String eventQueuePoolName;

    /**
     * @see Object#clone()
     */
    @Override
    public AbstractAuxiliaryCacheAttributes clone()
    {
        try
        {
            return (AbstractAuxiliaryCacheAttributes) super.clone();
        }
        catch (final CloneNotSupportedException e)
        {
            throw new IllegalStateException("Clone not supported. This should never happen.", e);
        }
    }

    /**
     * Gets the cacheName attribute of the AuxiliaryCacheAttributes object
     *
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Sets the pool name to use. If a pool is not found by this name, the thread pool manager will
     * return a default configuration.
     *
     * @return name of thread pool to use for this auxiliary
     */
    @Override
    public String getEventQueuePoolName()
    {
        return eventQueuePoolName;
    }

    /**
     * @return SINGLE or POOLED
     */
    @Override
    public ICacheEventQueue.QueueType getEventQueueType()
    {
        return eventQueueType;
    }

    /**
     * Gets the name attribute of the AuxiliaryCacheAttributes object
     *
     * @return The name value
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name
     */
    @Override
    public void setCacheName( final String name )
    {
        this.cacheName = name;
    }

    /**
     * If you choose a POOLED event queue type, the value of EventQueuePoolName will be used. This
     * is ignored if the pool type is SINGLE
     *
     * @param s SINGLE or POOLED
     */
    @Override
    public void setEventQueuePoolName( final String s )
    {
        eventQueuePoolName = s;
    }

    /**
     * SINGLE is the default. If you choose POOLED, the value of EventQueuePoolName will be used
     *
     * @param queueType SINGLE or POOLED
     */
    @Override
    public void setEventQueueType( final ICacheEventQueue.QueueType queueType )
    {
        this.eventQueueType = queueType;
    }

    /**
     * This is the name of the auxiliary in configuration file.
     *
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes#setName(String)
     */
    @Override
    public void setName( final String s )
    {
        this.name = s;
    }
}
