package org.apache.jcs.engine.control;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.logging.CacheEvent;
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
    public ICacheEvent createICacheEvent( String source, String region, String eventName, String optionalDetails,
                                          Serializable key )
    {
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
        return new CacheEvent();
    }

    /**
     * @param event
     */
    public void logICacheEvent( ICacheEvent event )
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