package org.apache.jcs.auxiliary.lateral;

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

import java.util.HashMap;
import java.util.Set;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheConstants;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Lateral distributor. Returns null on get. Net search not implemented.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCache implements ICache
{
    private final static Log log =
        LogFactory.getLog( LateralCache.class );

    private static int numCreated = 0;

    IElementAttributes attr = null;

    private HashMap keyHash;
    // not synchronized to maximize concurrency.

    // generalize this, use another interface
    ILateralCacheAttributes cattr;

    final String cacheName;

    /** either http, socket.udp, or socket.tcp can set in config */
    private ILateralCacheService lateral;

    /**
     * Constructor for the LateralCache object
     *
     * @param cattr
     * @param lateral
     */
    protected LateralCache( ILateralCacheAttributes cattr, ILateralCacheService lateral )
    {
        this.cacheName = cattr.getCacheName();
        this.cattr = cattr;
        this.lateral = lateral;
    }


    /**
     * Constructor for the LateralCache object
     *
     * @param cattr
     */
    protected LateralCache( ILateralCacheAttributes cattr )
    {
        this.cacheName = cattr.getCacheName();
        //this.servers = servers;

        this.cattr = cattr;
    }


    /** Description of the Method */
    public String toString()
    {
        return "LateralCache: " + cattr.getCacheName();
    }

    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "update: lateral = " + lateral + ", " +
                    "LateralCacheInfo.listenerId = " +
                    LateralCacheInfo.listenerId );
            }
            lateral.update( ce, LateralCacheInfo.listenerId );
        }
        catch ( NullPointerException npe )
        {
            log.error( "Failure updating lateral", npe );
            return;
        }
        catch ( Exception ex )
        {
            handleException( ex, "Failed to put " + ce.getKey() + " to " + ce.getCacheName() );
        }
    }
    // end update

    /** Returns null. The performace costs are too great. */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement obj = null;

        if ( cattr.getPutOnlyMode() )
        {
            return null;
        }
        else
        {
            try
            {
                obj = lateral.get( cacheName, key );
            }
            catch ( Exception e )
            {
                log.error( e );
                // do something with this
            }
        }
        return obj;
    }

    public Set getGroupKeys(String groupName)
    {
        return lateral.getGroupKeys(cacheName, groupName);
    }


    /**
     * Synchronously remove from the remote cache; if failed, replace the remote
     * handle with a zombie.
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        log.debug( "removing key:" + key );

        try
        {
            //DeleteLateralCacheMulticaster dlcm = new DeleteLateralCacheMulticaster( cattr.getCacheName(), (String)key, cattr.getLateralCacheAddrs(), cattr.getLateralDeleteServlet()  );
            //dlcm.multicast();
            lateral.remove( cacheName, key, LateralCacheInfo.listenerId );
        }
        catch ( Exception ex )
        {
            handleException( ex, "Failed to remove " + key + " from " + cattr.getCacheName() );
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
        try
        {
            //DeleteLateralCacheMulticaster dlcm = new DeleteLateralCacheMulticaster( cattr.getCacheName(), "ALL", cattr.getLateralCacheAddrs(), cattr.getLateralDeleteServlet()  );
            //dlcm.multicast();
            lateral.removeAll( cacheName, LateralCacheInfo.listenerId );
        }
        catch ( Exception ex )
        {
            handleException( ex, "Failed to remove all from " + cattr.getCacheName() );
        }
    }


    /** Synchronously dispose the cache. Not sure we want this. */
    public void dispose()
        throws IOException
    {
        log.debug( "Disposing of lateral cache" );

        ///* HELP: This section did nothing but generate compilation warnings.
        // TODO: may limit this funcionality. It is dangerous.
        // asmuts -- Added functionality to help with warnings.  I'm not getting any.
        try
        {
            lateral.dispose( cattr.getCacheName() );
            // Should remove connection
        }
        catch ( Exception ex )
        {
            log.error( "Couldn't dispose", ex );
            handleException( ex, "Failed to dispose " + cattr.getCacheName() );
        }
        //*/
    }

    /**
     * Returns the cache status.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return this.lateral instanceof IZombie ? CacheConstants.STATUS_ERROR : CacheConstants.STATUS_ALIVE;
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
     * Gets the cacheType attribute of the LateralCache object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.LATERAL_CACHE;
    }


    /**
     * Gets the cacheName attribute of the LateralCache object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }


    /** Not yet sure what to do here. */
    private void handleException( Exception ex, String msg )
        throws IOException
    {

        log.error( "Disabling lateral cache due to error " + msg, ex );

        lateral = new ZombieLateralCacheService();
        // may want to flush if region specifies
        // Notify the cache monitor about the error, and kick off the recovery process.
        LateralCacheMonitor.getInstance().notifyError();

        // could stop the net serach if it is built and try to reconnect?
        if ( ex instanceof IOException )
        {
            throw ( IOException ) ex;
        }
        throw new IOException( ex.getMessage() );
    }


    /**
     * Replaces the current remote cache service handle with the given handle.
     */
    public void fixCache( ILateralCacheService lateral )
    {
        this.lateral = lateral;
        return;
    }
}
