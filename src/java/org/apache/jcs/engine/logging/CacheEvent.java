package org.apache.jcs.engine.logging;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jcs.engine.logging.behavior.ICacheEvent;

/** It's returned from create and passed into log. */
public class CacheEvent
    implements ICacheEvent
{
    /** Don't change. */
    private static final long serialVersionUID = -5913139566421714330L;
    
    /** The auxiliary or other source of the event. */
    private String source;
    
    /** The cache region */
    private String region;
    
    /** The event name: update, get, remove, etc. */
    private String eventName;
    
    /** disk location, ip, etc. */
    private String optionalDetails;
    
    /** The key that was put or retrieved. */
    private Serializable key;

    /**
     * @param source the source to set
     */
    public void setSource( String source )
    {
        this.source = source;
    }

    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }

    /**
     * @param region the region to set
     */
    public void setRegion( String region )
    {
        this.region = region;
    }

    /**
     * @return the region
     */
    public String getRegion()
    {
        return region;
    }

    /**
     * @param eventName the eventName to set
     */
    public void setEventName( String eventName )
    {
        this.eventName = eventName;
    }

    /**
     * @return the eventName
     */
    public String getEventName()
    {
        return eventName;
    }

    /**
     * @param optionalDetails the optionalDetails to set
     */
    public void setOptionalDetails( String optionalDetails )
    {
        this.optionalDetails = optionalDetails;
    }

    /**
     * @return the optionalDetails
     */
    public String getOptionalDetails()
    {
        return optionalDetails;
    }

    /**
     * @param key the key to set
     */
    public void setKey( Serializable key )
    {
        this.key = key;
    }

    /**
     * @return the key
     */
    public Serializable getKey()
    {
        return key;
    }
    
    /** 
     * @return reflection toString
     */
    public String toString()
    {
        return ToStringBuilder.reflectionToString( this );
    }
}
