package org.apache.jcs.engine.logging;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * This implementation simple logs to a commons logger at debug level, for all events. It's mainly
 * for testing. It isn't very useful otherwise.
 */
public class CacheEventLoggerDebugLoggerImpl
    implements ICacheEventLogger
{
    /** This is the name of the category. */
    private String logCategoryName = CacheEventLoggerDebugLoggerImpl.class.getName();

    /** The logger. This is recreated on set logCategoryName */
    private Log log = LogFactory.getLog( logCategoryName );

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
        ICacheEvent event = new CacheEvent();
        event.setSource( source );
        event.setRegion( region );
        event.setEventName( eventName );
        event.setOptionalDetails( optionalDetails );
        event.setKey( key );

        return event;
    }

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    public void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( source + " | " + eventName + " | " + optionalDetails );
        }
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    public void logError( String source, String eventName, String errorMessage )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( source + " | " + eventName + " | " + errorMessage );
        }
    }

    /**
     * @param event
     */
    public void logICacheEvent( ICacheEvent event )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( event );
        }
    }

    /**
     * @param logCategoryName
     */
    public synchronized void setLogCategoryName( String logCategoryName )
    {
        if ( logCategoryName != null && !logCategoryName.equals( this.logCategoryName ) )
        {
            this.logCategoryName = logCategoryName;
            log = LogFactory.getLog( logCategoryName );
        }
    }
}
