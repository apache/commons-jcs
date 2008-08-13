package org.apache.jcs.engine.control;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventLogger;

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
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     */
    public void logEndICacheEvent( String source, String region, String eventName, String optionalDetails,
                                   Serializable key )
    {
        // TODO Auto-generated method stub            
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
     * @param errorMessage
     */
    public void logError( String source, String region, String eventName, String errorMessage )
    {
        // TODO Auto-generated method stub            
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
        // TODO Auto-generated method stub            
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