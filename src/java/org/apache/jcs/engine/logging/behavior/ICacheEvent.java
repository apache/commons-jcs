package org.apache.jcs.engine.logging.behavior;

import java.io.Serializable;

/** Defines the common fields required by a cache event. */
public interface ICacheEvent
    extends Serializable
{
    /**
     * @param source the source to set
     */
    public void setSource( String source );

    /**
     * @return the source
     */
    public String getSource();

    /**
     * @param region the region to set
     */
    public void setRegion( String region );

    /**
     * @return the region
     */
    public String getRegion();

    /**
     * @param eventName the eventName to set
     */
    public void setEventName( String eventName );

    /**
     * @return the eventName
     */
    public String getEventName();

    /**
     * @param optionalDetails the optionalDetails to set
     */
    public void setOptionalDetails( String optionalDetails );

    /**
     * @return the optionalDetails
     */
    public String getOptionalDetails();

    /**
     * @param key the key to set
     */
    public void setKey( Serializable key );

    /**
     * @return the key
     */
    public Serializable getKey();
}
