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
import org.apache.commons.jcs4.engine.match.KeyMatcherPatternImpl;
import org.apache.commons.jcs4.engine.match.behavior.IKeyMatcher;
import org.apache.commons.jcs4.utils.serialization.StandardSerializer;

/** This holds convenience methods used by most auxiliary caches. */
public abstract class AbstractAuxiliaryCache<K, V>
    extends AbstractCacheEventLogSupport<K, V>
    implements AuxiliaryCache<K, V>
{
    /** The serializer. Uses a standard serializer by default. */
    private IElementSerializer elementSerializer = new StandardSerializer();

    /** Key matcher used by the getMatching API */
    private IKeyMatcher<K> keyMatcher = new KeyMatcherPatternImpl<>();

    /** Cache configuration */
    private AuxiliaryCacheAttributes auxiliaryCacheAttributes;

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
     * @return elementSerializer
     */
    public IElementSerializer getElementSerializer()
    {
        return this.elementSerializer;
    }

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
     * Returns the cache configuration.
     *
     * @return cache configuration
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return auxiliaryCacheAttributes;
    }

    /**
     * Gets the extra info for the event log.
     *
     * @return the eventLogSourceName
     */
    @Override
    protected String getEventLogSourceName()
    {
        return getAuxiliaryCacheAttributes().getName();
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

    /**
     * Set the cache configuration
     *
     * @param auxiliaryCacheAttributes the auxiliaryCacheAttributes to set
     */
    public void setAuxiliaryCacheAttributes(AuxiliaryCacheAttributes auxiliaryCacheAttributes)
    {
        this.auxiliaryCacheAttributes = auxiliaryCacheAttributes;
    }
}
