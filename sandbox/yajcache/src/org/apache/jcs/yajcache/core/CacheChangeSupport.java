/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jcs.yajcache.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.jcs.yajcache.annotate.*;
import org.apache.jcs.yajcache.event.CacheChangeEvent;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
public class CacheChangeSupport<V> {
    private final @NonNullable List<CacheChangeListener<V>> listeners 
            = new CopyOnWriteArrayList<CacheChangeListener<V>>();
    
    private ICache<V> cache;
    
    public CacheChangeSupport(@NonNullable ICache<V> cache) {
        this.cache = cache;
    }
    public void addCacheChangeListener(@NonNullable CacheChangeListener<V> listener)
    {
        listeners.add(listener);
    }
    public void removeCacheChangeListener(@NonNullable CacheChangeListener<V> listener) 
    {
        listeners.remove(listener);
    }
    public @NonNullable Iterable<CacheChangeListener<V>> getCacheChangeListeners() 
    {
        return listeners;
    }
    public void fireCacheChange(@NonNullable CacheChangeEvent<V> evt) 
    {
        for (CacheChangeListener<V> listener : this.listeners) {
            listener.cacheChange(evt);
        }
    }
}
