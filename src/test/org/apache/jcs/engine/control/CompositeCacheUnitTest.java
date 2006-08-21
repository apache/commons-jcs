package org.apache.jcs.engine.control;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheMockImpl;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.memory.MemoryCacheMockImpl;

/**
 * Tests that directly engage the composite cache.
 * <p>
 * @author Aaron Smuts
 */
public class CompositeCacheUnitTest
    extends TestCase
{
    /**
     * Verify that the freeMemoryElements method on the memory cache is called on shutdown if there
     * is a disk cache.
     * <p>
     * @throws IOException
     */
    public void testShutdownMemoryFlush()
        throws IOException
    {
        // SETUP
        String cacheName = "testCacheName";
        String mockMemoryCacheClassName = "org.apache.jcs.engine.memory.MemoryCacheMockImpl";
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( mockMemoryCacheClassName );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( cacheName, cattr, attr );

        AuxiliaryCacheMockImpl diskMock = new AuxiliaryCacheMockImpl();
        diskMock.cacheType = ICache.DISK_CACHE;
        AuxiliaryCache[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        int numToInsert = 10;
        for ( int i = 0; i < numToInsert; i++ )
        {
            ICacheElement element = new CacheElement( cacheName, String.valueOf( i ), new Integer( i ) );
            cache.update( element, false );
        }

        cache.dispose();

        // VERIFY
        MemoryCacheMockImpl memoryCache = (MemoryCacheMockImpl) cache.getMemoryCache();
        assertEquals( "Wrong number freed.", numToInsert, memoryCache.lastNumberOfFreedElements );
    }

    /**
     * Verify that the freeMemoryElements method on the memory cache is NOT called on shutdown if
     * there is NOT a disk cache.
     * <p>
     * @throws IOException
     */
    public void testShutdownMemoryFlush_noDisk()
        throws IOException
    {
        // SETUP
        String cacheName = "testCacheName";
        String mockMemoryCacheClassName = "org.apache.jcs.engine.memory.MemoryCacheMockImpl";
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( mockMemoryCacheClassName );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( cacheName, cattr, attr );

        AuxiliaryCacheMockImpl diskMock = new AuxiliaryCacheMockImpl();
        diskMock.cacheType = ICache.REMOTE_CACHE;
        AuxiliaryCache[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        int numToInsert = 10;
        for ( int i = 0; i < numToInsert; i++ )
        {
            ICacheElement element = new CacheElement( cacheName, String.valueOf( i ), new Integer( i ) );
            cache.update( element, false );
        }

        cache.dispose();

        // VERIFY
        MemoryCacheMockImpl memoryCache = (MemoryCacheMockImpl) cache.getMemoryCache();
        assertEquals( "Wrong number freed.", 0, memoryCache.lastNumberOfFreedElements );
    }
}
