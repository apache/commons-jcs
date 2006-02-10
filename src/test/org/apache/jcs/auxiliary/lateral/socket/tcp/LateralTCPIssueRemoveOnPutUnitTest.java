package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.util.Random;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Tests the issue remove on put fuctionality.
 * 
 * @author asmuts
 */
public class LateralTCPIssueRemoveOnPutUnitTest
    extends TestCase
{
    private static boolean isSysOut = true;

    /**
     * Constructor for the TestDiskCache object.
     * 
     * @param testName
     */
    public LateralTCPIssueRemoveOnPutUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralIssueRemoveCache.ccf" );
    }

    /**
     * 
     * @throws Exception
     */
    public void test()
        throws Exception
    {
        this.runTestForRegion( "region1", 1, 200, 1 );
    }

    /**
     * This tests issues tons of puts. It also check to see that a key that was
     * put in was removed by the clients remove command.
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

        Thread.sleep( 100 );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1110" );
        lattr2.setIssueRemoveOnPut( true );
        // should still try to remove
        lattr2.setAllowPut( false );

        // this service will put and remove using the lateral to
        // the cache instance above
        // the cache thinks it is different since the listenerid is different
        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        String keyToBeRemovedOnPut = "test1";
        cache.put( keyToBeRemovedOnPut, "this should get remvoed" );

        ICacheElement element1 = new CacheElement( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get there" );
        service.update( element1 );

        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                Random ran = new Random( i );
                int n = ran.nextInt( 4 );
                int kn = ran.nextInt( range );
                String key = "key" + kn;

                ICacheElement element = new CacheElement( region, key, region + ":data" + i
                    + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                service.update( element );
                if ( show )
                {
                    p( "put " + key );
                }

                if ( i % 100 == 0 )
                {
                    System.out.println( cache.getStats() );
                }

            }
            p( "Finished cycle of " + numOps );
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

        Thread.sleep( 200 );
        
        Object testObj = cache.get( keyToBeRemovedOnPut );
        p( "test object = " + testObj );
        assertNull( "The test object should have been remvoed by a put.", testObj );

    }

    /**
     * @param s String to be printed
     */
    public static void p( String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
    }
}
