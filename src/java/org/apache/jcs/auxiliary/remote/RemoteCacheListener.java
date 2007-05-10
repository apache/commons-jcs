package org.apache.jcs.auxiliary.remote;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.utils.net.HostNameUtil;
import org.apache.jcs.utils.serialization.SerializationConversionUtil;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * Registered with RemoteCache server. The server updates the local caches via this listener. Each
 * server asings a unique listener id for a listener.
 * <p>
 * One listener is used per remote cache server. The same listener is used for all the regions that
 * talk to a particular server.
 */
public class RemoteCacheListener
    implements IRemoteCacheListener, IRemoteCacheConstants, Serializable
{
    private static final long serialVersionUID = 1L;

    private final static Log log = LogFactory.getLog( RemoteCacheListener.class );

    private static String localHostName = HostNameUtil.getLocalHostAddress();

    boolean disposed = false;

    /**
     * The cache manager used to put items in differnt regions. This is set lazily and should not be
     * sent to the remote server.
     */
    protected transient ICompositeCacheManager cacheMgr;

    /** The remote cache configuration object. */
    protected IRemoteCacheAttributes irca;

    /** Number of put requests received. For debugging only. */
    protected int puts = 0;

    /** Number of remove requests received. For debugging only. */
    protected int removes = 0;

    /** This is set by the remote cache server. */
    protected long listenerId = 0;

    private transient IElementSerializer elementSerializer = new StandardSerializer();

    /**
     * Only need one since it does work for all regions, just reference by multiple region names.
     * <p>
     * The constructor exports this object, making it available to receive incoming calls. The
     * calback port is anonymous unless a local port vlaue was specified in the configurtion.
     * @param irca
     * @param cacheMgr
     */
    public RemoteCacheListener( IRemoteCacheAttributes irca, ICompositeCacheManager cacheMgr )
    {
        this.irca = irca;

        this.cacheMgr = cacheMgr;

        // Export this remote object to make it available to receive incoming
        // calls, using an anonymous port unless the local port is specified.
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
            log.error( "Problem exporting object.", ex );
            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * Deregisters itself.
     * <p>
     * @throws IOException
     */
    public synchronized void dispose()
        throws IOException
    {
        if ( !disposed )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Unexporting listener." );
            }
            try
            {
                UnicastRemoteObject.unexportObject( this, true );
            }
            catch ( RemoteException ex )
            {
                log.error( "Problem unexporting the listener.", ex );
                throw new IllegalStateException( ex.getMessage() );
            }
            disposed = true;
        }
    }

    /**
     * Let the remote cache set a listener_id. Since there is only one listerenr for all the regions
     * and every region gets registered? the id shouldn't be set if it isn't zero. If it is we
     * assume that it is a reconnect.
     * <p>
     * @param id The new listenerId value
     * @throws IOException
     */
    public void setListenerId( long id )
        throws IOException
    {
        listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = [" + id + "]" );
        }
    }

    /**
     * Gets the listenerId attribute of the RemoteCacheListener object. This is stored int he
     * object. The RemoteCache object contains a reference to the listener and get the id this way.
     * @return The listenerId value
     * @throws IOException
     */
    public long getListenerId()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "get listenerId = [" + listenerId + "]" );
        }
        return listenerId;

    }

    /**
     * Gets the remoteType attribute of the RemoteCacheListener object
     * @return The remoteType value
     * @throws IOException
     */
    public int getRemoteType()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "getRemoteType = [" + irca.getRemoteType() + "]" );
        }
        return irca.getRemoteType();
    }

    /**
     * If this is configured to remove on put, then remove the element since it has been updated
     * elsewhere. cd should be incomplete for faster transmission. We don't want to pass data only
     * invalidation. The next time it is used the local cache will get the new version from the
     * remote store.
     * <p>
     * If remove on put is not ocnfigured, then update the item.
     * @param cb
     * @throws IOException
     */
    public void handlePut( ICacheElement cb )
        throws IOException
    {
        if ( irca.getRemoveUponRemotePut() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "PUTTING ELEMENT FROM REMOTE, (  invalidating ) " );
            }
            handleRemove( cb.getCacheName(), cb.getKey() );
        }
        else
        {
            puts++;
            if ( log.isDebugEnabled() )
            {
                log.debug( "PUTTING ELEMENT FROM REMOTE, ( updating ) " );
                log.debug( "cb = " + cb );

                if ( puts % 100 == 0 )
                {
                    log.debug( "puts = " + puts );
                }
            }

            ensureCacheManager();
            CompositeCache cache = cacheMgr.getCache( cb.getCacheName() );

            // Eventually the instance of will not be necessary.
            if ( cb != null && cb instanceof ICacheElementSerialized )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Object needs to be deserialized." );
                }
                try
                {
                    cb = SerializationConversionUtil.getDeSerializedCacheElement( (ICacheElementSerialized) cb,
                                                                                  this.elementSerializer );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Deserialized result = " + cb );
                    }
                }
                catch ( IOException e )
                {
                    throw e;
                }
                catch ( ClassNotFoundException e )
                {
                    log.error( "Received a serialized version of a class that we don't know about.", e );
                }
            }

            cache.localUpdate( cb );
        }

        return;
    }

    /**
     * Calls localRemove on the CompositeCache.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemove(java.lang.String,
     *      java.io.Serializable)
     */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        removes++;
        if ( log.isDebugEnabled() )
        {
            if ( removes % 100 == 0 )
            {
                log.debug( "removes = " + removes );
            }

            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        ensureCacheManager();
        CompositeCache cache = cacheMgr.getCache( cacheName );

        cache.localRemove( key );
    }

    /**
     * Calls localRemoveAll on the CompositeCache.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemoveAll(java.lang.String)
     */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }
        ensureCacheManager();
        CompositeCache cache = cacheMgr.getCache( cacheName );
        cache.localRemoveAll();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleDispose(java.lang.String)
     */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }
        // TODO consider what to do here, we really don't want to
        // dispose, we just want to disconnect.
        // just allow the cache to go into error recovery mode.
        // getCacheManager().freeCache( cacheName, true );
    }

    /**
     * Gets the cacheManager attribute of the RemoteCacheListener object. This is one of the few
     * places that force the cache to be a singleton.
     */
    protected void ensureCacheManager()
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

    /**
     * This is for debugging. It allows the remote server to log the address of clients.
     */
    public String getLocalHostAddress()
        throws IOException
    {
        return localHostName;
    }

    /**
     * For easier debugging.
     * <p>
     * @return Basic info on this listener.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n RemoteCacheListener: " );
        buf.append( "\n RemoteHost = " + irca.getRemoteHost() );
        buf.append( "\n RemotePort = " + irca.getRemotePort() );
        buf.append( "\n ListenerId = " + listenerId );
        return buf.toString();
    }
}
