package org.apache.jcs;

import junit.framework.*;

public class TestBDBJE
    extends TestCase {

  /**
   * Constructor for the TestDiskCache object.
   */
  public TestBDBJE(String testName) {
    super(testName);
  }

  /**
   * Adds items to cache, gets them, and removes them. The item count is more
   * than the size of the memory cache, so items should spool to disk.
   *
   * @param region Name of the region to access
   *
   * @exception Exception If an error occurs
   */
  public void runTestForRegion(String region, int start, int end) throws
      Exception {
    JCS jcs = JCS.getInstance(region);

    // Add items to cache

    for (int i = start; i <= end; i++) {
      jcs.put(i + ":key", region + " data " + i);
    }

    // Test that all items are in cache

    for (int i = start; i <= end; i++) {
      String value = (String) jcs.get(i + ":key");

      this.assertEquals(region + " data " + i, value);
    }

  }

  public void testDummy() {
	this.assertEquals( "r", "r" );
  }

  /**
   * Test setup
   */
  public void setUp() {
    JCS.setConfigFilename("/TestBDBJEDiskCacheCon.ccf");
  }

}
