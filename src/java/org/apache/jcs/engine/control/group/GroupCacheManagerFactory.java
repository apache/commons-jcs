package org.apache.jcs.engine.control.group;

import org.apache.jcs.engine.control.group.GroupCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** */
public abstract class GroupCacheManagerFactory
{
    private final static Log log =
        LogFactory.getLog( GroupCacheManagerFactory.class );

    private static GroupCacheManager instance;

    /** */
    public static GroupCacheManager getInstance()
    {
        return getInstance( null );
    }

    /** */
    public static GroupCacheManager getInstance( String propFile )
    {
        if ( instance == null )
        {
            synchronized ( GroupCacheManager.class )
            {
                if ( instance == null )
                {
                    log.debug( "Instance is null, creating" );

                    if ( propFile == null )
                    {
                        instance = new GroupCacheManager();
                    }
                    else
                    {
                        instance = new GroupCacheManager( propFile );
                    }
                }
            }
        }

        instance.incrementClients();
        return instance;
    }
}
