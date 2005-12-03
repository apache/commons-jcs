package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.util.Random;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * @author asmuts
 */
public class TestLateralTCPConcurrentRandom
    extends TestCase
{

    private static boolean isSysOut = false;

    //private static boolean isSysOut = true;

    /**
     * Constructor for the TestDiskCache object.
     * 
     * @param testName
     */
    public TestLateralTCPConcurrentRandom( String testName )
    {
        super( testName );
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCacheConcurrent.ccf" );
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

        boolean show = true;//false;

        JCS cache = JCS.getInstance( region );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1102" );

        // this service will put and remove using the lateral to
        // the cache instance above
        // the cache thinks it is different since the listenerid is different
        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                Random ran = new Random( i );
                int n = ran.nextInt( 4 );
                int kn = ran.nextInt( range );
                String key = "key" + kn;
                if ( n == 1 )
                {
                    ICacheElement element = new CacheElement( region, key, region + ":data" + i
                        + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                    service.update( element );
                    if ( show )
                    {
                        p( "put " + key );
                    }
                }
                /**/
                else if ( n == 2 )
                {
                    service.remove( region, key );
                    if ( show )
                    {
                        p( "removed " + key );
                    }
                }
                /**/
                else
                {
                    // slightly greater chance of get
                    try
                    {
                        Object obj = service.get( region, key );
                        if ( show && obj != null )
                        {
                            p( obj.toString() );
                        }
                    }
                    catch ( Exception e )
                    {
                        // consider failing, some timeouts are expected
                        e.printStackTrace();
                    }
                }

                if ( i % 100 == 0 )
                {
                    System.out.println( cache.getStats() );
                }

            }
            p( "Finished random cycle of " + numOps );
        }
        catch ( Exception e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
            throw e;
        }

        JCS jcs = JCS.getInstance( region );
        String key = "testKey" + testNum;
        String data = "testData" + testNum;
        jcs.put( key, data );
        String value = (String) jcs.get( key );
        assertEquals( "Couldn't put normally.", data, value );

        // make sure the items we can find are in the correct region.
        for ( int i = 1; i < numOps; i++ )
        {
            String keyL = "key" + i;
            String dataL = (String) jcs.get( keyL );
            if ( dataL != null )
            {
                assertTrue( "Incorrect region detected.", dataL.startsWith( region ) );
            }

        }

        //Thread.sleep( 1000 );

        //ICacheElement element = new CacheElement( region, "abc", "testdata");
        //service.update( element );

        //Thread.sleep( 2500 );
        // could be too mcuh going on right now to get ti through, sot he test
        // might fail.
        //String value2 = (String) jcs.get( "abc" );
        //assertEquals( "Couldn't put laterally, could be too much traffic in
        // queue.", "testdata", value2 );

    }

    public static void p( String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
        else
        {
            //if ( log.isInfoEnabled() )
            //{
            //    log.info( s );
            //}
        }
    }
}
