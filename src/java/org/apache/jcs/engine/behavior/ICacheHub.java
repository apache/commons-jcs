package org.apache.jcs.engine.behavior;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheHub extends ICacheType
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
