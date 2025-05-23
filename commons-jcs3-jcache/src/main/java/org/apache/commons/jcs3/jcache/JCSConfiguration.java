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
/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.jcs3.jcache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

public class JCSConfiguration<K, V> implements CompleteConfiguration<K, V>
{

    /**
     */
    private static final long serialVersionUID = 3322514800838658711L;
    private final Class<K> keyType;
    private final Class<V> valueType;
    private final boolean storeByValue;
    private final boolean readThrough;
    private final boolean writeThrough;
    private final Factory<CacheLoader<K, V>> cacheLoaderFactory;
    private final Factory<CacheWriter<? super K, ? super V>> cacheWristerFactory;
    private final Factory<ExpiryPolicy> expiryPolicyFactory;
    private final Set<CacheEntryListenerConfiguration<K, V>> cacheEntryListenerConfigurations;

    private volatile boolean statisticsEnabled;
    private volatile boolean managementEnabled;

    public JCSConfiguration(final Configuration<K, V> configuration, final Class<K> keyType, final Class<V> valueType)
    {
        this.keyType = keyType;
        this.valueType = valueType;
        if (configuration instanceof CompleteConfiguration)
        {
            final CompleteConfiguration<K, V> cConfiguration = (CompleteConfiguration<K, V>) configuration;
            storeByValue = configuration.isStoreByValue();
            readThrough = cConfiguration.isReadThrough();
            writeThrough = cConfiguration.isWriteThrough();
            statisticsEnabled = cConfiguration.isStatisticsEnabled();
            managementEnabled = cConfiguration.isManagementEnabled();
            cacheLoaderFactory = cConfiguration.getCacheLoaderFactory();
            cacheWristerFactory = cConfiguration.getCacheWriterFactory();
            this.expiryPolicyFactory = cConfiguration.getExpiryPolicyFactory();
            cacheEntryListenerConfigurations = new HashSet<>();

            final Iterable<CacheEntryListenerConfiguration<K, V>> entryListenerConfigurations = cConfiguration
                    .getCacheEntryListenerConfigurations();
            if (entryListenerConfigurations != null)
            {
                for (final CacheEntryListenerConfiguration<K, V> kvCacheEntryListenerConfiguration : entryListenerConfigurations)
                {
                    cacheEntryListenerConfigurations.add(kvCacheEntryListenerConfiguration);
                }
            }
        }
        else
        {
            expiryPolicyFactory = EternalExpiryPolicy.factoryOf();
            storeByValue = true;
            readThrough = false;
            writeThrough = false;
            statisticsEnabled = false;
            managementEnabled = false;
            cacheLoaderFactory = null;
            cacheWristerFactory = null;
            cacheEntryListenerConfigurations = new HashSet<>();
        }
    }

    public synchronized void addListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        cacheEntryListenerConfigurations.add(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations()
    {
        return Collections.unmodifiableSet(cacheEntryListenerConfigurations);
    }

    @Override
    public Factory<CacheLoader<K, V>> getCacheLoaderFactory()
    {
        return cacheLoaderFactory;
    }

    @Override
    public Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory()
    {
        return cacheWristerFactory;
    }

    @Override
    public Factory<ExpiryPolicy> getExpiryPolicyFactory()
    {
        return expiryPolicyFactory;
    }

    @Override
    public Class<K> getKeyType()
    {
        return keyType == null ? (Class<K>) Object.class : keyType;
    }

    @Override
    public Class<V> getValueType()
    {
        return valueType == null ? (Class<V>) Object.class : valueType;
    }

    @Override
    public boolean isManagementEnabled()
    {
        return managementEnabled;
    }

    @Override
    public boolean isReadThrough()
    {
        return readThrough;
    }

    @Override
    public boolean isStatisticsEnabled()
    {
        return statisticsEnabled;
    }

    @Override
    public boolean isStoreByValue()
    {
        return storeByValue;
    }

    @Override
    public boolean isWriteThrough()
    {
        return writeThrough;
    }

    public void managementDisabled()
    {
        managementEnabled = false;
    }

    public void managementEnabled()
    {
        managementEnabled = true;
    }

    public synchronized void removeListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        cacheEntryListenerConfigurations.remove(cacheEntryListenerConfiguration);
    }

    public void statisticsDisabled()
    {
        statisticsEnabled = false;
    }

    public void statisticsEnabled()
    {
        statisticsEnabled = true;
    }
}
