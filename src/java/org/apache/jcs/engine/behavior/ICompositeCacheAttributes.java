package org.apache.jcs.engine.behavior;

import java.io.Serializable;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICompositeCacheAttributes extends Serializable
{

    /**
     * SetMaxObjects is used to set the attribute to determine the maximum
     * number of objects allowed in the memory cache. If the max number of
     * objects or the cache size is set, the default for the one not set is
     * ignored. If both are set, both are used to determine the capacity of the
     * cache, i.e., object will be removed from the cache if either limit is
     * reached. TODO: move to MemoryCache config file.
     *
     * @param size The new maxObjects value
     */
    public void setMaxObjects( int size );


    /**
     * Gets the maxObjects attribute of the ICompositeCacheAttributes object
     *
     * @return The maxObjects value
     */
    public int getMaxObjects();


    /**
     * Sets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk );


    /**
     * Gets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @return The useDisk value
     */
    public boolean getUseDisk();


    /**
     * set whether the cache should use a lateral cache
     *
     * @param d The new useLateral value
     */
    public void setUseLateral( boolean d );


    /**
     * Gets the useLateral attribute of the ICompositeCacheAttributes object
     *
     * @return The useLateral value
     */
    public boolean getUseLateral();


    /**
     * Sets whether the cache is remote enabled
     *
     * @param isRemote The new useRemote value
     */
    public void setUseRemote( boolean isRemote );


    /**
     * returns whether the cache is remote enabled
     *
     * @return The useRemote value
     */
    public boolean getUseRemote();


    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s );


    /**
     * Gets the cacheName attribute of the ICompositeCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    /**
     * Sets the name of the MemoryCache, referenced by the appropriate manager.
     * TODO: create a separate memory cache attribute class.
     *
     * @param s The new memoryCacheName value
     */
    public void setMemoryCacheName( String s );


    /**
     * Gets the memoryCacheName attribute of the ICompositeCacheAttributes
     * object
     *
     * @return The memoryCacheName value
     */
    public String getMemoryCacheName();


    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @param useShrinker The new UseMemoryShrinker value
     */
    public void setUseMemoryShrinker( boolean useShrinker );

    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @return The UseMemoryShrinker value
     */
    public boolean getUseMemoryShrinker();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @param seconds The new MaxMemoryIdleTimeSeconds value
     */
    public void setMaxMemoryIdleTimeSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @return The MaxMemoryIdleTimeSeconds value
     */
    public long getMaxMemoryIdleTimeSeconds();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the shrinker interval.
     *
     * @param seconds The new ShrinkerIntervalSeconds value
     */
    public void setShrinkerIntervalSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the shrinker interval.
     *
     * @return The ShrinkerIntervalSeconds value
     */
    public long getShrinkerIntervalSeconds();


    // soultion to interface cloning
    /**
     * Description of the Method
     *
     * @return
     */
    public ICompositeCacheAttributes copy();

}
