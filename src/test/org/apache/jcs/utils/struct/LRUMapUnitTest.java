package org.apache.jcs.utils.struct;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Test basic functionality.
 * 
 * @author Aaron Smuts
 * 
 */
public class LRUMapUnitTest
    extends TestCase
{
    private Level origLevel = Level.INFO;

    public void setUp()
    {
        Logger logger = Logger.getLogger( LRUMap.class );
        origLevel = logger.getLevel();
        logger.setLevel( Level.DEBUG );
    }

    public void tearDown()
    {
        Logger logger = Logger.getLogger( LRUMap.class );
        logger.setLevel( origLevel );
    }

    /**
     * Verify that we can put, get, and remove and item.
     * 
     */
    public void testPutGetRemove()
    {
        int max = 100;
        LRUMap map = new LRUMap( max );

        String key = "MyKey";
        String data = "testdata";

        map.put( key, data );
        assertEquals( "Data is wrong.", data, map.get( key ) );

        map.verifyCache( key );

        map.remove( key );
        assertNull( "Data should have been removed.", map.get( key ) );
    }

    /**
     * Just test that we can put, get and remove as expected.
     * 
     * @exception Exception
     *                Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        int items = 2000;
        LRUMap map = new LRUMap( items );

        for ( int i = 0; i < items; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = items - 1; i >= 0; i-- )
        {
            String res = (String) map.get( i + ":key" );
            if ( res == null )
            {
                assertNotNull( "[" + i + ":key] should not be null", res );
            }
        }

        // verify that this passes.
        map.verifyCache();

    }
}
