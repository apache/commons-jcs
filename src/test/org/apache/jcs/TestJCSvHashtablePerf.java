package org.apache.jcs;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This test ensures that basic memory operations are with a speficified order
 * of magnitude of the java.util.Hashtable.
 * <p>
 * Currenlty JCS is un 2x a hashtable for gets, and under 1.2x for puts.
 *  
 */
public class TestJCSvHashtablePerf extends TestCase
{

    float ratioPut = 0;

    float ratioGet = 0;

    float target   = 2.50f;

    int   loops    = 20;

    int   tries    = 50000;

    /**
     * @param testName
     */
    public TestJCSvHashtablePerf(String testName)
    {
        super( testName );
    }

    /**
     * A unit test suite for JUnit
     * 
     * @return The test suite
     */
    public static Test suite()
    {
        return new TestSuite( TestJCSvHashtablePerf.class );
    }

    /**
     * A unit test for JUnit
     * 
     * @exception Exception
     *                Description of the Exception
     */
    public void testSimpleLoad() throws Exception
    {
        doWork();
        assertTrue( this.ratioPut < target );
        assertTrue( this.ratioGet < target );
    }

    /**
     *  
     */
    public void doWork()
    {

        long start = 0;
        long end = 0;
        long time = 0;
        float tPer = 0;

        long putTotalJCS = 0;
        long getTotalJCS = 0;
        long putTotalHashtable = 0;
        long getTotalHashtable = 0;

        try
        {

            JCS.setConfigFilename( "/TestJCSvHashtablePerf.ccf" );
            JCS cache = JCS.getInstance( "testCache1" );

            for (int j = 0; j < loops; j++)
            {

                String name = "JCS      ";
                start = System.currentTimeMillis();
                for (int i = 0; i < tries; i++)
                {
                    cache.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalJCS += time;
                tPer = Float.intBitsToFloat( (int) time )
                        / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = "
                        + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for (int i = 0; i < tries; i++)
                {
                    cache.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalJCS += time;
                tPer = Float.intBitsToFloat( (int) time )
                        / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = "
                        + time + "; millis per = " + tPer );

                ///////////////////////////////////////////////////////////////
                name = "Hashtable";
                Hashtable cache2 = new Hashtable();
                start = System.currentTimeMillis();
                for (int i = 0; i < tries; i++)
                {
                    cache2.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time )
                        / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = "
                        + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for (int i = 0; i < tries; i++)
                {
                    cache2.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time )
                        / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = "
                        + time + "; millis per = " + tPer );

                System.out.println( "\n" );
            }

        }
        catch (Exception e)
        {
            e.printStackTrace( System.out );
            System.out.println( e );
        }

        long putAvJCS = putTotalJCS / loops;
        long getAvJCS = getTotalJCS / loops;
        long putAvHashtable = putTotalHashtable / loops;
        long getAvHashtable = getTotalHashtable / loops;

        System.out.println( "Finished " + loops + " loops of " + tries
                + " gets and puts" );

        System.out.println( "\n" );
        System.out.println( "Put average for JCS       = " + putAvJCS );
        System.out.println( "Put average for Hashtable = " + putAvHashtable );
        ratioPut = Float.intBitsToFloat( (int) putAvJCS )
                / Float.intBitsToFloat( (int) putAvHashtable );
        System.out.println( "JCS puts took " + ratioPut
                + " times the Hashtable, the goal is <" + target + "x" );

        System.out.println( "\n" );
        System.out.println( "Get average for JCS       = " + getAvJCS );
        System.out.println( "Get average for Hashtable = " + getAvHashtable );
        ratioGet = Float.intBitsToFloat( (int) getAvJCS )
                / Float.intBitsToFloat( (int) getAvHashtable );
        System.out.println( "JCS gets took " + ratioGet
                + " times the Hashtable, the goal is <" + target + "x" );

    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        TestJCSvHashtablePerf test = new TestJCSvHashtablePerf( "command" );
        test.doWork();
    }

}