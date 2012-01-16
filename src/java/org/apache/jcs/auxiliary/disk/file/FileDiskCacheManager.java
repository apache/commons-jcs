package org.apache.jcs.auxiliary.disk.file;

import java.io.Serializable;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.disk.AbstractDiskCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * This is a non singleton. It creates caches on a per region basis.
 */
public class FileDiskCacheManager
    extends AbstractDiskCacheManager
{
    /** Don't change */
    private static final long serialVersionUID = -4153287154512264626L;

    /** The logger */
    private final static Log log = LogFactory.getLog( FileDiskCacheManager.class );

    /** Each region has an entry here. */
    private final Hashtable<String, AuxiliaryCache<? extends Serializable, ? extends Serializable>> caches =
        new Hashtable<String, AuxiliaryCache<? extends Serializable, ? extends Serializable>>();

    /** User configurable attributes */
    private final FileDiskCacheAttributes defaultCacheAttributes;

    /**
     * Constructor for the DiskFileCacheManager object
     * <p>
     * @param defaultCacheAttributes Default attributes for caches managed by the instance.
     * @param cacheEventLogger
     * @param elementSerializer
     */
    protected FileDiskCacheManager( FileDiskCacheAttributes defaultCacheAttributes, ICacheEventLogger cacheEventLogger,
                                  IElementSerializer elementSerializer )
    {
        this.defaultCacheAttributes = defaultCacheAttributes;
        setElementSerializer( elementSerializer );
        setCacheEventLogger( cacheEventLogger );
    }

    /**
     * Gets an DiskFileCache for the supplied name using the default attributes.
     * <p>
     * @param cacheName Name that will be used when creating attributes.
     * @return A cache.
     */
    public <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V> getCache( String cacheName )
    {
        FileDiskCacheAttributes cacheAttributes = (FileDiskCacheAttributes) defaultCacheAttributes.copy();

        cacheAttributes.setCacheName( cacheName );

        return getCache( cacheAttributes );
    }

    /**
     * Get an DiskFileCache for the supplied attributes. Will provide an existing cache for the name
     * attribute if one has been created, or will create a new cache.
     * <p>
     * @param cacheAttributes Attributes the cache should have.
     * @return A cache, either from the existing set or newly created.
     */
    public <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V> getCache( FileDiskCacheAttributes cacheAttributes )
    {
        AuxiliaryCache<K, V> cache = null;

        String cacheName = cacheAttributes.getCacheName();

        log.debug( "Getting cache named: " + cacheName );

        synchronized ( caches )
        {
            // Try to load the cache from the set that have already been
            // created. This only looks at the name attribute.

            cache = (AuxiliaryCache<K, V>) caches.get( cacheName );

            // If it was not found, create a new one using the supplied
            // attributes

            if ( cache == null )
            {
                cache = new FileDiskCache<K, V>( cacheAttributes, getElementSerializer() );
                cache.setCacheEventLogger( getCacheEventLogger() );
                caches.put( cacheName, cache );
            }
        }

        return cache;
    }

    /**
     * Gets the cacheType attribute of the DiskCacheManager object
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }
}
