package org.apache.jcs;

import junit.framework.*;

import org.apache.jcs.access.TestCacheAccess;

public class TestDiskCacheConcurrentRandom
    extends TestCase {

  /**
   * Constructor for the TestDiskCache object.
   */
  public TestDiskCacheConcurrentRandom(String testName) {
    super(testName);
  }

  /**
   * Randomly adds items to cache, gets them, and removes them.
   * The range count is more than the size of the memory cache,
   * so items should spool to disk.
   *
   * @param region Name of the region to access
   *
   * @exception Exception If an error occurs
   */
  public void runTestForRegion( String region, int range, int numOps, int testNum )
      throws Exception
  {
      // run a rondom operation test to detect deadlocks
      TestCacheAccess tca = new TestCacheAccess( "/TestDiskCacheCon.ccf" );
      tca.setRegion( region );
      tca.random( range, numOps);

      // make sure a simple put then get works
      // this may fail if the other tests are flooding the disk cache
      JCS jcs = JCS.getInstance( region );
      String key = "testKey" + testNum;
      String data = "testData" + testNum;
      jcs.put( key, data );
      String value = ( String ) jcs.get( key );
      this.assertEquals( data, value );

  }


  /**
   * Test setup
   */
  public void setUp() {
    JCS.setConfigFilename("/TestDiskCacheCon.ccf");
  }

}
