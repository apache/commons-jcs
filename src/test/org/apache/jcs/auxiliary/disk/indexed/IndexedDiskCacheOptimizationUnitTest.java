package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * @author Aaron Smuts
 */
public class IndexedDiskCacheOptimizationUnitTest
    extends TestCase
{

    /**
     * Set the optimize at remove count to 10. Add 20. Check the file size. Remove 10. Check the
     * times optimized. Check the file size.
     * @throws Exception
     */
    public void testBasicOptimization()
        throws Exception
    {
        int removeCount = 50;

        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testOptimization" );
        cattr.setMaxKeySize( removeCount * 3 );
        cattr.setOptimizeAtRemoveCount( removeCount );
        cattr.setMaxRecycleBinSize( removeCount * 3 );
        cattr.setDiskPath( "target/test-sandbox/testOptimization" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.removeAll();

        int numberToInsert = removeCount * 2;
        ICacheElement[] elements = DiskTestObjectUtil
            .createCacheElementsWithTestObjectsOfVariableSizes( numberToInsert, cattr.getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.sleep( 1000 );
        long sizeBeforeRemove = disk.getDataFileSize();
        System.out.println( "file sizeBeforeRemove " + sizeBeforeRemove );
        System.out.println( "totalSize inserted " + DiskTestObjectUtil.totalSize( elements, numberToInsert ) );

        for ( int i = 0; i < removeCount; i++ )
        {
            disk.doRemove( new Integer( i ) );
        }

        Thread.sleep( 100 );
        Thread.yield();
        Thread.sleep( 100 );
        long sizeAfterRemove = disk.getDataFileSize();
        System.out.println( "file sizeAfterRemove " + sizeAfterRemove );
        long expectedSizeAfterRemove = DiskTestObjectUtil.totalSize( elements, removeCount, elements.length );
        System.out.println( "totalSize expected after remove " + expectedSizeAfterRemove );

        assertTrue( "The post optimization size should be smaller.", sizeAfterRemove < sizeBeforeRemove );

        assertEquals( "The file size is not as expected size.", expectedSizeAfterRemove, sizeAfterRemove );
    }
}
