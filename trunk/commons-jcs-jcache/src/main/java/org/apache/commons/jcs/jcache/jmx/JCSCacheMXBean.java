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
package org.apache.commons.jcs.jcache.jmx;

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.management.CacheMXBean;

public class JCSCacheMXBean<K, V> implements CacheMXBean
{
    private final Cache<K, V> delegate;

    public JCSCacheMXBean(final Cache<K, V> delegate)
    {
        this.delegate = delegate;
    }

    private Configuration<K, V> config()
    {
        return delegate.getConfiguration(Configuration.class);
    }

    private CompleteConfiguration<K, V> completeConfig()
    {
        return delegate.getConfiguration(CompleteConfiguration.class);
    }

    @Override
    public String getKeyType()
    {
        return config().getKeyType().getName();
    }

    @Override
    public String getValueType()
    {
        return config().getValueType().getName();
    }

    @Override
    public boolean isReadThrough()
    {
        try
        {
            return completeConfig().isReadThrough();
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isWriteThrough()
    {
        try
        {
            return completeConfig().isWriteThrough();
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isStoreByValue()
    {
        return config().isStoreByValue();
    }

    @Override
    public boolean isStatisticsEnabled()
    {
        try
        {
            return completeConfig().isStatisticsEnabled();
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isManagementEnabled()
    {
        try
        {
            return completeConfig().isManagementEnabled();
        }
        catch (final Exception e)
        {
            return false;
        }
    }
}
