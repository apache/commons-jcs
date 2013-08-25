package org.apache.commons.jcs.engine.memory.fifo;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.memory.fifo.FIFOMemoryCache;

/** Unit tests for the fifo implementation. */
public class FIFOMemoryCacheUnitTest
    extends TestCase
{
    /**
     * Verify that the oldest inserted item is removed
     * <p>
     * @throws IOException
     */
    public void testExpirationPolicy_oneExtra()
        throws IOException
    {
        // SETUP
        int maxObjects = 10;
        String cacheName = "testExpirationPolicy_oneExtra";

        ICompositeCacheAttributes attributes = new CompositeCacheAttributes();
        attributes.setMaxObjects( maxObjects );
        attributes.setSpoolChunkSize( 1 );

        FIFOMemoryCache<String, String> cache = new FIFOMemoryCache<String, String>();
        cache.initialize( new CompositeCache<String, String>( cacheName, attributes,
                                              new ElementAttributes() ) );

        for ( int i = 0; i <= maxObjects; i++ )
        {
            CacheElement<String, String> element = new CacheElement<String, String>( cacheName, "key" + i, "value" + i );
            cache.update( element );
        }

        CacheElement<String, String> oneMoreElement = new CacheElement<String, String>( cacheName, "onemore", "onemore" );

        // DO WORK
        cache.update( oneMoreElement );

        // VERIFY
        assertEquals( "Should have max elements", maxObjects, cache.getSize() );
        for ( int i = maxObjects; i > maxObjects; i-- )
        {
            assertNotNull( "Shjould have elemnt " + i, cache.get( "key" + i ) );
        }
        assertNotNull( "Shjould have oneMoreElement", cache.get( "onemore" ) );
    }

    /**
     * Verify that the oldest inserted item is removed
     * <p>
     * @throws IOException
     */
    public void testExpirationPolicy_doubleOver()
        throws IOException
    {
        // SETUP
        int maxObjects = 10;
        String cacheName = "testExpirationPolicy_oneExtra";

        ICompositeCacheAttributes attributes = new CompositeCacheAttributes();
        attributes.setMaxObjects( maxObjects );
        attributes.setSpoolChunkSize( 1 );

        FIFOMemoryCache<String, String> cache = new FIFOMemoryCache<String, String>();
        cache.initialize( new CompositeCache<String, String>( cacheName, attributes,
                                              new ElementAttributes() ) );

        // DO WORK
        for ( int i = 0; i <= (maxObjects * 2); i++ )
        {
            CacheElement<String, String> element = new CacheElement<String, String>( cacheName, "key" + i, "value" + i );
            cache.update( element );
        }

        // VERIFY
        assertEquals( "Should have max elements", maxObjects, cache.getSize() );
        for ( int i = (maxObjects * 2); i > maxObjects; i-- )
        {
            assertNotNull( "Shjould have elemnt " + i, cache.get( "key" + i ) );
        }
    }
}
