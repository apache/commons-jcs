package org.apache.jcs.auxiliary;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.CacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.engine.match.KeyMatcherPatternImpl;
import org.apache.jcs.engine.match.behavior.IKeyMatcher;
import org.apache.jcs.utils.serialization.StandardSerializer;

/** This holds convenience methods used by most auxiliary caches. */
public abstract class AbstractAuxiliaryCache<K extends Serializable, V extends Serializable>
    implements AuxiliaryCache<K, V>
{
    /** Don't change. */
    private static final long serialVersionUID = -1285708398502576617L;

    /** An optional event logger */
    protected ICacheEventLogger cacheEventLogger;

    /** The serializer. Uses a standard serializer by default. */
    protected IElementSerializer elementSerializer = new StandardSerializer();

    /** Key matcher used by the getMatching API */
    protected IKeyMatcher<K> keyMatcher = new KeyMatcherPatternImpl<K>();

    /**
     * Logs an event if an event logger is configured.
     * <p>
     * @param item
     * @param eventName
     * @return ICacheEvent
     */
    protected ICacheEvent<K> createICacheEvent( ICacheElement<K, V> item, String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<K>();
        }
        String diskLocation = getEventLoggingExtraInfo();
        String regionName = null;
        K key = null;
        if ( item != null )
        {
            regionName = item.getCacheName();
            key = item.getKey();
        }
        return cacheEventLogger.createICacheEvent( getAuxiliaryCacheAttributes().getName(), regionName, eventName,
                                                   diskLocation, key );
    }

    /**
     * Logs an event if an event logger is configured.
     * <p>
     * @param regionName
     * @param key
     * @param eventName
     * @return ICacheEvent
     */
    protected <T extends Serializable> ICacheEvent<T> createICacheEvent( String regionName, T key, String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<T>();
        }
        String diskLocation = getEventLoggingExtraInfo();
        return cacheEventLogger.createICacheEvent( getAuxiliaryCacheAttributes().getName(), regionName, eventName,
                                                   diskLocation, key );

    }

    /**
     * Logs an event if an event logger is configured.
     * <p>
     * @param cacheEvent
     */
    protected <T extends Serializable> void logICacheEvent( ICacheEvent<T> cacheEvent )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logICacheEvent( cacheEvent );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     * <p>
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    protected void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logApplicationEvent( source, eventName, optionalDetails );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     * <p>
     * @param source
     * @param eventName
     * @param errorMessage
     */
    protected void logError( String source, String eventName, String errorMessage )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logError( source, eventName, errorMessage );
        }
    }

    /**
     * Gets the extra info for the event log.
     * <p>
     * @return IP, or disk location, etc.
     */
    public abstract String getEventLoggingExtraInfo();

    /**
     * Allows it to be injected.
     * <p>
     * @param cacheEventLogger
     */
    public void setCacheEventLogger( ICacheEventLogger cacheEventLogger )
    {
        this.cacheEventLogger = cacheEventLogger;
    }

    /**
     * Allows it to be injected.
     * <p>
     * @return cacheEventLogger
     */
    public ICacheEventLogger getCacheEventLogger()
    {
        return this.cacheEventLogger;
    }

    /**
     * Allows you to inject a custom serializer. A good example would be a compressing standard
     * serializer.
     * <p>
     * Does not allow you to set it to null.
     * <p>
     * @param elementSerializer
     */
    public void setElementSerializer( IElementSerializer elementSerializer )
    {
        if ( elementSerializer != null )
        {
            this.elementSerializer = elementSerializer;
        }
    }

    /**
     * Allows it to be injected.
     * <p>
     * @return elementSerializer
     */
    public IElementSerializer getElementSerializer()
    {
        return this.elementSerializer;
    }

    /**
     * Sets the key matcher used by get matching.
     * <p>
     * @param keyMatcher
     */
    public void setKeyMatcher( IKeyMatcher<K> keyMatcher )
    {
        if ( keyMatcher != null )
        {
            this.keyMatcher = keyMatcher;
        }
    }

    /**
     * Returns the key matcher used by get matching.
     * <p>
     * @return keyMatcher
     */
    public IKeyMatcher<K> getKeyMatcher()
    {
        return this.keyMatcher;
    }
}
