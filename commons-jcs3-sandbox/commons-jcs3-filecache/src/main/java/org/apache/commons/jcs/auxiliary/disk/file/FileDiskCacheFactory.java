package org.apache.commons.jcs.auxiliary.disk.file;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheFactory;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogFactory;

/** Create Disk File Caches */
public class FileDiskCacheFactory
    implements AuxiliaryCacheFactory
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( FileDiskCacheFactory.class );

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
    @Override
    public <K, V> FileDiskCache<K, V> createCache(
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
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the DiskCacheFactory object
     * <p>
     * @param name The new name value
     */
    @Override
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#initialize()
     */
    @Override
    public void initialize()
    {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#dispose()
     */
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }
}
