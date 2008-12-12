package org.apache.jcs.auxiliary.remote.http.client;

import junit.framework.TestCase;

import org.apache.jcs.JCS;

/** Manual tester for a JCS instance configured to use the http client. */
public class RemoteHttpCacheManualTester
    extends TestCase
{
    /** number to use for the test */
    private static int items = 100;

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestRemoteHttpCache.ccf" );
    }

    /**
     * A unit test for JUnit
     * @exception Exception Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "testCache1" );

        jcs.put( "TestKey", "TestValue" );

        System.out.println( jcs.getStats() );

        for ( int i = 1; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = items; i > 0; i-- )
        {
            String res = (String) jcs.get( i + ":key" );
            if ( res == null )
            {
                //assertNotNull( "[" + i + ":key] should not be null", res );
            }
        }

        // test removal
        jcs.remove( "300:key" );
        assertNull( jcs.get( "TestKey" ) );

        System.out.println( jcs.getStats() );
    }
}
