package org.apache.commons.jcs.auxiliary.lateral.javagroups;

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

import org.apache.commons.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior.ILateralCacheJGListener;
import org.apache.commons.jcs.engine.behavior.ICache;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * JavaGroups listener. Good for distributing cache data accross multiple vms on
 * the same machine. You also don't need to know the number of listerners for
 * configuration.
 *
 */
public class LateralCacheJGListener
    implements ILateralCacheJGListener, Serializable
{
    private static final Log log = LogFactory.getLog( LateralCacheJGListener.class );

    /**
     * Description of the Field
     */
    protected transient ICompositeCacheManager cacheMgr;

    /**
     * Description of the Field
     */
    protected static final HashMap instances = new HashMap();

    // instance vars
    private LateralJGReceiver receiver;

    private ILateralCacheAttributes ilca;

    private int puts = 0;

    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param ilca
     */
    protected LateralCacheJGListener( ILateralCacheAttributes ilca )
    {
        this.ilca = ilca;
    }

    /**
     * Description of the Method
     */
    public void init()
    {
        try
        {
            // need to connect based on type
            //ILateralCacheListener ilcl = this;
            receiver = new LateralJGReceiver( ilca, this );
            Thread t = new Thread( receiver );
            t.start();
        }
        catch ( Exception ex )
        {
            log.error( ex );
            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * let the lateral cache set a listener_id. Since there is only one
     * listerenr for all the regions and every region gets registered? the id
     * shouldn't be set if it isn't zero. If it is we assume that it is a
     * reconnect.
     *
     * @param id
     *            The new listenerId value
     * @throws IOException
     */
    public void setListenerId( long id )
        throws IOException
    {
        LateralCacheInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }

    /**
     * Gets the listenerId attribute of the LateralCacheJGListener object
     *
     * @return The listenerId value
     * @throws IOException
     */
    public long getListenerId()
        throws IOException
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "get listenerId = " + LateralCacheInfo.listenerId );
        }
        return LateralCacheInfo.listenerId;
    }

    /**
     * Gets the instance attribute of the LateralCacheJGListener class
     *
     * @return The instance value
     * @param ilca
     * @param cacheMgr
     */
    public static ILateralCacheListener getInstance( ILateralCacheAttributes ilca, ICompositeCacheManager cacheMgr )
    {

        //throws IOException, NotBoundException
        ILateralCacheListener ins = (ILateralCacheListener) instances.get( ilca.getJGChannelProperties() );

        synchronized ( LateralCacheJGListener.class )
        {
            if ( ins == null )
            {
                ins = new LateralCacheJGListener( ilca );

                ins.setCacheManager( cacheMgr );

                ins.init();
            }
            if ( log.isInfoEnabled() )
            {
                log.info( "created new listener " + ilca.getJGChannelProperties() );
            }
            instances.put( ilca.getJGChannelProperties(), ins );
        }

        return ins;
    }

    //////////////////////////// implements the ILateralCacheListener
    // interface. //////////////
    /**
     * @param element
     * @param cb
     * @throws IOException
     */
    public void handlePut( ICacheElement<K, V> element )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "PUTTING ELEMENT FROM LATERAL" );
        }

        puts++;
        if ( log.isInfoEnabled() )
        {
            if ( puts % 100 == 0 )
            {
                log.info( "puts = " + puts );
            }
        }

        getCache( element.getCacheName() ).localUpdate( element );

    }

    /**
     * Description of the Method
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void handleRemove( String cacheName, K key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCache( cacheName ).localRemove( key );
    }

    /**
     * Description of the Method
     *
     * @param cacheName
     * @throws IOException
     */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }

        ICache cache = getCache( cacheName );
        cache.removeAll();
    }

    /**
     * Test get implementation.
     *
     * @return
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public Serializable handleGet( String cacheName, K key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleGet> cacheName=" + cacheName + ", key = " + key );
        }

        return getCache( cacheName ).localGet( key );
    }

    /**
     * Description of the Method
     *
     * @param cacheName
     * @throws IOException
     */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }
        // TODO handle active disposal
        //this.getCacheManager().freeCache( cacheName, true );
    }

    /**
     * Gets the cacheManager attribute of the LateralCacheTCPListener object
     *
     * @param name
     * @return CompositeCache
     */
    protected CompositeCache getCache( String name )
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();

            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr = " + cacheMgr );
            }
        }

        return cacheMgr.getCache( name );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheListener#setCacheManager(org.apache.commons.jcs.engine.behavior.ICompositeCacheManager)
     */
    public void setCacheManager( ICompositeCacheManager cacheMgr )
    {
        this.cacheMgr = cacheMgr;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheListener#getCacheManager()
     */
    public ICompositeCacheManager getCacheManager()
    {
        return this.cacheMgr;
    }

}
