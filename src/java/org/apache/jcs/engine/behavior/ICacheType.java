package org.apache.jcs.engine.behavior;

import java.io.Serializable;

/**
 * Inteface implemented by a specific cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheType extends Serializable
{
    /** Composite/ memory cache type, cetral hub. */
    public final static int CACHE_HUB = 1;

    /** Description of the Field */
    public final static int MEMORY_CACHE = 1;

    /** Disk cache type. */
    public final static int DISK_CACHE = 2;

    /** Lateral cache type. */
    public final static int LATERAL_CACHE = 3;

    /** Remote cache type. */
    public final static int REMOTE_CACHE = 4;


    /**
     * Returns the cache type.
     *
     * @return The cacheType value
     */
    public int getCacheType();

}
