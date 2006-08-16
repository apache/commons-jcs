package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Tests for common functionality.
 * <p>
 * @author Aaron Smuts
 */
public class IndexDiskCacheUnitTest
    extends TestCase
{

    /**
     * Simply verify that we can put items in the disk cache and retrieve them.
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

    /**
     * Verify that we don't override the largest item.
     */
    public void testRecycleBin()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemoveItems" );
        cattr.setMaxRecycleBinSize( 2 );
        cattr.setOptimizeAtRemoveCount( 7 );
        cattr.setMaxKeySize( 5 );
        cattr.setMaxPurgatorySize( 0 );
        cattr.setDiskPath( "target/test-sandbox/BreakIndexTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        String[] test = { "a", "bb", "ccc", "dddd", "eeeee", "ffffff", "ggggggg", "hhhhhhhhh", "iiiiiiiiii" };
        String[] expect = { null, "bb", "ccc", null, null, "ffffff", null, "hhhhhhhhh", "iiiiiiiiii" };

        System.out.println( "------------------------- testRecycleBin " );

        for ( int i = 0; i < 6; i++ )
        {
            ICacheElement element = new CacheElement( "testRecycleBin", "key:" + test[i], test[i] );
            System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
            disk.doUpdate( element );
        }

        for ( int i = 3; i < 5; i++ )
        {
            System.out.println( "About to remove " + "key:" + test[i] + " i = " + i );
            disk.remove( "key:" + test[i] );
        }

        // there was a bug where 7 would try to be put in the empty slot left by 4's removal, but it
        // will not fit.
        for ( int i = 7; i < 9; i++ )
        {
            ICacheElement element = new CacheElement( "testRecycleBin", "key:" + test[i], test[i] );
            System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
            disk.doUpdate( element );
        }

        try
        {
            for ( int i = 0; i < 9; i++ )
            {
                ICacheElement element = disk.get( "key:" + test[i] );
                if ( element != null )
                {
                    System.out.println( "element = " + element.getVal() );
                }
                else
                {
                    System.out.println( "null --" + "key:" + test[i] );
                }

                String expectedValue = expect[i];
                if ( expectedValue == null )
                {
                    assertNull( "Expected a null element", element );
                }
                else
                {
                    assertNotNull( "The element for key [" + "key:" + test[i] + "] should not be null. i = " + i,
                                   element );
                    assertEquals( "Elements contents do not match expected", element.getVal(), expectedValue );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Should not get an exception: " + e.toString() );
        }

        disk.removeAll();
    }

}
