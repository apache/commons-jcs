package org.apache.jcs.auxiliary;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventLogger;

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
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     */
    public void logEndICacheEvent( String source, String region, String eventName, String optionalDetails,
                                   Serializable key )
    {
        endICacheEventCalls++;
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param item
     */
    public void logEndICacheEvent( String source, String region, String eventName, String optionalDetails,
                                   ICacheElement item )
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
     * @param errorMessage
     */
    public void logError( String source, String region, String eventName, String errorMessage )
    {
        errorEventCalls++;
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param errorMessage
     * @param item
     */
    public void logError( String source, String region, String eventName, String errorMessage, ICacheElement item )
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param errorMessage
     * @param key
     */
    public void logError( String source, String region, String eventName, String errorMessage, Serializable key )
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     */
    public void logStartICacheEvent( String source, String region, String eventName, String optionalDetails,
                                     Serializable key )
    {
        startICacheEventCalls++;
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param item
     */
    public void logStartICacheEvent( String source, String region, String eventName, String optionalDetails,
                                     ICacheElement item )
    {
        startICacheEventCalls++;
    }
}
