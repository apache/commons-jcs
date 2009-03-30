package org.apache.jcs.auxiliary.disk.indexed;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.MockCacheEventLogger;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.MockElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.timing.SleepUtil;

/** Unit tests for the manager */
public class IndexedDiskCacheManagerUnitTest
    extends TestCase
{
    /**
     * Verify that the disk cache has the event logger
     * @throws IOException
     */
    public void testGetCache_normal()
        throws IOException
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        IndexedDiskCacheAttributes defaultCacheAttributes = new IndexedDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/IndexedDiskCacheManagerUnitTest" );

        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        MockElementSerializer elementSerializer = new MockElementSerializer();

        String key = "myKey";
        ICacheElement cacheElement = new CacheElement( "test", key, "MyValue" );

        IndexedDiskCacheManager manager = IndexedDiskCacheManager.getInstance( defaultCacheAttributes,
                                                                               cacheEventLogger, elementSerializer );

        // DO WORK
        IndexedDiskCache cache = (IndexedDiskCache) manager.getCache( cacheName );

        cache.update( cacheElement );        
        SleepUtil.sleepAtLeast( 100 );        
        cache.get( key );

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
        assertEquals( "Wrong serialize count", elementSerializer.serializeCount, 1 );
        assertEquals( "Wrong deSerialize count", elementSerializer.deSerializeCount, 1 );
    }
}
