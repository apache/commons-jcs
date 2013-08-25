package org.apache.commons.jcs.auxiliary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs.engine.logging.CacheEvent;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

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

    /** list of messages */
    public List<String> errorMessages = new ArrayList<String>();

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
    public <T extends Serializable> void logICacheEvent( ICacheEvent<T> event )
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
        errorMessages.add( errorMessage );
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     * @return ICacheEvent
     */
    public <T extends Serializable> ICacheEvent<T> createICacheEvent( String source, String region,
            String eventName, String optionalDetails, T key )
    {
        startICacheEventCalls++;
        return new CacheEvent<T>();
    }
}
