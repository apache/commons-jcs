package org.apache.jcs.auxiliary.remote.server;

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
 * This listener class is for inter cache commumication.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheServerListener
     implements IRemoteCacheListener, IRemoteCacheConstants, Serializable
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheServerListener.class );

    /** Description of the Field */
    protected static transient CompositeCacheManager cacheMgr;

    /** Description of the Field */
    protected static IRemoteCacheListener instance;
    /** Description of the Field */
    protected IRemoteCacheAttributes irca;

    /** Description of the Field */
    private int puts = 0;
    private int removes = 0;


    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param irca
     */
    protected RemoteCacheServerListener( IRemoteCacheAttributes irca )
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
        RemoteCacheServerInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }


    /**
     * Gets the listenerId attribute of the RemoteCacheServerListener object
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
            log.debug( "get listenerId = " + RemoteCacheServerInfo.listenerId );
        }
        return RemoteCacheServerInfo.listenerId;
    }


    /**
     * Gets the remoteType attribute of the RemoteCacheServerListener object
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
     * Gets the instance attribute of the RemoteCacheServerListener class
     *
     * @return The instance value
     */
    public static IRemoteCacheListener getInstance( IRemoteCacheAttributes irca )
    {
        //throws IOException, NotBoundException
        if ( instance == null )
        {
            synchronized ( RemoteCacheServerListener.class )
            {
                if ( instance == null )
                {
                    instance = new RemoteCacheServerListener( irca );
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
        if ( log.isDebugEnabled() )
        {
            puts++;
            if ( puts % 100 == 0 )
            {
                log.debug( "puts = " + puts );
            }
        }

        CompositeCache cache = cacheMgr.getCache( irca.getCacheName() );
        cache.localUpdate( cb );
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
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCacheManager();
        // interface limitation here

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
        cm.freeCache( cacheName, true );
    }


    // override for new funcitonality
    /**
     * Gets the cacheManager attribute of the RemoteCacheServerListener object
     */
    protected void getCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();

            if ( log.isDebugEnabled() )
            {
                log.debug( "had to get cacheMgr: " + cacheMgr );
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
