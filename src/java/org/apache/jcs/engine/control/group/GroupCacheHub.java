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
public class GroupCacheHub
     extends CacheHub
     implements Serializable
{
    private final static Log log =
        LogFactory.getLog( CacheHub.class );

    /**
     * Overides the base class getInstance method to use a GroupCacheHub
     * as the instance.
     */
    public static synchronized CacheHub getInstance( String propFile )
    {
        if ( instance == null )
        {
            log.debug( "Instance is null, creating" );

            if ( propFile == null )
            {
                instance = new GroupCacheHub();
            }
            else
            {
                instance = new GroupCacheHub( propFile );
            }
        }

        ( ( GroupCacheHub ) instance ).incrementClients();

        return instance;
    }

    /** Constructor for the GroupCacheHub object */
    protected GroupCacheHub()
    {
        super();
    }

    /**
     * Constructor for the GroupCacheHub object
     *
     * @param propFile
     */
    protected GroupCacheHub( String propFile )
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
}
