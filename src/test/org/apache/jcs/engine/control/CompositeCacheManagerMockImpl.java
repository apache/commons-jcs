package org.apache.jcs.engine.control;

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

import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 */
public class CompositeCacheManagerMockImpl
    implements ICompositeCacheManager
{

    private CompositeCache cache;

    /* (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICompositeCacheManager#getCache(java.lang.String)
     */
    public CompositeCache getCache( String cacheName )
    {
        if ( cache == null )
        {
            System.out.println( "Creating mock cache" );
            CompositeCache newCache = new CompositeCache( cacheName, new CompositeCacheAttributes(), new ElementAttributes() );
            this.setCache( newCache );
        }
        return cache;
    }

    /**
     * @param cache The cache to set.
     */
    public void setCache( CompositeCache cache )
    {
        this.cache = cache;
    }

    /**
     * @return Returns the cache.
     */
    public CompositeCache getCache()
    {
        return cache;
    }

}
