package org.apache.jcs.utils.struct;

import java.util.Map;

import org.apache.jcs.utils.struct.LRUMap;

import junit.framework.TestCase;

/**
 * Basic unit tests for the LRUMap
 *
 * @author Aaron Smuts
 *
 */
public class LRUMapUnitTest
    extends TestCase
{

    /**
     * Put up to the size limit and then make sure they are all there.
     *
     */
    public void testPutWithSizeLimit()
    {
        int size = 10;
        Map cache = new LRUMap( size );
        
        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }
        
        for ( int i = 0; i < size; i++ )
        {
            String data = (String)cache.get( "key:" + i );
            assertEquals( "Data is wrong.", "data:" + i, data );
        }        
    }
 
    /**
     * Put into the lru with no limit and then make sure they are all there.
     *
     */
    public void testPutWithNoSizeLimit()
    {
        int size = 10;
        Map cache = new LRUMap( );
        
        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }
        
        for ( int i = 0; i < size; i++ )
        {
            String data = (String)cache.get( "key:" + i );
            assertEquals( "Data is wrong.", "data:" + i, data );
        }       
    }
    
    /**
     * Put and then remove.  Make sure the element is returned.
     *
     */
    public void testPutAndRemove()
    {
        int size = 10;
        Map cache = new LRUMap( size );
        
        cache.put( "key:" + 1, "data:" + 1 );
        String data = (String)cache.remove( "key:" + 1 );
        assertEquals( "Data is wrong.", "data:" + 1, data );
    }
    
    /**
     * Call remove on an empty map
     *
     */
    public void testRemoveEmpty()
    {
        int size = 10;
        Map cache = new LRUMap( size );
        
        Object returned = cache.remove( "key:" + 1 );
        assertNull( "Shouldn't hvae anything.", returned );
    }
    
    
    
}
