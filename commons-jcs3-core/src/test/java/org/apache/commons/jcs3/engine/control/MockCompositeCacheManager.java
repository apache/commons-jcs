package org.apache.commons.jcs3.engine.control;

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

import java.util.Properties;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.engine.CompositeCacheAttributes;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;

/** For testing. */
public class MockCompositeCacheManager
    implements ICompositeCacheManager
{
    /** The cache that was returned. */
    private CompositeCache<?, ?> cache;

    /** Properties with which this manager was configured. This is exposed for other managers. */
    private Properties configurationProperties;

    /**
	 * @see org.apache.commons.jcs3.engine.behavior.IShutdownObservable#deregisterShutdownObserver(org.apache.commons.jcs3.engine.behavior.IShutdownObserver)
	 */
	@Override
	public void deregisterShutdownObserver(final IShutdownObserver observer)
	{
		// Do nothing
	}

    @Override
    public <K, V> AuxiliaryCache<K, V> getAuxiliaryCache(final String auxName, final String cacheName)
    {
        return null;
    }

    /**
     * @return the cache.
     */
    public CompositeCache<?, ?> getCache()
    {
        return cache;
    }

    /**
     * @param cacheName
     * @return a CompositeCache
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> CompositeCache<K, V> getCache( final String cacheName )
    {
        if ( cache == null )
        {
//            System.out.println( "Creating mock cache" );
            final CompositeCache<K, V> newCache =
                new CompositeCache<>( new CompositeCacheAttributes(), new ElementAttributes() );
            setCache( newCache );
        }

        return (CompositeCache<K, V>)cache;
    }

    /**
     * This is exposed so other manager can get access to the props.
     *
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

    /**
	 * @see org.apache.commons.jcs3.engine.behavior.IShutdownObservable#registerShutdownObserver(org.apache.commons.jcs3.engine.behavior.IShutdownObserver)
	 */
	@Override
	public void registerShutdownObserver(final IShutdownObserver observer)
	{
		// Do nothing
	}

	/**
     * @param cache The cache to set.
     */
    public void setCache( final CompositeCache<?, ?> cache )
    {
        this.cache = cache;
    }

	/**
     * This is exposed so other manager can get access to the props.
     *
     * @param props
     */
    public void setConfigurationProperties( final Properties props )
    {
        this.configurationProperties = props;
    }
}
