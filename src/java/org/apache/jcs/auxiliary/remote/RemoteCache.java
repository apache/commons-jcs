package org.apache.jcs.auxiliary.remote;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.Serializable;

import java.util.Set;

import org.apache.jcs.access.exception.ObjectNotFoundException;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheConstants;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Client proxy for an RMI remote cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCache implements ICache
{
    private final static Log log =
        LogFactory.getLog( RemoteCache.class );

    final String cacheName;
    private IRemoteCacheService remote;
    private IRemoteCacheAttributes irca;

    IElementAttributes attr = null;

    /** Description of the Method */
    public String toString()
    {
        return "RemoteCache: " + cacheName;
    }


    // was public but need to access from server
    /**
     * Constructor for the RemoteCache object
     *
     * @param cattr
     * @param remote
     */
    public RemoteCache( IRemoteCacheAttributes cattr, IRemoteCacheService remote )
    {
        this.irca = cattr;
        this.cacheName = cattr.getCacheName();
        this.remote = remote;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Construct> cacheName=" + cattr.getCacheName() );
            log.debug( "irca = " + irca.toString() );
        }
        /*
         * TODO
         * should be done by the remote cache, not the job of the hub manager
         * Set up the idle period for the RemoteCacheMonitor.
         * long monPeriod = 0;
         * try {
         * monPeriod = Long.parseLong(props.getProperty("remote.monitor.idle.period", "0"));
         * } catch(NumberFormatException ex) {
         * log.warn(ex.getMessage());
         * }
         * RemoteCacheMonitor.setIdlePeriod(monPeriod);
         */
    }


    /**
     * Sets the attributes attribute of the RemoteCache object
     *
     * @param attr The new attributes value
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

    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        // Do not communicate with cluster except via server.
        // separates the remote from the local.  Must run a server to
        // cluster, else it can be run inside a local.
        //if ( this.irca.getRemoteType() != irca.CLUSTER )
        if ( true )
        {

            if ( !this.irca.getGetOnly() )
            {
                try
                {
                    remote.update( ce, RemoteCacheInfo.listenerId );
                }
                catch ( NullPointerException npe )
                {
                    log.error( "npe for ce = " + ce + "ce.attr = " + ce.getElementAttributes(), npe );
                    return;
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to put " + ce.getKey() + " to " + ce.getCacheName() );
                    //throw ex;
                }
            }
            else
            {
                //p( "get only mode, irca = " + irca.toString() );
            }
        }
    }


    /**
     * Synchronously get from the remote cache; if failed, replace the remote
     * handle with a zombie.
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        try
        {
            return remote.get( cacheName, sanitized( key ) );
        }
        catch ( ObjectNotFoundException one )
        {
            log.debug( "didn't find element " + key + " in remote" );
            return null;
        }
        catch ( Exception ex )
        {
            handleException( ex, "Failed to get " + key + " from " + cacheName );
            //throw ex;
            return null;
            // never executes; just keep the compiler happy.
        }
    }

    public Set getGroupKeys(String groupName) throws java.rmi.RemoteException
    {
        return remote.getGroupKeys(cacheName, groupName);
    }

    /**
     * Wraps a non JDK object into a MarshalledObject, so that we can avoid
     * unmarshalling the real object on the remote side. This technique offers
     * the benefit of surviving incompatible class versions without the need to
     * restart the remote cache server.
     */
    private Serializable sanitized( Serializable s )
        throws IOException
    {
        // In the unlikely case when the passed in object is a MarshalledObjct, we again wrap
        // it into a new MarsahlledObject for "escape" purposes during the get operation.
        //return s.getClass().getName().startsWith("java.") && !(s instanceof MarshalledObject) ? s : new MarshalledObject(s);

        // avoid this step for now, [problem with group id wrapper]
        return s;
    }

    /**
     * Synchronously remove from the remote cache; if failed, replace the remote
     * handle with a zombie.
     */
    public boolean remove( Serializable key )
        throws IOException
    {

        // Do not communicate with cluster except via server.
        // separates the remote from the local.  Must run a server to
        // cluster, else it can be run inside a local.
        //if ( this.irca.getRemoteType() != irca.CLUSTER )
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
                    remote.remove( cacheName, sanitized( key ), RemoteCacheInfo.listenerId );
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to remove " + key + " from " + cacheName );
                    //throw ex;
                }
            }
        }
        return false;
    }


    /**
     * Synchronously removeAll from the remote cache; if failed, replace the
     * remote handle with a zombie.
     */
    public void removeAll()
        throws IOException
    {

        // Do not communicate with cluster except via server.
        // separates the remote from the local.  Must run a server to
        // cluster, else it can be run inside a local.
        if ( this.irca.getRemoteType() != irca.CLUSTER )
        {

            if ( !this.irca.getGetOnly() )
            {
                try
                {
                    remote.removeAll( cacheName, RemoteCacheInfo.listenerId );
                }
                catch ( Exception ex )
                {
                    handleException( ex, "Failed to remove all from " + cacheName );
                    //throw ex;
                }
            }
        }
    }


    /**
     * Synchronously dispose the remote cache; if failed, replace the remote
     * handle with a zombie.
     */
    public void dispose()
        throws IOException
    {
//    remote.freeCache(cacheName);
        log.debug( "disposing of remote cache" );
        try
        {
            remote.dispose( cacheName );
        }
        catch ( Exception ex )
        {
            log.error( "couldn't dispose" );
            handleException( ex, "Failed to dispose " + cacheName );
            //remote = null;
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
        return "cacheName = " + cacheName;
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
     */
    public void fixCache( IRemoteCacheService remote )
    {
        this.remote = remote;
        return;
    }


    /**
     * Handles exception by disabling the remote cache service before
     * re-throwing the exception in the form of an IOException.
     */
    private void handleException( Exception ex, String msg )
        throws IOException
    {
        log.error( "Disabling remote cache due to error " + msg );
        //log.error(ex);
        log.error( ex.toString() );
        remote = new ZombieRemoteCacheService();
        // may want to flush if region specifies
        // Notify the cache monitor about the error, and kick off the recovery process.
        RemoteCacheMonitor.getInstance().notifyError();

        // initiate failover if local
        RemoteCacheNoWaitFacade rcnwf = ( RemoteCacheNoWaitFacade ) RemoteCacheFactory.facades.get( irca.getCacheName() );
        log.debug( "Initiating failover, rcnf = " + rcnwf );
        if ( rcnwf != null && rcnwf.rca.getRemoteType() == rcnwf.rca.LOCAL )
        {
            log.debug( "found facade calling failover" );
            // may need to remove the noWait index here. It will be 0 if it is local
            // since there is only 1 possible listener.
            rcnwf.failover( 0 );
        }

        if ( ex instanceof IOException )
        {
            throw ( IOException ) ex;
        }
        throw new IOException( ex.getMessage() );
    }
}
