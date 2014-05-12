package org.apache.commons.jcs.jcache;

import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertFalse;

public class ImmediateExpiryTest
{
    @Test
    public void immediate()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default",
                new MutableConfiguration<Object, Object>()
                        .setExpiryPolicyFactory(
                                new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new CreatedExpiryPolicy(Duration.ZERO))));
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", "bar");
        assertFalse(cache.containsKey("foo"));
        cachingProvider.close();
    }
}
