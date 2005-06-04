package org.apache.jcs.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * This class hands out event Queues. This allows us to change the
 * implementation more easily.
 * 
 * @author aaronsm
 *  
 */
public class CacheEventQueueFactory
{

    private static final Log log = LogFactory.getLog( CacheEventQueueFactory.class );

    /**
     * The most commonly used factory method.
     * 
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param threadPoolName
     * @param poolType
     * @return
     */
    public ICacheEventQueue createCacheEventQueue( ICacheListener listener, long listenerId, String cacheName,
                                                  String threadPoolName, int poolType )
    {
        return createCacheEventQueue( listener, listenerId, cacheName, 10, 500, threadPoolName, poolType );
    }

    /**
     * Fully configured event queue.
     * 
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName
     *            null is ok, if not a pooled event queue this is ignored
     * @param poolType
     *            single or pooled
     * @return
     */
    public ICacheEventQueue createCacheEventQueue( ICacheListener listener, long listenerId, String cacheName,
                                                  int maxFailure, int waitBeforeRetry, String threadPoolName,
                                                  int poolType )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "threadPoolName = [" + threadPoolName + "] poolType = " + poolType + " " );
        }

        if ( poolType == ICacheEventQueue.SINGLE_QUEUE_TYPE )
        {
            return new CacheEventQueue( listener, listenerId, cacheName, maxFailure, waitBeforeRetry );
        }
        else
        {
            return new PooledCacheEventQueue( listener, listenerId, cacheName, maxFailure, waitBeforeRetry,
                                              threadPoolName );
        }
    }
}