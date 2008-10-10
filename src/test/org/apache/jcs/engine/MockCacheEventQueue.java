package org.apache.jcs.engine;

import org.apache.jcs.auxiliary.remote.MockRemoteCacheListener;

/** For testing the factory */
public class MockCacheEventQueue
    extends CacheEventQueue
{
    /** junk */
    public MockCacheEventQueue()
    {
        super( new MockRemoteCacheListener(), 1, null, 1, 1 );
    }
}
