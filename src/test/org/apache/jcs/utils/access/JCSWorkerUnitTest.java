package org.apache.jcs.utils.access;

import junit.framework.TestCase;

/**
 * Test cases for the JCS worker.
 * 
 * @author Aaron Smuts
 * 
 */
public class JCSWorkerUnitTest
    extends TestCase
{

    /**
     * Test basic worker funtionality.  This is a serial not a concurrent test.  
     * <p>
     * Just verify that the worker will go to the cache before asking the helper.
     * 
     * @throws Exception
     * 
     */
    public void testSimpleGet()
        throws Exception
    {
        JCSWorker cachingWorker = new JCSWorker( "example region" );

        // This is the helper.
        JCSWorkerHelper helper = new AbstractJCSWorkerHelper()
        {
            int timesCalled = 0;

            public Object doWork()
            {
                Object results = new Long( ++timesCalled );
                return results;
            }
        };

        String key = "abc";

        Long result = (Long) cachingWorker.getResult( key, helper );
        assertEquals( "Called the wrong number of times", new Long( 1 ), result );

        // should get it fromthe cache.
        Long result2 = (Long) cachingWorker.getResult( key, helper );
        assertEquals( "Called the wrong number of times", new Long( 1 ), result2 );
        
    }

}
