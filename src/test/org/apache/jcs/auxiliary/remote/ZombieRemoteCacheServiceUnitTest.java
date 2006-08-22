package org.apache.jcs.auxiliary.remote;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Tests for the zombie remote cache service.
 */
public class ZombieRemoteCacheServiceUnitTest
    extends TestCase
{
    /**
     * Verify that an update event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testUpdateThenWalk()
        throws Exception
    {
        // SETUP
        RemoteCacheServiceMockImpl service = new RemoteCacheServiceMockImpl();

        ZombieRemoteCacheService zombie = new ZombieRemoteCacheService( 10 );

        String cacheName = "testUpdate";

        // DO WORK
        ICacheElement element = new CacheElement( cacheName, "key", "value" );
        zombie.update( element, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", element, service.lastUpdate );
    }

    /**
     * Verify that nothing is added if the max is set to 0.
     * <p>
     * @throws Exception
     */
    public void testUpdateThenWalk_zeroSize()
        throws Exception
    {
        // SETUP
        RemoteCacheServiceMockImpl service = new RemoteCacheServiceMockImpl();

        ZombieRemoteCacheService zombie = new ZombieRemoteCacheService( 0 );

        String cacheName = "testUpdate";

        // DO WORK
        ICacheElement element = new CacheElement( cacheName, "key", "value" );
        zombie.update( element, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertNull( "Nothing should have been put to the service.", service.lastUpdate );
    }

    /**
     * Verify that a remove event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testRemoveThenWalk()
        throws Exception
    {
        // SETUP
        RemoteCacheServiceMockImpl service = new RemoteCacheServiceMockImpl();

        ZombieRemoteCacheService zombie = new ZombieRemoteCacheService( 10 );

        String cacheName = "testRemoveThenWalk";
        String key = "myKey";

        // DO WORK
        zombie.remove( cacheName, key, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", key, service.lastRemoveKey );
    }
    
    /**
     * Verify that a removeAll event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testRemoveAllThenWalk()
        throws Exception
    {
        // SETUP
        RemoteCacheServiceMockImpl service = new RemoteCacheServiceMockImpl();

        ZombieRemoteCacheService zombie = new ZombieRemoteCacheService( 10 );

        String cacheName = "testRemoveThenWalk";

        // DO WORK
        zombie.removeAll( cacheName, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", cacheName, service.lastRemoveAllCacheName);
    }    
}
