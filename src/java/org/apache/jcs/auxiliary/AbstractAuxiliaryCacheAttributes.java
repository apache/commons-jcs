package org.apache.jcs.auxiliary;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.jcs.engine.behavior.ICacheEventQueue;

/*
 * This has common attributes used by all auxiliaries.
 */
public abstract class AbstractAuxiliaryCacheAttributes
    implements AuxiliaryCacheAttributes
{
    /** Don't change     */
    private static final long serialVersionUID = -6594609334959187673L;

    /** cacheName */
    protected String cacheName;

    /** name  */
    protected String name;

    /** eventQueueType -- pooled or single threaded  */
    protected int eventQueueType;

    /** Named when pooled */
    protected String eventQueuePoolName;

    /**
     * @param name
     */
    public void setCacheName( String name )
    {
        this.cacheName = name;
    }

    /**
     * Gets the cacheName attribute of the AuxiliaryCacheAttributes object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * This is the name of the auxiliary in configuration file.
     * <p>
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setName(java.lang.String)
     */
    public void setName( String s )
    {
        this.name = s;
    }

    /**
     * Gets the name attribute of the AuxiliaryCacheAttributes object
     * <p>
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * SINGLE is the default. If you choose POOLED, the value of
     * EventQueuePoolName will be used
     * <p>
     * @param s
     *            SINGLE or POOLED
     * @return
     */
    public void setEventQueueType( String s )
    {
        if ( s != null )
        {
            if ( s.equalsIgnoreCase( POOLED_QUEUE_TYPE ) )
            {
                this.eventQueueType = ICacheEventQueue.POOLED_QUEUE_TYPE;
            }
            else
            {
                // single by default
                this.eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
            }
        }
        else
        {
            //  null, single by default
            this.eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
        }
    }

    /**
     * @return SINGLE or POOLED
     */
    public String getEventQueueType()
    {
        if ( this.eventQueueType == ICacheEventQueue.POOLED_QUEUE_TYPE )
        {
            return POOLED_QUEUE_TYPE;
        }
        else
        {
            return SINGLE_QUEUE_TYPE;
        }
    }

    /**
     * Returns the value used by the factory.
     * <p>
     * @return code
     */
    public int getEventQueueTypeFactoryCode()
    {
        return this.eventQueueType;
    }

    /**
     * If you choose a POOLED event queue type, the value of EventQueuePoolName
     * will be used. This is ignored if the pool type is SINGLE
     * <p>
     * @param s
     *            SINGLE or POOLED
     * @return
     */
    public void setEventQueuePoolName( String s )
    {
        eventQueuePoolName = s;
    }

    /**
     * Sets the pool name to use. If a pool is not found by this name, the
     * thread pool manager will return a default configuration.
     * <p>
     * @return name of thread pool to use for this auxiliary
     */
    public String getEventQueuePoolName()
    {
        return eventQueuePoolName;
    }
}
