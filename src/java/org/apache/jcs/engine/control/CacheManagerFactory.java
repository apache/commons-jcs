package org.apache.jcs.engine.control;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** */
public abstract class CacheManagerFactory
{
    private final static Log log =
        LogFactory.getLog( CacheManagerFactory.class );

    private static CompositeCacheManager instance;

    /** Gets the instance of CompositeCacheManager */
    public static CompositeCacheManager getInstance()
    {
        return getInstance( null );
    }

    /** Gets the CompositeCacheManager instance */
    public static CompositeCacheManager getInstance( String propFile )
    {
        if ( instance == null )
        {
            synchronized ( CompositeCacheManager.class )
            {
                if ( instance == null )
                {
                    log.debug( "Instance is null, creating" );

                    if ( propFile == null )
                    {
                        instance = new CompositeCacheManager();
                    }
                    else
                    {
                        instance = new CompositeCacheManager( propFile );
                    }
                }
            }
        }

        instance.incrementClients();
        return instance;
    }
}
