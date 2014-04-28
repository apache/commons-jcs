package org.apache.commons.jcs.jcache;

import org.junit.Test;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertNotNull;

public class CachingProviderTest {
    @Test
    public void findProvider() {
        assertNotNull(Caching.getCachingProvider());
    }

    @Test
    public void createCacheMgr() {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        assertNotNull(cachingProvider.getCacheManager());
        cachingProvider.close();
    }
}
