package org.apache.jcs.auxiliary.remote;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.behavior.IZombie;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import org.apache.jcs.utils.serialization.SerializationConversionUtil;
import org.apache.jcs.utils.serialization.StandardSerializer;
import org.apache.jcs.utils.threadpool.ThreadPool;
import org.apache.jcs.utils.threadpool.ThreadPoolManager;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Client proxy for an RMI remote cache. This handles gets, updates, and
 * removes. It also initiates failover recovery when an error is encountered.
 * 
 */
public class RemoteCache
    implements ICache
{
    private static final long serialVersionUID = -5329231850422826460L;

    private final static Log log = LogFactory.getLog( RemoteCache.class );

    final String cacheName;

    private IRemoteCacheAttributes irca;

    private IRemoteCacheService remote;

    private IRemoteCacheListener listener;

    IElementAttributes attr = null;

    private ThreadPool pool = null;

    private boolean usePoolForGet = false;

    private IElementSerializer elementSerializer = new StandardSerializer();

    /**
     * Constructor for the RemoteCache object. This object communicates with a
     * remote cache server. One of these exists for each region. This also holds
     * a reference to a listener. The same listener is used for all regions for
     * one remote server. Holding a reference to the listener allows this object
     * to know the listener id assigned by the remote cache.
     * 
     * @param cattr
     * @param remote
     * @param listener
     */
    public RemoteCache( IRemoteCacheAttributes cattr, IRemoteCacheService remote, IRemoteCacheListener listener )
    {
        this.irca = cattr;
        this.cacheName = cattr.getCacheName();
        this.remote = remote;
        this.listener = listener;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Construct> cacheName=" + cattr.getCacheName() );
            log.debug( "irca = " + irca );
            log.debug( "remote = " + remote );
            log.debug( "listener = " + listener );
        }

        // use a pool if it is greater than 0
        if ( log.isDebugEnabled() )
        {
            log.debug( "GetTimeoutMillis() = " + irca.getGetTimeoutMillis() );
        }

        if ( irca.getGetTimeoutMillis() > 0 )
        {
            pool = ThreadPoolManager.getInstance().getPool( irca.getThreadPoolName() );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Thread Pool = " + pool );
            }
            if ( pool != null )
            {
                usePoolForGet = true;
                pool.getPool().setThreadFactory( new MyThreadFactory() );
            }
        }

        try
        {
            // Don't set a socket factory if the setting is -1
            if ( irca.getRmiSocketFactoryTimeoutMillis() > 0 )
            {
                // TODO make configurable.
                // use this socket factory to add a timeout.
                RMISocketFactory.setSocketFactory( new RMISocketFactory()
                {
                    public Socket createSocket( String host, int port )
                        throws IOException
                    {
                        Socket socket = new Socket( host, port );
                        socket.setSoTimeout( irca.getRmiSocketFactoryTimeoutMillis() );
                        socket.setSoLinger( false, 0 );
                        return socket;
                    }

                    public ServerSocket createServerSocket( int port )
                        throws IOException
                    {
                        return new ServerSocket( port );
                    }
                } );
            }
        }
        catch ( Exception e )
        {
            // TODO change this so that we only try to do it once. Otherwise we
            // genreate errors for each region on construction.
            log.info( e.getMessage() );
        }
    }

    /**
     * Sets the attributes attribute of the RemoteCache object
     * 
     * @param attr
     *            The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = attr;
    }

    /**
     * Gets the attributes attribute of the RemoteCache object
     * 
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return this.attr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        if ( true )
        {
            if ( !this.irca.getGetOnly() )
            {
                try
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "sending item to remote server" );
                    }

                    // convert so we don't have to know about the object on the
                    // other end.
                    ICacheElementSerialized serialized = SerializationConversionUtil
                        .getSerializedCacheElement( ce, this.elementSerializer );

                    remote.update( serialized, getListenerId() );
                }
                catch ( NullPointerException npe )
                {
                    log.error( "npe for ce = " + ce + "ce.attr = " + ce.getElementAttributes(), npe );
                    return;
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to put " + ce.getKey() + " to " + ce.getCacheName() );
                }
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "get only mode, not sending to remote server" );
                }
            }
        }
    }

    /**
     * Synchronously get from the remote cache; if failed, replace the remote
     * handle with a zombie.
     * 
     * Use threadpool to timeout is a value is set for GetTimeoutMillis
     * 
     * @param key
     * @return ICacheElement, a wrapper around the key, value, and attributes
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement retVal = null;

        try
        {
            if ( usePoolForGet )
            {
                retVal = getUsingPool( sanitized( key ) );
            }
            else
            {
                retVal = remote.get( cacheName, sanitized( key ), getListenerId() );
            }

            // Eventually the instance of will not be necessary.
            if ( retVal != null && retVal instanceof ICacheElementSerialized )
            {
                retVal = SerializationConversionUtil.getDeSerializedCacheElement( (ICacheElementSerialized) retVal,
                                                                                  this.elementSerializer );
            }
        }
        catch ( Exception ex )
        {
            handleException( ex, "Failed to get " + key + " from " + cacheName );
        }

        return retVal;
    }

    /**
     * This allows gets to timeout in case of remote server machine shutdown.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public ICacheElement getUsingPool( final Serializable key )
        throws IOException
    {
        int timeout = irca.getGetTimeoutMillis();

        try
        {
            FutureResult future = new FutureResult();
            Runnable command = future.setter( new Callable()
            {
                public Object call()
                    throws IOException
                {
                    return remote.get( cacheName, key, getListenerId() );
                }
            } );

            // execute using the pool
            pool.execute( command );

            // used timed get in order to timeout
            ICacheElement ice = (ICacheElement) future.timedGet( timeout );
            if ( log.isDebugEnabled() )
            {
                if ( ice == null )
                {
                    log.debug( "nothing found in remote cache" );
                }
                else
                {
                    log.debug( "found item in remote cache" );
                }
            }
            return ice;
        }
        catch ( TimeoutException te )
        {
            log.warn( "TimeoutException, Get Request timed out after " + timeout );
            throw new IOException( "Get Request timed out after " + timeout );
        }
        catch ( InterruptedException ex )
        {
            log.warn( "InterruptedException, Get Request timed out after " + timeout );
            throw new IOException( "Get Request timed out after " + timeout );
        }
        catch ( InvocationTargetException ex )
        {
            // assume that this is an IOException thrown by the callable.
            log.error( "InvocationTargetException, Assuming an IO exception thrown in the background.", ex );
            throw new IOException( "Get Request timed out after " + timeout );
        }
    }

    /**
     * Returns all the keys for a group.
     * 
     * @param groupName
     * @return
     * @throws java.rmi.RemoteException
     */
    public Set getGroupKeys( String groupName )
        throws java.rmi.RemoteException
    {
        return remote.getGroupKeys( cacheName, groupName );
    }

    /**
     * Wraps a non JDK object into a MarshalledObject, so that we can avoid
     * unmarshalling the real object on the remote side. This technique offers
     * the benefit of surviving incompatible class versions without the need to
     * restart the remote cache server.
     * 
     * @param s
     * @return A sanitized version of the key.
     * @throws IOException
     */
    private Serializable sanitized( Serializable s )
        throws IOException
    {
        // In the unlikely case when the passed in object is a MarshalledObjct,
        // we again wrap
        // it into a new MarsahlledObject for "escape" purposes during the get
        // operation.
        // return s.getClass().getName().startsWith("java.") && !(s instanceof
        // MarshalledObject) ? s : new MarshalledObject(s);

        // avoid this step for now, [problem with group id wrapper]
        return s;
    }

    /**
     * Synchronously remove from the remote cache; if failed, replace the remote
     * handle with a zombie.
     * 
     * @param key
     * @return boolean, whether or not the item was removed
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        if ( true )
        {

            if ( !this.irca.getGetOnly() )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "remove> key=" + key );
                }
                try
                {
                    remote.remove( cacheName, sanitized( key ), getListenerId() );
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to remove " + key + " from " + cacheName );
                }
            }
        }
        return false;
    }

    /**
     * Synchronously removeAll from the remote cache; if failed, replace the
     * remote handle with a zombie.
     * 
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        if ( true )
        {
            if ( !this.irca.getGetOnly() )
            {
                try
                {
                    remote.removeAll( cacheName, getListenerId() );
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to remove all from " + cacheName );
                }
            }
        }
    }

    /**
     * Synchronously dispose the remote cache; if failed, replace the remote
     * handle with a zombie.
     * 
     * @throws IOException
     */
    public void dispose()
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Disposing of remote cache" );
        }
        try
        {
            remote.dispose( cacheName );
        }
        catch ( Exception ex )
        {
            log.error( "couldn't dispose", ex );
            handleException( ex, "Failed to dispose " + cacheName );
        }
    }

    /**
     * Returns the cache status. An error status indicates the remote connection
     * is not available.
     * 
     * @return The status value
     */
    public int getStatus()
    {
        return remote instanceof IZombie ? CacheConstants.STATUS_ERROR : CacheConstants.STATUS_ALIVE;
    }

    /**
     * Gets the stats attribute of the RemoteCache object
     * 
     * @return The stats value
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * 
     * @return IStats object
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Remote Cache No Wait" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Remote Host:Port" );
        se.setData( this.irca.getRemoteHost() + ":" + this.irca.getRemotePort() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Remote Type" );
        se.setData( this.irca.getRemoteTypeName() + "" );
        elems.add( se );

        if ( this.irca.getRemoteType() == IRemoteCacheAttributes.CLUSTER )
        {
            // somethign cluster specific
        }

        // no data gathered here

        se = new StatElement();
        se.setName( "UsePoolForGet" );
        se.setData( "" + usePoolForGet );
        elems.add( se );

        if ( pool != null )
        {
            se = new StatElement();
            se.setName( "Pool Size" );
            se.setData( "" + pool.getPool().getPoolSize() );
            elems.add( se );

            se = new StatElement();
            se.setName( "Maximum Pool Size" );
            se.setData( "" + pool.getPool().getMaximumPoolSize() );
            elems.add( se );
        }

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

    /**
     * Returns the current cache size.
     * 
     * @return The size value
     */
    public int getSize()
    {
        return 0;
    }

    /**
     * Gets the cacheType attribute of the RemoteCache object
     * 
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return REMOTE_CACHE;
    }

    /**
     * Gets the cacheName attribute of the RemoteCache object
     * 
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Replaces the current remote cache service handle with the given handle.
     * 
     * @param remote
     *            IRemoteCacheService -- the remote server or proxy to the
     *            remote server
     */
    public void fixCache( IRemoteCacheService remote )
    {
        this.remote = remote;
        return;
    }

    /**
     * Handles exception by disabling the remote cache service before
     * re-throwing the exception in the form of an IOException.
     * 
     * @param ex
     * @param msg
     * @throws IOException
     */
    private void handleException( Exception ex, String msg )
        throws IOException
    {
        log.error( "Disabling remote cache due to error " + msg );
        log.error( ex );

        remote = new ZombieRemoteCacheService();
        // may want to flush if region specifies
        // Notify the cache monitor about the error, and kick off the recovery
        // process.
        RemoteCacheMonitor.getInstance().notifyError();

        // initiate failover if local
        RemoteCacheNoWaitFacade rcnwf = (RemoteCacheNoWaitFacade) RemoteCacheFactory.getFacades()
            .get( irca.getCacheName() );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Initiating failover, rcnf = " + rcnwf );
        }

        if ( rcnwf != null && rcnwf.rca.getRemoteType() == RemoteCacheAttributes.LOCAL )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Found facade, calling failover" );
            }
            // may need to remove the noWait index here. It will be 0 if it is
            // local since there is only 1 possible listener.
            rcnwf.failover( 0 );
        }

        if ( ex instanceof IOException )
        {
            throw (IOException) ex;
        }
        throw new IOException( ex.getMessage() );
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return irca;
    }

    /**
     * let the remote cache set a listener_id. Since there is only one listerenr
     * for all the regions and every region gets registered? the id shouldn't be
     * set if it isn't zero. If it is we assume that it is a reconnect.
     * 
     * @param id
     *            The new listenerId value
     */
    public void setListenerId( long id )
    {
        try
        {
            listener.setListenerId( id );

            if ( log.isDebugEnabled() )
            {
                log.debug( "set listenerId = " + id );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem setting listenerId", e );
        }
    }

    /**
     * Gets the listenerId attribute of the RemoteCacheListener object
     * 
     * @return The listenerId value
     */
    public long getListenerId()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "get listenerId = " + listener.getListenerId() );
            }
            return listener.getListenerId();
        }
        catch ( Exception e )
        {
            log.error( "Problem setting listenerId", e );
        }
        return -1;
    }

    /**
     * Allows other member of this package to access the listerner. This is
     * mainly needed for deregistering alistener.
     * 
     * @return IRemoteCacheListener, the listener for this remote server
     */
    protected IRemoteCacheListener getListener()
    {
        return listener;
    }

    /**
     * @param elementSerializer
     *            The elementSerializer to set.
     */
    public void setElementSerializer( IElementSerializer elementSerializer )
    {
        this.elementSerializer = elementSerializer;
    }

    /**
     * @return Returns the elementSerializer.
     */
    public IElementSerializer getElementSerializer()
    {
        return elementSerializer;
    }

    /**
     * Debugging info.
     * 
     * @return basic info about the RemoteCache
     */
    public String toString()
    {
        return "RemoteCache: " + cacheName + " attributes = " + irca;
    }

    /**
     * Allows us to set the daemon status on the clockdaemon
     * 
     * @author aaronsm
     * 
     */
    class MyThreadFactory
        implements ThreadFactory
    {
        /*
         * (non-Javadoc)
         * 
         * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            t.setDaemon( true );
            return t;
        }
    }
}