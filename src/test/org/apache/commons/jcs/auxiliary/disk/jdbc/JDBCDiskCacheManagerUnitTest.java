package org.apache.commons.jcs.auxiliary.disk.jdbc;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.auxiliary.disk.jdbc.JDBCDiskCache;
import org.apache.commons.jcs.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;
import org.apache.commons.jcs.auxiliary.disk.jdbc.JDBCDiskCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/** Unit tests for the manager */
public class JDBCDiskCacheManagerUnitTest
    extends TestCase
{
    /** Verify that the disk cache has the event logger */
    public void testGetCache_normal()
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        JDBCDiskCacheAttributes defaultCacheAttributes = new JDBCDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/JDBCDiskCacheManagerUnitTest" );

        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();

        JDBCDiskCacheManager manager = JDBCDiskCacheManager.getInstance( defaultCacheAttributes, CompositeCacheManager
            .getUnconfiguredInstance(), cacheEventLogger, elementSerializer );

        // DO WORK
        JDBCDiskCache<String, String> cache = manager.getCache( cacheName );

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
    }
}
