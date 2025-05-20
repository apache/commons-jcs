/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jcs.auxiliary.disk.file;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheFactory;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogFactory;

/** Create Disk File Caches */
public class FileDiskCacheFactory
    implements AuxiliaryCacheFactory
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( FileDiskCacheFactory.class );

    /** The auxiliary name. */
    private String name;

    /** The manager used by this factory instance */
    private FileDiskCacheManager diskFileCacheManager;

    /**
     * Creates a manager if we don't have one, and then uses the manager to create the cache. The
     * same factory will be called multiple times by the composite cache to create a cache for each
     * region.
     *
     * @param attr config
     * @param cacheMgr the manager to use if needed
     * @param cacheEventLogger the event logger
     * @param elementSerializer the serializer
     * @return AuxiliaryCache
     */
    @Override
    public <K, V> FileDiskCache<K, V> createCache(
            final AuxiliaryCacheAttributes attr, final ICompositeCacheManager cacheMgr,
           final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer )
    {
        final FileDiskCacheAttributes idfca = (FileDiskCacheAttributes) attr;
        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating DiskFileCache for attributes = " + idfca );
        }
        synchronized( this )
        {
            if ( diskFileCacheManager == null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Creating DiskFileCacheManager" );
                }
                diskFileCacheManager = new FileDiskCacheManager( idfca, cacheEventLogger, elementSerializer );
            }
            return diskFileCacheManager.getCache( idfca );
        }
    }

    /**
     * Gets the name attribute of the DiskCacheFactory object
     *
     * @return The name value
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the DiskCacheFactory object
     *
     * @param name The new name value
     */
    @Override
    public void setName( final String name )
    {
        this.name = name;
    }

    /**
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#initialize()
     */
    @Override
    public void initialize()
    {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#dispose()
     */
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }
}
