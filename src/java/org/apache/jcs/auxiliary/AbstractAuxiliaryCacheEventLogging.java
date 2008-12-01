package org.apache.jcs.auxiliary;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * All ICacheEvents are defined as final. Children must implement process events. These are wrapped
 * in event log parent calls.
 * <p>
 * You can override the public method, but if you don't, the default will call getWithTiming.
 */
public abstract class AbstractAuxiliaryCacheEventLogging
    extends AbstractAuxiliaryCache
{
    /** Don't change. */
    private static final long serialVersionUID = -3921738303365238919L;

    /**
     * Puts an item into the cache.
     * <p>
     * @param cacheElement
     * @throws IOException
     */
    public void update( ICacheElement cacheElement )
        throws IOException
    {
        updateWithEventLogging( cacheElement );
    }

    /**
     * Puts an item into the cache. Wrapped in logging.
     * <p>
     * @param cacheElement
     * @throws IOException
     */
    protected final void updateWithEventLogging( ICacheElement cacheElement )
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( cacheElement, ICacheEventLogger.UPDATE_EVENT );
        try
        {
            processUpdate( cacheElement );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Implementation of put.
     * <p>
     * @param cacheElement
     * @throws IOException
     */
    protected abstract void processUpdate( ICacheElement cacheElement )
        throws IOException;

    /**
     * Gets the item from the cache.
     * <p>
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        return getWithEventLogging( key );
    }

    /**
     * Gets the item from the cache. Wrapped in logging.
     * <p>
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    protected final ICacheElement getWithEventLogging( Serializable key )
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), key, ICacheEventLogger.GET_EVENT );
        try
        {
            return processGet( key );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Implementation of get.
     * <p>
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    protected abstract ICacheElement processGet( Serializable key )
        throws IOException;

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    public Map getMultiple( Set keys )
        throws IOException
    {
        return getMultipleWithEventLogging( keys );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    protected final Map getMultipleWithEventLogging( Set keys )
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), (Serializable) keys,
                                                    ICacheEventLogger.GETMULTIPLE_EVENT );
        try
        {
            return processGetMultiple( keys );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Implementation of getMultiple.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    protected abstract Map processGetMultiple( Set keys )
        throws IOException;

    /**
     * Gets items from the cache matching the given pattern. Items from memory will replace those
     * from remote sources.
     * <p>
     * This only works with string keys. It's too expensive to do a toString on every key.
     * <p>
     * Auxiliaries will do their best to handle simple expressions. For instance, the JDBC disk
     * cache will convert * to % and . to _
     * <p>
     * @param pattern
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    public Map getMatching( String pattern )
        throws IOException
    {
        return getMatchingWithEventLogging( pattern );
    }

    /**
     * Gets mmatching items from the cache based on the given pattern.
     * <p>
     * @param pattern
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    protected final Map getMatchingWithEventLogging( String pattern )
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), pattern, ICacheEventLogger.GETMATCHING_EVENT );
        try
        {
            return processGetMatching( pattern );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Implementation of getMatching.
     * <p>
     * @param pattern
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    protected abstract Map processGetMatching( String pattern )
        throws IOException;

    /**
     * Removes the item from the cache. Wraps the remove in event logs.
     * <p>
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        return removeWithEventLogging( key );
    }

    /**
     * Removes the item from the cache. Wraps the remove in event logs.
     * <p>
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    protected final boolean removeWithEventLogging( Serializable key )
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), key, ICacheEventLogger.REMOVE_EVENT );
        try
        {
            return processRemove( key );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Specific implementation of remove.
     * <p>
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    protected abstract boolean processRemove( Serializable key )
        throws IOException;

    /**
     * Removes all from the region. Wraps the removeAll in event logs.
     * <p>
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        removeAllWithEventLogging();
    }

    /**
     * Removes all from the region. Wraps the removeAll in event logs.
     * <p>
     * @throws IOException
     */
    protected final void removeAllWithEventLogging()
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), "all", ICacheEventLogger.REMOVEALL_EVENT );
        try
        {
            processRemoveAll();
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Specific implementation of removeAll.
     * <p>
     * @throws IOException
     */
    protected abstract void processRemoveAll()
        throws IOException;

    /**
     * Synchronously dispose the remote cache; if failed, replace the remote handle with a zombie.
     * <p>
     * @throws IOException
     */
    public void dispose()
        throws IOException
    {
        disposeWithEventLogging();
    }

    /**
     * Synchronously dispose the remote cache; if failed, replace the remote handle with a zombie.
     * Wraps the removeAll in event logs.
     * <p>
     * @throws IOException
     */
    protected final void disposeWithEventLogging()
        throws IOException
    {
        ICacheEvent cacheEvent = createICacheEvent( getCacheName(), "none", ICacheEventLogger.DISPOSE_EVENT );
        try
        {
            processDispose();
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Specific implementation of dispose.
     * <p>
     * @throws IOException
     */
    protected abstract void processDispose()
        throws IOException;
}
