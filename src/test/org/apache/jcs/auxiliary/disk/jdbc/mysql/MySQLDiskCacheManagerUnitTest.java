package org.apache.jcs.auxiliary.disk.jdbc.mysql;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.MockCacheEventLogger;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.MockElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

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

        MySQLDiskCacheManager manager = MySQLDiskCacheManager.getInstance( defaultCacheAttributes, cacheEventLogger,
                                                                           elementSerializer );

        // DO WORK
        MySQLDiskCache cache = (MySQLDiskCache) manager.getCache( cacheName );

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
    }
}
