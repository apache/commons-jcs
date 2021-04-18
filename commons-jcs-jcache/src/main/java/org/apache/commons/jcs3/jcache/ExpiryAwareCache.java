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
package org.apache.commons.jcs3.jcache;

import java.util.Collections;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.event.EventType;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;

// allows us to plug some lifecycle callbacks on the core cache without impacting too much the core
public class ExpiryAwareCache<A, B> extends CompositeCache<A, B>
{
    private Map<CacheEntryListenerConfiguration<A, B>, JCSListener<A, B>> listeners;
    private Cache<A, B> cacheRef;

    ExpiryAwareCache(final ICompositeCacheAttributes cattr, final IElementAttributes attr)
    {
        super(cattr, attr);
    }

    @Override
    protected void doExpires(final ICacheElement<A, B> element)
    {
        super.doExpires(element);
        for (final JCSListener<A, B> listener : listeners.values())
        {
            listener.onExpired(Collections.singletonList(new JCSCacheEntryEvent<>(
                    cacheRef, EventType.REMOVED, null, element.getKey(), element.getVal())));
        }
    }

    void init(final Cache<A, B> cache, final Map<CacheEntryListenerConfiguration<A, B>, JCSListener<A, B>> listeners)
    {
        this.cacheRef = cache;
        this.listeners = listeners;
    }
}
