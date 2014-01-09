package org.apache.commons.jcs.engine.logging;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.jcs.engine.logging.behavior.ICacheEvent;

/** It's returned from create and passed into log. */
public class CacheEvent<K extends Serializable>
    implements ICacheEvent<K>
{
    /** Don't change. */
    private static final long serialVersionUID = -5913139566421714330L;

    /** The time at which this object was created. */
    private final long createTime = System.currentTimeMillis();

    /** The auxiliary or other source of the event. */
    private String source;

    /** The cache region */
    private String region;

    /** The event name: update, get, remove, etc. */
    private String eventName;

    /** disk location, ip, etc. */
    private String optionalDetails;

    /** The key that was put or retrieved. */
    private K key;

    /**
     * @param source the source to set
     */
    @Override
	public void setSource( String source )
    {
        this.source = source;
    }

    /**
     * @return the source
     */
    @Override
	public String getSource()
    {
        return source;
    }

    /**
     * @param region the region to set
     */
    @Override
	public void setRegion( String region )
    {
        this.region = region;
    }

    /**
     * @return the region
     */
    @Override
	public String getRegion()
    {
        return region;
    }

    /**
     * @param eventName the eventName to set
     */
    @Override
	public void setEventName( String eventName )
    {
        this.eventName = eventName;
    }

    /**
     * @return the eventName
     */
    @Override
	public String getEventName()
    {
        return eventName;
    }

    /**
     * @param optionalDetails the optionalDetails to set
     */
    @Override
	public void setOptionalDetails( String optionalDetails )
    {
        this.optionalDetails = optionalDetails;
    }

    /**
     * @return the optionalDetails
     */
    @Override
	public String getOptionalDetails()
    {
        return optionalDetails;
    }

    /**
     * @param key the key to set
     */
    @Override
	public void setKey( K key )
    {
        this.key = key;
    }

    /**
     * @return the key
     */
    @Override
	public K getKey()
    {
        return key;
    }

    /**
     * The time at which this object was created.
     * <p>
     * @return the createTime
     */
    public long getCreateTime()
    {
        return createTime;
    }

    /**
     * @return reflection toString
     */
    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("CacheEvent: ").append(eventName).append(" Created: ").append(new Date(createTime));
    	if (source != null)
    	{
        	sb.append(" Source: ").append(source);
    	}
    	if (region != null)
    	{
        	sb.append(" Region: ").append(region);
    	}
    	if (key != null)
    	{
        	sb.append(" Key: ").append(key);
    	}
    	if (optionalDetails != null)
    	{
        	sb.append(" Details: ").append(optionalDetails);
    	}
        return sb.toString();
    }
}
