package org.apache.jcs.auxiliary.remote.http.client;

import org.apache.jcs.auxiliary.remote.AbsractRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/** Does nothing */
public class RemoteHttpClientListener
    extends AbsractRemoteCacheListener
{
    /**
     * Only need one since it does work for all regions, just reference by multiple region names.
     * <p>
     * The constructor exports this object, making it available to receive incoming calls. The
     * callback port is anonymous unless a local port value was specified in the configuration.
     * <p>
     * @param irca
     * @param cacheMgr
     */
    public RemoteHttpClientListener( IRemoteCacheAttributes irca, ICompositeCacheManager cacheMgr )
    {
        super( irca, cacheMgr );
    }

    /** Nothing */
    public void dispose()
    {
        // noop
    }
}
