package org.apache.jcs.engine.logging;

import java.io.Serializable;

import org.apache.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * For testing the configurator.
 */
public class MockCacheEventLogger
    implements ICacheEventLogger
{
    /** test property */
    private String testProperty;

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    public void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    public void logError( String source, String eventName, String errorMessage )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     * @return ICacheEvent
     */
    public <T extends Serializable> ICacheEvent<T> createICacheEvent( String source, String region, String eventName, String optionalDetails,
                                          T key )
    {
        return new CacheEvent<T>();
    }

    /**
     * @param event
     */
    public <T extends Serializable> void logICacheEvent( ICacheEvent<T> event )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param testProperty
     */
    public void setTestProperty( String testProperty )
    {
        this.testProperty = testProperty;
    }

    /**
     * @return testProperty
     */
    public String getTestProperty()
    {
        return testProperty;
    }
}