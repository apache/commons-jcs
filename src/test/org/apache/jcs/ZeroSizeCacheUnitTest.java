package org.apache.jcs;

import junit.framework.TestCase;

/**
 * 
 * @author Aaron Smuts
 *  
 */
public class ZeroSizeCacheUnitTest
    extends TestCase
{

    private static int items = 20000;

    /**
     * Test setup
     */
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestZeroSizeCache.ccf" );
        JCS.getInstance( "testCache1" );
    }

    /**
     * Verify that a 0 size cache does not result in errors. You should be able
     * to disable a region this way.
     * @throws Exception 
     *  
     */
    public void testPutGetRemove()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "testCache1" );

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        // all the gets should be null
        for ( int i = items; i >= 0; i-- )
        {
            String res = (String) jcs.get( i + ":key" );
            if ( res == null )
            {
                assertNull( "[" + i + ":key] should be null", res );
            }
        }

        // test removal, should be no exceptions
        jcs.remove( "300:key" );

        // allow the shrinker to run
        Thread.sleep( 500 );

        // do it again.
        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = items; i >= 0; i-- )
        {
            String res = (String) jcs.get( i + ":key" );
            if ( res == null )
            {
                assertNull( "[" + i + ":key] should be null", res );
            }
        }

        System.out.println( jcs.getStats() );

    }

}
