package org.apache.jcs.auxiliary.remote;

import java.io.IOException;
import java.io.Serializable;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.rmi.server.UnicastRemoteObject;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// remove

/**
 * Registered with RemoteCache server. The server updates the local caches via
 * this class.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheListener
     implements IRemoteCacheListener, IRemoteCacheConstants, Serializable
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheListener.class );

    /** Description of the Field */
    protected static transient CompositeCacheManager cacheMgr;

    /** Description of the Field */
    protected static IRemoteCacheListener instance;
    /** Description of the Field */
    protected IRemoteCacheAttributes irca;

    /** Description of the Field */
    protected int puts = 0;
    /** Description of the Field */
    protected int removes = 0;


    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param irca
     */
    protected RemoteCacheListener( IRemoteCacheAttributes irca )
    {
        this.irca = irca;

        // may need to add to ICacheManager interface to handle
        // the source arument extended update and remove methods

        // causes circular reference, unfortunate, becasue the
        // the overhead is higer
        // will need to pass a refernce thru
        //cacheMgr = CacheManagerFactory.getInstance();

        // Export this remote object to make it available to receive incoming calls,
        // using an anonymous port.
        try
        {
            if ( irca.getLocalPort() != 0 )
            {
                UnicastRemoteObject.exportObject( this, irca.getLocalPort() );
            }
            else
            {
                UnicastRemoteObject.exportObject( this );
            }
        }
        catch ( RemoteException ex )
        {
            log.error( ex );
            throw new IllegalStateException( ex.getMessage() );
        }

    }


    /**
     * let the remote cache set a listener_id. Since there is only one listerenr
     * for all the regions and every region gets registered? the id shouldn't be
     * set if it isn't zero. If it is we assume that it is a reconnect.
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException
    {
        RemoteCacheInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }


    /**
     * Gets the listenerId attribute of the RemoteCacheListener object
     *
     * @return The listenerId value
     */
    public byte getListenerId()
        throws IOException
    {

        // set the manager since we are in use
        //getCacheManager();

        //p( "get listenerId" );
        if ( log.isDebugEnabled() )
        {
            log.debug( "get listenerId = " + RemoteCacheInfo.listenerId );
        }
        return RemoteCacheInfo.listenerId;
    }


    /**
     * Gets the remoteType attribute of the RemoteCacheListener object
     *
     * @return The remoteType value
     */
    public int getRemoteType()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "getRemoteType = " + irca.getRemoteType() );
        }
        return irca.getRemoteType();
    }


    /**
     * Gets the instance attribute of the RemoteCacheListener class
     *
     * @return The instance value
     */
    public static IRemoteCacheListener getInstance( IRemoteCacheAttributes irca )
    {
        //throws IOException, NotBoundException
        if ( instance == null )
        {
            synchronized ( RemoteCacheListener.class )
            {
                if ( instance == null )
                {
                    instance = new RemoteCacheListener( irca );
                }
            }
        }
        //instance.incrementClients();
        return instance;
    }


    //////////////////////////// implements the IRemoteCacheListener interface. //////////////
    /**
     * Just remove the element since it has been updated elsewhere cd should be
     * incomplete for faster transmission. We don't want to pass data only
     * invalidation. The next time it is used the local cache will get the new
     * version from the remote store
     */
    public void handlePut( ICacheElement cb )
        throws IOException
    {

        if ( irca.getRemoveUponRemotePut() )
        {
            log.debug( "PUTTING ELEMENT FROM REMOTE, (  invalidating ) " );

            handleRemove( cb.getCacheName(), cb.getKey() );

        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "PUTTING ELEMENT FROM REMOTE, ( updating ) " );
                log.debug( "cb = " + cb );

                puts++;
                if ( puts % 100 == 0 )
                {
                    log.debug( "puts = " + puts );
                }
            }

            getCacheManager();
            CompositeCache cache = cacheMgr.getCache( cb.getCacheName() );

            cache.localUpdate( cb );
        }

        return;
    }


    /** Description of the Method */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            removes++;
            if ( removes % 100 == 0 )
            {
                log.debug( "removes = " + removes );
            }

            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCacheManager();
        CompositeCache cache = ( CompositeCache ) cacheMgr.getCache( cacheName );

        cache.localRemove( key );
    }


    /** Description of the Method */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }
        getCacheManager();
        ICache cache = cacheMgr.getCache( cacheName );
        cache.removeAll();
    }


    /** Description of the Method */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }
        CompositeCacheManager cm = ( CompositeCacheManager ) cacheMgr;
        cm.freeCache( cacheName, true);
    }


    // override for new funcitonality
    /**
     * Gets the cacheManager attribute of the RemoteCacheListener object
     */
    protected void getCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();
            log.debug( "had to get cacheMgr" );
            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr = " + cacheMgr );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "already got cacheMgr = " + cacheMgr );
            }
        }
    }
}
