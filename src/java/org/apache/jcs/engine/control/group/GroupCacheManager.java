package org.apache.jcs.engine.control.group;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** */
public class GroupCacheManager
     extends CacheHub
     implements Serializable
{
    private final static Log log =
        LogFactory.getLog( CacheHub.class );

    private static GroupCacheManager instance;

    /** Constructor for the GroupCacheManager object */
    protected GroupCacheManager()
    {
        super();
    }

    /**
     * Constructor for the GroupCacheManager object
     *
     * @param propFile
     */
    protected GroupCacheManager( String propFile )
    {
        super( propFile );
    }

    /** Factory method to create the actual GroupCache instance. */
    protected Cache createSystemCache( String cacheName,
                                       ICache[] auxCaches,
                                       ICompositeCacheAttributes cattr,
                                       IElementAttributes attr )
    {
        ICompositeCache systemGroupIdCache =
            ( ICompositeCache ) systemCaches.get( "groupIdCache" );

        return new GroupCache( cacheName, auxCaches, cattr, attr,
            systemGroupIdCache );
    }

    /** */
    protected Cache createCache( String cacheName,
                                 ICache[] auxCaches,
                                 ICompositeCacheAttributes cattr,
                                 IElementAttributes attr )
    {
        ICompositeCache systemGroupIdCache =
            ( ICompositeCache ) systemCaches.get( "groupIdCache" );

        return new GroupCache( cacheName, auxCaches, cattr, attr,
            systemGroupIdCache );
    }

    /** */
    protected void incrementClients()
    {
        super.incrementClients();
    }
}
