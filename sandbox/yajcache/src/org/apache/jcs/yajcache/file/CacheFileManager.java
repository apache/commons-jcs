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

package org.apache.jcs.yajcache.file;

import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;
import org.apache.jcs.yajcache.core.ICacheChangeListener;
import org.apache.jcs.yajcache.event.CacheChangeEvent;

import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.core.ICache;
import org.apache.jcs.yajcache.core.ICacheChangeHandler;
/**
 *
 * Handle cache change events by persisting to or removing from the file system.
 * @author Hanson Char
 */
@CopyRightApache
public class CacheFileManager<V> 
        implements ICacheChangeListener<V>, ICacheChangeHandler<V>
{
    private ICache<V> cache;
    
    public CacheFileManager(ICache<V> cache) {
        this.cache = cache;
        // Creates the file directory for the cache
        CacheFileUtils.inst.mkCacheDirs(cache.getName());
    }
    @Implements(ICacheChangeListener.class)
    public void cacheChange(@NonNullable CacheChangeEvent<V> event) {
        event.dispatch(this);
    }
    
    @Implements(ICacheChangeHandler.class)
    public boolean handlePut(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value) 
    {
        if (value instanceof Serializable) {
            byte[] ba = SerializationUtils.serialize((Serializable)value);
            CacheFileDAO.inst.writeCacheItem(
                cacheName, CacheFileContentType.JAVA_SERIALIZATION, key, ba);
            return true;
        }
        return false;
    }
    @Implements(ICacheChangeHandler.class)
    public boolean handleRemove(
            @NonNullable String cacheName, @NonNullable String key) 
    {
        return CacheFileDAO.inst.removeCacheItem(cacheName, key);
    }
    @Implements(ICacheChangeHandler.class)
    public boolean handleClear(@NonNullable String cacheName) {
        return CacheFileUtils.inst.rmCacheDir(cacheName);
    }
    
    @TODO("Avoid multiple serialization for the same put")
    @Implements(ICacheChangeHandler.class)
    public boolean handlePutCopy(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value) 
    {
        return false;
    }
    @TODO("Avoid multiple serialization for the same put")
    @Implements(ICacheChangeHandler.class)
    public boolean handlePutBeanCopy(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value) 
    {
        return false;
    }
    @TODO("Avoid multiple serialization for the same put")
    @Implements(ICacheChangeHandler.class)
    public boolean handlePutBeanClone(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value) 
    {
        return false;
    }
}
