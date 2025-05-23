package org.apache.commons.jcs3.auxiliary;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;

/**
 * All ICacheEvents are defined as final. Children must implement process events. These are wrapped
 * in event log parent calls.
 *
 * You can override the public method, but if you don't, the default will call getWithTiming.
 */
public abstract class AbstractAuxiliaryCacheEventLogging<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /**
     * Synchronously dispose the remote cache; if failed, replace the remote handle with a zombie.
     *
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        disposeWithEventLogging();
    }

    /**
     * Synchronously dispose the remote cache; if failed, replace the remote handle with a zombie.
     * Wraps the removeAll in event logs.
     *
     * @throws IOException
     */
    protected final void disposeWithEventLogging()
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( getCacheName(), "none", ICacheEventLogger.DISPOSE_EVENT );
        try
        {
            processDispose();
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets the item from the cache.
     *
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final K key )
        throws IOException
    {
        return getWithEventLogging( key );
    }

    /**
     * Gets items from the cache matching the given pattern. Items from memory will replace those
     * from remote sources.
     *
     * This only works with string keys. It's too expensive to do a toString on every key.
     *
     * Auxiliaries will do their best to handle simple expressions. For instance, the JDBC disk
     * cache will convert * to % and . to _
     *
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String pattern )
        throws IOException
    {
        return getMatchingWithEventLogging( pattern );
    }

    /**
     * Gets matching items from the cache based on the given pattern.
     *
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    protected final Map<K, ICacheElement<K, V>> getMatchingWithEventLogging( final String pattern )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( getCacheName(), pattern, ICacheEventLogger.GETMATCHING_EVENT );
        try
        {
            return processGetMatching( pattern );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
        throws IOException
    {
        return getMultipleWithEventLogging( keys );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    protected final Map<K, ICacheElement<K, V>> getMultipleWithEventLogging(final Set<K> keys )
        throws IOException
    {
        final ICacheEvent<Serializable> cacheEvent = createICacheEvent( getCacheName(), (Serializable) keys,
                                                    ICacheEventLogger.GETMULTIPLE_EVENT );
        try
        {
            return processGetMultiple( keys );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets the item from the cache. Wrapped in logging.
     *
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    protected final ICacheElement<K, V> getWithEventLogging( final K key )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( getCacheName(), key, ICacheEventLogger.GET_EVENT );
        try
        {
            return processGet( key );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Specific implementation of dispose.
     *
     * @throws IOException
     */
    protected abstract void processDispose()
        throws IOException;

    /**
     * Implements get.
     *
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    protected abstract ICacheElement<K, V> processGet( K key )
        throws IOException;

    /**
     * Implements getMatching.
     *
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    protected abstract Map<K, ICacheElement<K, V>> processGetMatching( String pattern )
        throws IOException;

    /**
     * Specific implementation of remove.
     *
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    protected abstract boolean processRemove( K key )
        throws IOException;

    /**
     * Specific implementation of removeAll.
     *
     * @throws IOException
     */
    protected abstract void processRemoveAll()
        throws IOException;

    /**
     * Implements put.
     *
     * @param cacheElement
     * @throws IOException
     */
    protected abstract void processUpdate( ICacheElement<K, V> cacheElement )
        throws IOException;

    /**
     * Removes the item from the cache. Wraps the remove in event logs.
     *
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    @Override
    public boolean remove( final K key )
        throws IOException
    {
        return removeWithEventLogging( key );
    }

    /**
     * Removes all from the region. Wraps the removeAll in event logs.
     *
     * @throws IOException
     */
    @Override
    public void removeAll()
        throws IOException
    {
        removeAllWithEventLogging();
    }

    /**
     * Removes all from the region. Wraps the removeAll in event logs.
     *
     * @throws IOException
     */
    protected final void removeAllWithEventLogging()
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( getCacheName(), "all", ICacheEventLogger.REMOVEALL_EVENT );
        try
        {
            processRemoveAll();
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Removes the item from the cache. Wraps the remove in event logs.
     *
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    protected final boolean removeWithEventLogging( final K key )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( getCacheName(), key, ICacheEventLogger.REMOVE_EVENT );
        try
        {
            return processRemove( key );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Puts an item into the cache.
     *
     * @param cacheElement
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> cacheElement )
        throws IOException
    {
        updateWithEventLogging( cacheElement );
    }

    /**
     * Puts an item into the cache. Wrapped in logging.
     *
     * @param cacheElement
     * @throws IOException
     */
    protected final void updateWithEventLogging( final ICacheElement<K, V> cacheElement )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( cacheElement, ICacheEventLogger.UPDATE_EVENT );
        try
        {
            processUpdate( cacheElement );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }
}
