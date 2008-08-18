package org.apache.jcs.auxiliary;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.logging.CacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * For testing auxiliary event logging. Improve later so we can test the details. This is very
 * crude.
 */
public class MockCacheEventLogger
    implements ICacheEventLogger
{
    /** times called */
    public int applicationEventCalls = 0;

    /** times called */
    public int startICacheEventCalls = 0;

    /** times called */
    public int endICacheEventCalls = 0;

    /** times called */
    public int errorEventCalls = 0;

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    public void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        applicationEventCalls++;
    }

    /**
     * @param cacheEvent
     */
    public void logICacheEvent( ICacheEvent cacheEvent )
    {
        endICacheEventCalls++;
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    public void logError( String source, String eventName, String errorMessage )
    {
        errorEventCalls++;
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     * @return ICacheEvent
     */
    public ICacheEvent createICacheEvent( String source, String region, String eventName, String optionalDetails,
                                          Serializable key )
    {
        startICacheEventCalls++;
        return new CacheEvent();
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param item
     * @return ICacheEvent
     */
    public ICacheEvent createICacheEvent( String source, String region, String eventName, String optionalDetails,
                                          ICacheElement item )
    {
        startICacheEventCalls++;
        return new CacheEvent();
    }
}
