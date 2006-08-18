package org.apache.jcs.auxiliary.disk.indexed;

import java.io.IOException;

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

    /**
     * Verify that the overlap check returns true when there are no overlaps.
     */
    public void testCheckForDedOverlaps_noOverlap()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testCheckForDedOverlaps_noOverlap" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for ( int i = 0; i < numDescriptors; i++ )
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor( pos, i * 2 );
            pos = pos + ( i * 2 ) + IndexedDisk.RECORD_HEADER;
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps( sortedDescriptors );

        // VERIFY
        assertTrue( "There should be no overlap. it should be ok", result );
    }

    /**
     * Verify that the overlap check returns false when there are overlaps.
     */
    public void testCheckForDedOverlaps_overlaps()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testCheckForDedOverlaps_overlaps" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for ( int i = 0; i < numDescriptors; i++ )
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor( pos, i * 2 );
            // don't add the header + IndexedDisk.RECORD_HEADER;
            pos = pos + ( i * 2 );
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps( sortedDescriptors );

        // VERIFY
        assertFalse( "There should be overlaps. it should be not ok", result );
    }

    /**
     * Verify that the file size is as expected.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testFileSize()
        throws IOException, InterruptedException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testFileSize" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numberToInsert = 20;
        int bytes = 24;
        ICacheElement[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects( numberToInsert, bytes, cattr
            .getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        long expectedSize = DiskTestObjectUtil.totalSize( elements, numberToInsert );
        long resultSize = disk.getDataFileSize();

        System.out.println( "testFileSize stats " + disk.getStats() );

        assertEquals( "Wrong file size", expectedSize, resultSize );
    }

}
