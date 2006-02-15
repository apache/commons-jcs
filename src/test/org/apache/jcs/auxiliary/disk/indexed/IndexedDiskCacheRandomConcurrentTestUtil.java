package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.access.TestCacheAccess;

/**
 * This is used by other tests to generate a random load on the disk cache.
 */
public class IndexedDiskCacheRandomConcurrentTestUtil
    extends TestCase
{

    /**
     * Constructor for the TestDiskCache object.
     * 
     * @param testName
     */
    public IndexedDiskCacheRandomConcurrentTestUtil( String testName )
    {
        super( testName );
    }

    /**
     * Randomly adds items to cache, gets them, and removes them. The range
     * count is more than the size of the memory cache, so items should spool to
     * disk.
     * 
     * @param region
     *            Name of the region to access
     * @param range
     * @param numOps
     * @param testNum
     * 
     * @exception Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region, int range, int numOps, int testNum )
        throws Exception
    {
        // run a rondom operation test to detect deadlocks
        TestCacheAccess tca = new TestCacheAccess( "/TestDiskCacheCon.ccf" );
        tca.setRegion( region );
        tca.random( range, numOps );

        // make sure a simple put then get works
        // this may fail if the other tests are flooding the disk cache
        JCS jcs = JCS.getInstance( region );
        String key = "testKey" + testNum;
        String data = "testData" + testNum;
        jcs.put( key, data );
        String value = (String) jcs.get( key );
        assertEquals( data, value );

    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

}
