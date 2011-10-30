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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Mock auxiliary for unit tests.
 * <p>
 * @author Aaron Smuts
 */
public class MockAuxiliaryCache
    extends AbstractAuxiliaryCache
{
    /** Don't change */
    private static final long serialVersionUID = 1L;

    /** Can setup the cache type */
    public int cacheType = ICache.DISK_CACHE;

    /** Can setup status */
    public int status = CacheConstants.STATUS_ALIVE;

    /** Times getMatching was Called */
    public int getMatchingCallCount = 0;

    /**
     * @param ce
     * @throws IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param pattern
     * @return Map
     * @throws IOException
     */
    public Map<Serializable, ICacheElement> getMatching(String pattern)
        throws IOException
    {
        getMatchingCallCount++;
        return new HashMap<Serializable, ICacheElement>();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    public Map<Serializable, ICacheElement> getMultiple(Set<Serializable> keys)
    {
        return new HashMap<Serializable, ICacheElement>();
    }

    /**
     * @param key
     * @return boolean
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @throws IOException
     */
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @return int
     */
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @return int
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * @return null
     */
    public String getCacheName()
    {
        return null;
    }

    /**
     * @param group
     * @return null
     * @throws IOException
     */
    public Set<Serializable> getGroupKeys( String group )
        throws IOException
    {
        return null;
    }

    /**
     * @return null
     */
    public IStats getStatistics()
    {
        return null;
    }

    /**
     * @return null
     */
    public String getStats()
    {
        return null;
    }

    /**
     * @return cacheType
     */
    public int getCacheType()
    {
        return cacheType;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return null;
    }

    /** @return null */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return null;
    }
}
