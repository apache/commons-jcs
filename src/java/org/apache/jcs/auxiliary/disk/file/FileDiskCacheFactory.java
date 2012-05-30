package org.apache.jcs.auxiliary.disk.file;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/** Create Disk File Caches */
public class FileDiskCacheFactory
    implements AuxiliaryCacheFactory
{
    /** The logger. */
    private final static Log log = LogFactory.getLog( FileDiskCacheFactory.class );

    /** The auxiliary name. */
    private String name;

    /** The manager used by this factory instance */
    private FileDiskCacheManager diskFileCacheManager;

    /**
     * Creates a manager if we don't have one, and then uses the manager to create the cache. The
     * same factory will be called multiple times by the composite cache to create a cache for each
     * region.
     * <p>
     * @param attr config
     * @param cacheMgr the manager to use if needed
     * @param cacheEventLogger the event logger
     * @param elementSerializer the serializer
     * @return AuxiliaryCache
     */
    public <K extends Serializable, V extends Serializable> FileDiskCache<K, V> createCache(
            AuxiliaryCacheAttributes attr, ICompositeCacheManager cacheMgr,
           ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        FileDiskCacheAttributes idfca = (FileDiskCacheAttributes) attr;
        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating DiskFileCache for attributes = " + idfca );
        }
        synchronized( this )
        {
            if ( diskFileCacheManager == null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Creating DiskFileCacheManager" );
                }
                diskFileCacheManager = new FileDiskCacheManager( idfca, cacheEventLogger, elementSerializer );
            }
            return diskFileCacheManager.getCache( idfca );
        }
    }

    /**
     * Gets the name attribute of the DiskCacheFactory object
     * <p>
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the DiskCacheFactory object
     * <p>
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
