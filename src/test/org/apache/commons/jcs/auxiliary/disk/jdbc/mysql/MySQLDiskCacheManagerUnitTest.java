package org.apache.commons.jcs.auxiliary.disk.jdbc.mysql;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/** Unit tests for the manager */
public class MySQLDiskCacheManagerUnitTest
    extends TestCase
{
    /** Verify that the disk cache has the event logger */
    public void testGetCache_normal()
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        MySQLDiskCacheAttributes defaultCacheAttributes = new MySQLDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/JDBCDiskCacheManagerUnitTest" );

        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();

        MySQLDiskCacheManager manager = MySQLDiskCacheManager.getInstance( defaultCacheAttributes, CompositeCacheManager.getUnconfiguredInstance(), cacheEventLogger,
                                                                           elementSerializer );

        // DO WORK
        MySQLDiskCache<String, String> cache = manager.getCache( cacheName );

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
    }
}
