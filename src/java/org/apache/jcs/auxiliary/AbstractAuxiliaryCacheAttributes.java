package org.apache.jcs.auxiliary;

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

import org.apache.jcs.engine.behavior.ICacheEventQueue;

/**
 * This has common attributes used by all auxiliaries.
 * <p>
 * @author aaronsm
 *
 */
public abstract class AbstractAuxiliaryCacheAttributes
    implements AuxiliaryCacheAttributes
{
    /**
     * cacheName
     */
    protected String cacheName;

    /**
     * name
     */
    protected String name;

    /**
     * eventQueueType -- pooled or single threaded
     */
    protected int eventQueueType;

    /**
     * Named when pooled
     */
    protected String eventQueuePoolName;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setCacheName(java.lang.String)
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getCacheName()
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setEventQueueType(java.lang.String)
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueueType()
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueueTypeFactoryCode()
     */
    public int getEventQueueTypeFactoryCode()
    {
        return this.eventQueueType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setEventQueuePoolName(java.lang.String)
     */
    public void setEventQueuePoolName( String s )
    {
        eventQueuePoolName = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueuePoolName()
     */
    public String getEventQueuePoolName()
    {
        return eventQueuePoolName;
    }

}
