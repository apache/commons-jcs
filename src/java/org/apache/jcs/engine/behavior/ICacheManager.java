package org.apache.jcs.engine.behavior;

/**
 * Inteface implemented by a specific cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheManager extends ICacheType
{

    /**
     * methods to get a cache region from a maanger
     *
     * @return The cache value
     */
    public ICache getCache( String cacheName );

}
// end interface
