package org.apache.jcs.auxiliary.lateral.xmlrpc;

/*
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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.auxiliary.lateral.xmlrpc.behavior.ILateralCacheXMLRPCListener;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id: LateralCacheXMLRPCListener.java,v 1.8 2002/02/15 04:33:37 jtaylor
 *      Exp $
 */
public class LateralCacheXMLRPCListener implements ILateralCacheXMLRPCListener, Serializable
{
    private final static Log log =
        LogFactory.getLog( LateralCacheXMLRPCListener.class );

    /** Description of the Field */
    protected static transient CacheHub cacheMgr;
    /** Description of the Field */
    protected final static HashMap instances = new HashMap();

    // instance vars
    private LateralXMLRPCReceiver receiver;
    private ILateralCacheAttributes ilca;
    private boolean inited = false;


    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param ilca
     */
    protected LateralCacheXMLRPCListener( ILateralCacheAttributes ilca )
    {
        this.ilca = ilca;
    }


    /** Description of the Method */
    public void init()
    {
        try
        {
            // need to connect based on type
            //ILateralCacheListener ilcl = this;
            //p( "in init, ilcl = " + ilcl );
            receiver = new LateralXMLRPCReceiver( ilca, this );
            //Thread t = new Thread( receiver );
            //t.start();
        }
        catch ( RuntimeException ex )
        {
            log.error( ex );
            throw new IllegalStateException( ex.getMessage() );
        }
        inited = true;
    }


    /**
     * let the lateral cache set a listener_id. Since there is only one
     * listerenr for all the regions and every region gets registered? the id
     * shouldn't be set if it isn't zero. If it is we assume that it is a
     * reconnect.
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException
    {
        LateralCacheInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }


    /**
     * Gets the listenerId attribute of the LateralCacheXMLRPCListener object
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
            log.debug( "get listenerId = " + LateralCacheInfo.listenerId );
        }
        return LateralCacheInfo.listenerId;
    }


    /**
     * Gets the instance attribute of the LateralCacheXMLRPCListener class
     *
     * @return The instance value
     */
    public static ILateralCacheListener getInstance( ILateralCacheAttributes ilca )
    {
        //throws IOException, NotBoundException
        ILateralCacheListener ins = ( ILateralCacheListener ) instances.get( String.valueOf( ilca.getTcpListenerPort() ) );
        if ( ins == null )
        {
            synchronized ( LateralCacheXMLRPCListener.class )
            {
                if ( ins == null )
                {
                    ins = new LateralCacheXMLRPCListener( ilca );
                    ins.init();
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "created new listener " + ilca.getTcpListenerPort() );
                }
                instances.put( String.valueOf( ilca.getTcpListenerPort() ), ins );
            }
        }
        return ins;
    }


    //////////////////////////// implements the ILateralCacheListener interface. //////////////
    /** */
    public void handlePut( ICacheElement cb )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "PUTTING ELEMENT FROM LATERAL" );
        }
        getCacheManager();
        ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cb.getCacheName() );
        cache.update( cb, CacheConstants.REMOTE_INVOKATION );
        //handleRemove(cb.getCacheName(), cb.getKey());
    }


    /** Description of the Method */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCacheManager();
        // interface limitation here

        ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cacheName );
        cache.remove( key, CacheConstants.REMOTE_INVOKATION );
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

    /** Test get implementation. */
    public Serializable handleGet( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleGet> cacheName=" + cacheName + ", key = " + key );
        }
        getCacheManager();
        ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cacheName );
        // get container
        return cache.get( key, CacheConstants.REMOTE_INVOKATION );
    }

    /** Description of the Method */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }
        CacheHub cm = ( CacheHub ) cacheMgr;
        cm.freeCache( cacheName, CacheConstants.REMOTE_INVOKATION );
    }


    // override for new funcitonality
    /**
     * Gets the cacheManager attribute of the LateralCacheXMLRPCListener object
     */
    protected void getCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CacheHub.getInstance();
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
