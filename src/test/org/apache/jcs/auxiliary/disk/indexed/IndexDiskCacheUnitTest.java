package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Tests for common functionality.
 * 
 * @author Aaron Smuts
 * 
 */
public class IndexDiskCacheUnitTest
    extends TestCase
{

    /**
     * Simply verify that we can put items in the disk cache and retrieve them.
     * 
     */
    public void testSimplePutAndGet()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testSimplePutAndGet" );
        cattr.setMaxKeySize( 1000 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 999;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testSimplePutAndGet", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( "key:" + i );
            assertNotNull( "Should have recevied an element.", element );
            assertEquals( "Element is wrong.", "data:" + i, element.getVal() );
        }
        
        System.out.println( disk.getStats() );
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     * 
     */
    public void testRemoveItems()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemoveItems" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testRemoveItems", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        // remove each
        for ( int i = 0; i < cnt; i++ )
        {
            disk.remove( "key:" + i );
            ICacheElement element = disk.doGet( "key:" + i );
            assertNull( "Should not have recevied an element.", element );
        }
    }

}
