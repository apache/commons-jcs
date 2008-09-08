package org.apache.jcs.auxiliary.disk.indexed;

import org.apache.jcs.auxiliary.MockCacheEventLogger;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.MockElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

import junit.framework.TestCase;

/** Unit tests for the manager */
public class IndexedDiskCacheManagerUnitTest
    extends TestCase
{
    /** Verify that the disk cache has the event logger */
    public void testGetCache_normal()
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        IndexedDiskCacheAttributes defaultCacheAttributes = new IndexedDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/IndexedDiskCacheManagerUnitTest" );
        
        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();
        
        IndexedDiskCacheManager manager = IndexedDiskCacheManager.getInstance( defaultCacheAttributes, cacheEventLogger, elementSerializer );
        
        // DO WORK
        IndexedDiskCache cache = (IndexedDiskCache)manager.getCache( cacheName );
        
        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger());
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer());
    }
}
