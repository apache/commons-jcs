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
