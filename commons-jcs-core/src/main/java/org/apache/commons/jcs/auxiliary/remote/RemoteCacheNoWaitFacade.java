package org.apache.commons.jcs.auxiliary.remote;

import java.util.List;

import org.apache.commons.jcs.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICache;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to provide access to multiple services under nowait protection. Factory should construct
 * NoWaitFacade to give to the composite cache out of caches it constructs from the varies manager
 * to lateral services.
 * <p>
 * Typically, we only connect to one remote server per facade. We use a list of one
 * RemoteCacheNoWait.
 */
public class RemoteCacheNoWaitFacade<K, V>
    extends AbstractRemoteCacheNoWaitFacade<K, V>
{
    /** log instance */
    private static final Log log = LogFactory.getLog( RemoteCacheNoWaitFacade.class );

    /** Provide factory instance to RemoteCacheFailoverRunner */
    private final RemoteCacheFactory cacheFactory;

    /**
     * Constructs with the given remote cache, and fires events to any listeners.
     * <p>
     * @param noWaits
     * @param rca
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @param cacheFactory
     */
    public RemoteCacheNoWaitFacade( List<ICache<K, V>> noWaits,
                                    RemoteCacheAttributes rca,
                                    ICompositeCacheManager cacheMgr,
                                    ICacheEventLogger cacheEventLogger,
                                    IElementSerializer elementSerializer,
                                    RemoteCacheFactory cacheFactory)
    {
        super( noWaits, rca, cacheMgr, cacheEventLogger, elementSerializer );
        this.cacheFactory = cacheFactory;

        for (RemoteCacheNoWait<K,V> rcnw : this.noWaits)
        {
            ((RemoteCache<K, V>)rcnw.getRemoteCache()).setFacade(this);
        }
    }

    /**
     * Begin the failover process if this is a local cache. Clustered remote caches do not failover.
     * <p>
     * @param rcnw The no wait in error.
     */
    @Override
    protected void failover( RemoteCacheNoWait<K, V> rcnw )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "in failover for " + rcnw );
        }

        if ( getAuxiliaryCacheAttributes().getRemoteType() == RemoteType.LOCAL )
        {
            if ( rcnw.getStatus() == CacheStatus.ERROR )
            {
                // start failover, primary recovery process
                RemoteCacheFailoverRunner<K, V> runner = new RemoteCacheFailoverRunner<K, V>( this, this.cacheFactory );
                runner.setDaemon( true );
                runner.start();
                runner.notifyError();

                if ( getCacheEventLogger() != null )
                {
                    getCacheEventLogger().logApplicationEvent( "RemoteCacheNoWaitFacade", "InitiatedFailover",
                                                               rcnw + " was in error." );
                }
            }
            else
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "The noWait is not in error" );
                }
            }
        }
    }

}
