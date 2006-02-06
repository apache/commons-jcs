package org.apache.jcs.utils.threadpool;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.jcs.utils.props.PropertyLoader;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * Verify that the manager can create pools as intended by the default and
 * specified file names.
 * 
 * @author asmuts
 */
public class ThreadPoolManagerUnitTest
    extends TestCase
{

    /**
     * Make sure it can load a default cache.ccf file
     */
    public void testDefaultConfig()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPool pool = mgr.getPool( "test1" );
        assertNotNull( pool );

        int poolSize = pool.getPool().getPoolSize();
        int expectedPoolSize = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.test1.startUpSize" ) );
        assertEquals( poolSize, expectedPoolSize );

        // int qs = ((BoundedBuffer)pool.getQueue()).size();

        int max = pool.getPool().getMaximumPoolSize();
        System.out.println( max );

        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.test1.maximumPoolSize" ) );
        // "Max should be " + expected",
        assertEquals( max, expected );
    }

    /**
     * Try to get an undefined pool from an existing default file.
     */
    public void testDefaultConfigUndefinedPool()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPool pool = mgr.getPool( "doesnotexist" );
        assertNotNull( pool );

        int max = pool.getPool().getMaximumPoolSize();
        System.out.println( max );

        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.default.maximumPoolSize" ) );
        // "Max should be " + expected",
        assertEquals( max, expected );
    }

    /**
     * Makes ure we can get a non existent pool from the non exitent config
     * file.
     */
    public void testNonExistentConfigFile()
    {
        ThreadPoolManager.setPropsFileName( "somefilethatdoesntexist" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPool pool = mgr.getPool( "doesntexist" );
        assertNotNull( "Should have gotten back a pool configured like the default", pool );

        int max = pool.getPool().getMaximumPoolSize();
        System.out.println( max );

        // it will load from the default file
        int expected = Integer.parseInt( PropertyLoader.loadProperties( "cache.ccf" )
            .getProperty( "thread_pool.default.maximumPoolSize" ) );

        // "Max should be " + expected",
        assertEquals( max, expected );
    }

    /**
     * Get a couple pools by name and then see if they are in the list.
     * 
     */
    public void testGetPoolNames()
    {
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        String poolName1 = "testGetPoolNames1";
        mgr.getPool( poolName1 );

        String poolName2 = "testGetPoolNames2";
        mgr.getPool( poolName2 );

        ArrayList names = mgr.getPoolNames();
        assertTrue( "Should have name in list.", names.contains( poolName1 ) );
        assertTrue( "Should have name in list.", names.contains( poolName2 ) );
    }

    /**
     * Verify that the wait policy gets set correctly.
     * 
     */
    public void testWaitPolicyConfig()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        // force config from new props file
        mgr.configure();
        assertNotNull( mgr );

        ThreadPool pool = mgr.getPool( "waittest" );
        assertNotNull( "Should have gotten back a pool.", pool );

        int max = pool.getPool().getMaximumPoolSize();
        System.out.println( "testWaitPolicyConfig " + max );

        // it will load from the default file
        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.waittest.maximumPoolSize" ) );

        // "Max should be " + expected",
        assertEquals( "Max is wrong", max, expected );

        PoolConfiguration config = mgr.loadConfig( "thread_pool.waittest" );

        assertEquals( "Policy is wrong.", PoolConfiguration.POLICY_WAIT, config.getWhenBlockedPolicy() );

    }

    /**
     * Verify that if we specify not to use a buffer boundary that we get a
     * linked queue.
     * 
     */
    public void testNoBoundary()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        // force config from new props file
        mgr.configure();
        assertNotNull( mgr );

        ThreadPool pool = mgr.getPool( "nobound" );
        assertNotNull( "Should have gotten back a pool.", pool );

        assertTrue( "Should have a linked queue and not a bounded buffer.", pool.getQueue() instanceof LinkedQueue );

    }
}
