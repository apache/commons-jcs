package org.apache.commons.jcs.engine.control;

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

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;

/** For testing. */
public class MockCompositeCacheManager
    implements ICompositeCacheManager
{
    /** The cache that was returned. */
    private CompositeCache<? extends Serializable, ? extends Serializable> cache;

    /** Properties with which this manager was configured. This is exposed for other managers. */
    private Properties configurationProperties;

    /**
     * @param cacheName
     * @return Returns a CompositeCache
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K extends Serializable, V extends Serializable> CompositeCache<K, V> getCache( String cacheName )
    {
        if ( cache == null )
        {
            System.out.println( "Creating mock cache" );
            CompositeCache<K, V> newCache = new CompositeCache<K, V>( cacheName, new CompositeCacheAttributes(),
                                                          new ElementAttributes() );
            this.setCache( newCache );
        }

        return (CompositeCache<K, V>)cache;
    }

    /**
     * @param cache The cache to set.
     */
    public void setCache( CompositeCache<? extends Serializable, ? extends Serializable> cache )
    {
        this.cache = cache;
    }

    /**
     * @return Returns the cache.
     */
    public CompositeCache<? extends Serializable, ? extends Serializable> getCache()
    {
        return cache;
    }

    /**
     * This is exposed so other manager can get access to the props.
     * <p>
     * @param props
     */
    public void setConfigurationProperties( Properties props )
    {
        this.configurationProperties = props;
    }

    /**
     * This is exposed so other manager can get access to the props.
     * <p>
     * @return the configurationProperties
     */
    @Override
    public Properties getConfigurationProperties()
    {
        return configurationProperties;
    }

    /** @return Mock */
    @Override
    public String getStats()
    {
        return "Mock";
    }
}
