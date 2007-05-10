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
public class AuxiliaryCacheMockImpl
    implements AuxiliaryCache
{
    private static final long serialVersionUID = 1L;

    /** Can setup the cache type */
    public int cacheType = ICache.DISK_CACHE;

    /** Can setup status */
    public int status = CacheConstants.STATUS_ALIVE;

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#get(java.io.Serializable)
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#remove(java.io.Serializable)
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#removeAll()
     */
    public void removeAll()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#dispose()
     */
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getSize()
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
}
