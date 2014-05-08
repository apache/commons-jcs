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
package org.apache.commons.jcs.jcache.extras.writer;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;

public abstract class CacheWriterAdapter<K, V> implements CacheWriter<K, V>, Factory<CacheWriter<K, V>>
{
    @Override
    public void writeAll(final Collection<Cache.Entry<? extends K, ? extends V>> entries) throws CacheWriterException
    {
        for (final Cache.Entry<? extends K, ? extends V> entry : entries)
        {
            write(entry);
        }
    }

    @Override
    public void deleteAll(final Collection<?> keys) throws CacheWriterException
    {
        for (final Object k : keys)
        {
            delete(k);
        }
    }

    @Override
    public CacheWriter<K, V> create()
    {
        return this;
    }
}
