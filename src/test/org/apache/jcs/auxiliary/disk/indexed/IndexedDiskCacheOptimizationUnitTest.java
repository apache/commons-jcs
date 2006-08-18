package org.apache.jcs.auxiliary.disk.indexed;

import java.io.Serializable;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
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

        int numberToInsert = removeCount * 2;

        int[] sizes = new int[numberToInsert];
        Random random = new Random( 89 );
        for ( int i = 0; i < numberToInsert; i++ )
        {
            int bytes = random.nextInt( 20 );
            // 4-24 KB
            int size = ( bytes + 4 ) * 1024;
            sizes[i] = size;
            Tile tile = new Tile( new Integer( i ), new byte[size] );
            // images

            ICacheElement element = new CacheElement( cattr.getCacheName(), tile.id, tile );
            disk.doUpdate( element );
        }

        Thread.sleep( 1000 );
        long sizeBeforeRemove = disk.getDataFileSize();
        System.out.println( "file sizeBeforeRemove " + sizeBeforeRemove );
        System.out.println( "totalSize inserted " + totalSize( sizes, numberToInsert ) );

        for ( int i = 0; i < removeCount; i++ )
        {
            disk.doRemove( new Integer( i ) );
        }

        Thread.sleep( 100 );
        Thread.yield();
        Thread.sleep( 100 );
        long sizeAfterRemove = disk.getDataFileSize();
        System.out.println( "file sizeAfterRemove " + sizeAfterRemove );
        System.out.println( "totalSize expected after remove " + totalSize( sizes, removeCount ) );

        assertTrue( "The post optimization size should be smaller.", sizeAfterRemove < sizeBeforeRemove );

        long reality = Math.abs( totalSize( sizes, removeCount ) - sizeAfterRemove );
        assertTrue( "The file size should be within 15% of the expected size. reality = " + reality,
                    reality < (sizeAfterRemove * 1.15 ) - sizeAfterRemove );
        // TODO figure out the estimated size purportion.
    }

    /**
     * Total from the start to the endPostion.
     * <p>
     * @param sizes
     * @param endPosition
     * @return size
     */
    private long totalSize( int[] sizes, int endPosition )
    {
        long total = 0;
        for ( int i = 0; i < endPosition; i++ )
        {
            total += sizes[i];
        }
        return total;
    }

    /**
     * Resembles a cached image.
     */
    private static class Tile
        implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         * Key
         */
        public Integer id;

        /**
         * Byte size
         */
        public byte[] imageBytes;

        /**
         * @param id
         * @param imageBytes
         */
        public Tile( Integer id, byte[] imageBytes )
        {
            this.id = id;
            this.imageBytes = imageBytes;
        }
    }
}
