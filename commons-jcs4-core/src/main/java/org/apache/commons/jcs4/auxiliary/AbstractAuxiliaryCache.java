package org.apache.commons.jcs4.auxiliary;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.logging.CacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs4.engine.match.KeyMatcherPatternImpl;
import org.apache.commons.jcs4.engine.match.behavior.IKeyMatcher;
import org.apache.commons.jcs4.utils.serialization.StandardSerializer;

/** This holds convenience methods used by most auxiliary caches. */
public abstract class AbstractAuxiliaryCache<K, V>
    implements AuxiliaryCache<K, V>
{
    /** An optional event logger */
    private ICacheEventLogger cacheEventLogger;

    /** The serializer. Uses a standard serializer by default. */
    private IElementSerializer elementSerializer = new StandardSerializer();

    /** Key matcher used by the getMatching API */
    private IKeyMatcher<K> keyMatcher = new KeyMatcherPatternImpl<>();

    /**
     * Logs an event if an event logger is configured.
     *
     * @param item
     * @param eventName
     * @return ICacheEvent
     */
    protected ICacheEvent<K> createICacheEvent( final ICacheElement<K, V> item, final String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<>();
        }
        final String diskLocation = getEventLoggingExtraInfo();
        String regionName = null;
        K key = null;
        if ( item != null )
        {
            regionName = item.cacheName();
            key = item.key();
        }
        return cacheEventLogger.createICacheEvent( getAuxiliaryCacheAttributes().getName(), regionName, eventName,
                                                   diskLocation, key );
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param regionName
     * @param key
     * @param eventName
     * @return ICacheEvent
     */
    protected <T> ICacheEvent<T> createICacheEvent( final String regionName, final T key, final String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<>();
        }
        final String diskLocation = getEventLoggingExtraInfo();
        return cacheEventLogger.createICacheEvent( getAuxiliaryCacheAttributes().getName(), regionName, eventName,
                                                   diskLocation, key );

    }

    /**
     * Gets the item from the cache.
     *
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    @Override
    public abstract ICacheElement<K, V> get( K key ) throws IOException;

    /**
     * Allows it to be injected.
     *
     * @return cacheEventLogger
     */
    public ICacheEventLogger getCacheEventLogger()
    {
        return this.cacheEventLogger;
    }

    /**
     * Allows it to be injected.
     *
     * @return elementSerializer
     */
    public IElementSerializer getElementSerializer()
    {
        return this.elementSerializer;
    }

    /**
     * Gets the extra info for the event log.
     *
     * @return IP, or disk location, etc.
     */
    public abstract String getEventLoggingExtraInfo();

    /**
     * Returns the key matcher used by get matching.
     *
     * @return keyMatcher
     */
    public IKeyMatcher<K> getKeyMatcher()
    {
        return this.keyMatcher;
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    protected void logApplicationEvent( final String source, final String eventName, final String optionalDetails )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logApplicationEvent( source, eventName, optionalDetails );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param source
     * @param eventName
     * @param errorMessage
     */
    protected void logError( final String source, final String eventName, final String errorMessage )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logError( source, eventName, errorMessage );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param cacheEvent
     */
    protected <T> void logICacheEvent( final ICacheEvent<T> cacheEvent )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    protected Map<K, ICacheElement<K, V>> processGetMultiple(final Set<K> keys) throws IOException
    {
        if (keys != null)
        {
            return keys.stream()
                .map(key -> {
                    try
                    {
                        return get(key);
                    }
                    catch (final IOException e)
                    {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ICacheElement::key,
                        element -> element));
        }

        return new HashMap<>();
    }

    /**
     * Allows it to be injected.
     *
     * @param cacheEventLogger
     */
    public void setCacheEventLogger( final ICacheEventLogger cacheEventLogger )
    {
        this.cacheEventLogger = cacheEventLogger;
    }

    /**
     * Allows you to inject a custom serializer. A good example would be a compressing standard
     * serializer.
     * <p>
     * Does not allow you to set it to null.
     *
     * @param elementSerializer
     */
    public void setElementSerializer( final IElementSerializer elementSerializer )
    {
        if ( elementSerializer != null )
        {
            this.elementSerializer = elementSerializer;
        }
    }

    /**
     * Sets the key matcher used by get matching.
     *
     * @param keyMatcher
     */
    @Override
    public void setKeyMatcher( final IKeyMatcher<K> keyMatcher )
    {
        if ( keyMatcher != null )
        {
            this.keyMatcher = keyMatcher;
        }
    }
}
