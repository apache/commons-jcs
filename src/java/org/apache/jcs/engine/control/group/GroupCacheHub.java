package org.apache.jcs.engine.control.group;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.auxiliary.AuxiliaryCache;

/** */
public class GroupCacheHub
    extends CacheHub
    implements Serializable
{
    /**
     * @see CacheHub#createInstance
     */
    protected static CacheHub createInstance()
    {
        return new GroupCacheHub();
    }

    /**
     * @see CacheHub#createSystemCache
     */
    protected Cache createSystemCache( String cacheName,
                                       AuxiliaryCache[] auxCaches,
                                       ICompositeCacheAttributes cattr,
                                       IElementAttributes attr )
    {
        ICompositeCache systemGroupIdCache =
            ( ICompositeCache ) systemCaches.get( "groupIdCache" );

        return new GroupCache( cacheName, auxCaches, cattr, attr,
                               systemGroupIdCache );
    }

    /**
     * @see CacheHub#createCache
     */
    protected Cache createCache( String cacheName,
                                 AuxiliaryCache[] auxCaches,
                                 ICompositeCacheAttributes cattr,
                                 IElementAttributes attr )
    {
        ICompositeCache systemGroupIdCache =
            ( ICompositeCache ) systemCaches.get( "groupIdCache" );

        return new GroupCache( cacheName, auxCaches, cattr, attr,
                               systemGroupIdCache );
    }
}
