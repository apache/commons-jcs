package org.apache.jcs.engine.behavior;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author
 * @created January 15, 2002
 * @version 1.0
 */

public interface ICompositeCacheManager extends ICacheManager
{

    /**
     * Gets the cache attribute of the ICompositeCacheManager object
     *
     * @return The cache value
     */
    public ICache getCache( ICompositeCacheAttributes cattr );


    /**
     * Gets the defaultCattr attribute of the ICompositeCacheManager object
     *
     * @return The defaultCattr value
     */
    public ICompositeCacheAttributes getDefaultCattr();

}
