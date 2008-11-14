package org.apache.jcs.auxiliary.disk.block;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;

/** Unit tests for the Block Disk Cache */
public class BlockDiskCacheUnitTest
    extends TestCase
{
    /**
     * Test the basic get matching.
     * <p>
     * @throws Exception
     */
    public void testPutGetMatching_SmallWait()
        throws Exception
    {
        // SETUP
        int items = 200;

        String cacheName = "testPutGetMatching_SmallWait";
        BlockDiskCacheAttributes cattr = new BlockDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/BlockDiskCacheUnitTest" );
        BlockDiskCache diskCache = new BlockDiskCache( cattr );

        // DO WORK
        for ( int i = 0; i <= items; i++ )
        {
            diskCache.update( new CacheElement( cacheName, i + ":key", cacheName + " data " + i ) );
        }
        Thread.sleep( 500 );

        Map matchingResults = diskCache.getMatching( "1.8.+" );

        // VERIFY
        assertEquals( "Wrong number returned", 10, matchingResults.size() );
        //System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        //System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }

    /**
     * Test the basic get matching. With no wait this will all come from purgatory.
     * <p>
     * @throws Exception
     */
    public void testPutGetMatching_NoWait()
        throws Exception
    {
        // SETUP
        int items = 200;

        String cacheName = "testPutGetMatching_NoWait";
        BlockDiskCacheAttributes cattr = new BlockDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/BlockDiskCacheUnitTest" );
        BlockDiskCache diskCache = new BlockDiskCache( cattr );

        // DO WORK
        for ( int i = 0; i <= items; i++ )
        {
            diskCache.update( new CacheElement( cacheName, i + ":key", cacheName + " data " + i ) );
        }

        Map matchingResults = diskCache.getMatching( "1.8.+" );

        // VERIFY
        assertEquals( "Wrong number returned", 10, matchingResults.size() );
        //System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        //System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }
}
