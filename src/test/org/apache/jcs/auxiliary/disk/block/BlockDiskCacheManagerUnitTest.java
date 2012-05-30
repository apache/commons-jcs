package org.apache.jcs.auxiliary.disk.block;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.MockCacheEventLogger;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.MockElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/** Unit tests for the manager */
public class BlockDiskCacheManagerUnitTest
    extends TestCase
{
    /** Verify that the disk cache has the event logger */
    public void testGetCache_normal()
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        BlockDiskCacheAttributes defaultCacheAttributes = new BlockDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/BlockDiskCacheManagerUnitTest" );

        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();

        BlockDiskCacheManager manager = BlockDiskCacheManager.getInstance( defaultCacheAttributes, cacheEventLogger,
                                                                           elementSerializer );

        // DO WORK
        AuxiliaryCache<String, String> auxcache = manager.getCache(cacheName);
        BlockDiskCache<String, String> cache = (BlockDiskCache<String, String>) auxcache;

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
    }
}
