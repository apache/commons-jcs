package org.apache.jcs.access;

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectExistsException;

import junit.framework.TestCase;

/**
 * Tests the methods of the cache access class from which the class JCS extends.
 *
 * @author Aaron Smuts
 *
 */
public class CacheAccessUnitTest
    extends TestCase
{

    /**
     * Verify that we get an object exists exception if the item is in
     * the cache.
     *
     */
    public void testPutSafe()
    {
        
        CacheAccess access = null;
        try
        {
            access = CacheAccess.getAccess( "test" );
            
            assertNotNull( "We should have an access class", access );
        }
        catch ( CacheException e )
        {
            fail( "Shouldn't have received an error." + e.getMessage() );
        }
        
        String key = "mykey";
        String value = "myvalue";
        
        try
        {
            access.put( key, value );
        }
        catch ( CacheException e )
        {
            fail( "Should have been able to put " + e.getMessage() );
        }
        String returnedValue1 = (String)access.get( key );
        assertEquals( "Wrong value returned.", value, returnedValue1 );
        
        try
        {
            access.putSafe( key, "someothervalue" );
            fail( "We should have received an eception since this key is alredy in the cache." );
        }
        catch ( CacheException e )
        {
            //e.printStackTrace();
            // expected
            assertTrue( "Wrong type of exception.", e instanceof ObjectExistsException );
            assertTrue( "Should have the key in the error message.", e.getMessage().indexOf( "[" + key + "]" ) != -1 );
        }

        String returnedValue2 = (String)access.get( key );
        assertEquals( "Wrong value returned.  Shoudl still be the original.", value, returnedValue2 );
    }
    
    
}
