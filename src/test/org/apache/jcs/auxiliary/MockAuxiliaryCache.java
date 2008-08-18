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
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Mock auxiliary for unit tests.
 * <p>
 * @author Aaron Smuts
 */
public class MockAuxiliaryCache
    implements AuxiliaryCache
{
    private static final long serialVersionUID = 1L;

    /** Can setup the cache type */
    public int cacheType = ICache.DISK_CACHE;

    /** Can setup status */
    public int status = CacheConstants.STATUS_ALIVE;

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
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    public Map getMultiple( Set keys )
    {
        return new HashMap();
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

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatus()
     */
    public int getStatus()
    {
        // TODO Auto-generated method stub
        return status;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getCacheName()
     */
    public String getCacheName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getGroupKeys(java.lang.String)
     */
    public Set getGroupKeys( String group )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICache#getStats()
     */
    public String getStats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheType#getCacheType()
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

    public void setCacheEventLogger( ICacheEventLogger cacheEventLogger )
    {
        // TODO Auto-generated method stub

    }

    public void setElementSerializer( IElementSerializer elementSerializer )
    {
        // TODO Auto-generated method stub
        
    }
}
