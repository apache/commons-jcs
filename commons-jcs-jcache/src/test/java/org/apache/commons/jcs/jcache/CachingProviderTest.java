package org.apache.commons.jcs.jcache;

import static org.junit.Assert.assertNotNull;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.junit.Test;

public class CachingProviderTest
{
    @Test
    public void findProvider()
    {
        assertNotNull(Caching.getCachingProvider());
    }

    @Test
    public void createCacheMgr()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        assertNotNull(cachingProvider.getCacheManager());
        cachingProvider.close();
    }
}
