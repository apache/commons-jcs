package org.apache.commons.jcs.engine;

import java.io.Serializable;

import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs.engine.CacheEventQueue;

/** For testing the factory */
public class MockCacheEventQueue<K extends Serializable, V extends Serializable>
    extends CacheEventQueue<K, V>
{
    /** junk */
    public MockCacheEventQueue()
    {
        super( new MockRemoteCacheListener<K, V>(), 1, null, 1, 1 );
    }
}
