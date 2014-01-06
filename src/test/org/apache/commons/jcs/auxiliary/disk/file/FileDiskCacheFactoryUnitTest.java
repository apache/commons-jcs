package org.apache.commons.jcs.auxiliary.disk.file;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/** Verify that the factory works */
public class FileDiskCacheFactoryUnitTest
    extends TestCase
{
    /** Verify that we can get a cache from the manager via the factory */
    public void testCreateCache_Normal()
    {
        // SETUP
        String cacheName = "testCreateCache_Normal";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/FileDiskCacheFactoryUnitTest" );

        ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();

        FileDiskCacheFactory factory = new FileDiskCacheFactory();

        // DO WORK
        FileDiskCache<String, String> result = factory.createCache( cattr, cacheMgr, cacheEventLogger,
                                                                    elementSerializer );

        // VERIFY
        assertNotNull( "Should have a disk cache", result );
        assertEquals( "Should have a disk cache with a serializer", elementSerializer, result.getElementSerializer() );
    }
}
