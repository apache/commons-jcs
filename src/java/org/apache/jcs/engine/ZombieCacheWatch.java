package org.apache.jcs.engine;

import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.behavior.ICacheObserver;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ZombieCacheWatch implements ICacheObserver, IZombie
{
    /**
     * Adds a feature to the CacheListener attribute of the ZombieCacheWatch
     * object
     *
     * @param cacheName The feature to be added to the CacheListener attribute
     * @param obj The feature to be added to the CacheListener attribute
     */
    public void addCacheListener( String cacheName, ICacheListener obj ) { }


    /**
     * Adds a feature to the CacheListener attribute of the ZombieCacheWatch
     * object
     *
     * @param obj The feature to be added to the CacheListener attribute
     */
    public void addCacheListener( ICacheListener obj ) { }


    /** Description of the Method */
    public void removeCacheListener( String cacheName, ICacheListener obj ) { }


    /** Description of the Method */
    public void removeCacheListener( ICacheListener obj ) { }
}
