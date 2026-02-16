package org.apache.commons.jcs4.engine.logging;

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

import java.util.Date;

import org.apache.commons.jcs4.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger.CacheEventType;

/** It's returned from create and passed into log. */
public record CacheEvent<K>(
        /** The time at which this object was created. */
        long createTime,

        /** The auxiliary or other source of the event. */
        String source,

        /** The cache region */
        String region,

        /** The event type: update, get, remove, etc. */
        CacheEventType eventType,

        /** Disk location, ip, etc. */
        String optionalDetails,

        /** The key that was put or retrieved. */
        K key
) implements ICacheEvent<K>
{
    /** Don't change. */
    private static final long serialVersionUID = -5913139566421714330L;

    /**
     * Default Constructor
     */
    public CacheEvent()
    {
        this(System.currentTimeMillis(), null, null, null, null, null);
    }

    /**
     * Constructor
     *
     * @param source
     * @param region
     * @param eventType
     * @param optionalDetails
     * @param key
     */
    public CacheEvent(final String source, final String region, CacheEventType eventType,
            String optionalDetails, K key)
    {
        this(System.currentTimeMillis(), source, region, eventType, optionalDetails, key);
    }

    /**
     * @return reflection toString
     */
    @Override
    public String toString()
    {
    	final StringBuilder sb = new StringBuilder();
    	sb.append("CacheEvent: ").append(eventType)
    	  .append(" Created: ").append(new Date(createTime));
    	if (source != null)
    	{
        	sb.append(" Source: ").append(source);
    	}
    	if (region != null)
    	{
        	sb.append(" Region: ").append(region);
    	}
    	if (key != null)
    	{
        	sb.append(" Key: ").append(key);
    	}
    	if (optionalDetails != null)
    	{
        	sb.append(" Details: ").append(optionalDetails);
    	}
        return sb.toString();
    }
}
