package org.apache.jcs.engine;

import java.util.Hashtable;
import java.util.Map;

import org.apache.jcs.engine.behavior.ICache;;

/**
 * Used to associates a set of [cache listener to cache event queue] for a
 * cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheListeners
{

    /** Description of the Field */
    public final ICache cache;

    /*
     * Map ICacheListener to ICacheEventQueue.
     */
    /** Description of the Field */
    public final Map eventQMap = new Hashtable();


    /**
     * Constructs with the given cache.
     *
     * @param cache
     */
    public CacheListeners( ICache cache )
    {
        if ( cache == null )
        {
            throw new IllegalArgumentException( "cache must not be null" );
        }
        this.cache = cache;
    }

}
// end CacheListeners
