/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.remote.group;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.auxiliary.remote.RemoteCacheListener;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;

import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupCache;
import org.apache.jcs.engine.control.group.GroupCache;
import org.apache.jcs.engine.control.group.GroupCache;
import org.apache.jcs.engine.control.CacheHub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// remove

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteGroupCacheListener extends RemoteCacheListener implements IRemoteCacheListener, IRemoteCacheConstants, Serializable
{
    private final static Log log =
        LogFactory.getLog( RemoteGroupCacheListener.class );

    /**
     * Constructor for the RemoteGroupCacheListener object
     *
     * @param irca
     */
    protected RemoteGroupCacheListener( IRemoteCacheAttributes irca )
    {
        super( irca );
        log.debug( "creating RemoteGroupCacheListener" );
    }


    // should store ina  hashmap based on store address
    /**
     * Gets the instance attribute of the RemoteGroupCacheListener class
     *
     * @return The instance value
     */
    public static IRemoteCacheListener getInstance( IRemoteCacheAttributes irca )
    {
        //throws IOException, NotBoundException
        if ( instance == null )
        {
            synchronized ( RemoteGroupCacheListener.class )
            {
                if ( instance == null )
                {
                    instance = new RemoteGroupCacheListener( irca );
                }
            }
        }
        //instance.incrementClients();
        return instance;
    }


    /**
     * The remote cache can be configured to remove on remote put or to store
     * the element. Removal saves space on the local cache. If the local needs
     * the element it can go it. The local copy will not be stale, and it need
     * not waste space and time. The group cache is in a more complicated
     * situation. If the group cache removes upon remote put, the list of
     * elements on the local cache will be short. The best thing to do is remove
     * the local item so it isn't stale and at the same time update the list. We
     * don't want to invalidate the list since it is not easy to pass a HashSet
     * around while the program is modifying it. The locking would be too slow
     * and it would be a great burden on all caches involved. I had to expose
     * another method og teh GroupCache here. It is not in the the interface.
     * TODO: add the updateAttrnameSer to interface, maybe.
     */
    public void handlePut( ICacheElement cb )
        throws IOException
    {

        try
        {

            if ( cb.getKey() instanceof GroupAttrName )
            {

                if ( irca.getRemoveUponRemotePut() )
                {
                    log.debug( "PUTTING ELEMENT FROM REMOTE, (  invalidating ) " );

                    // remove the item
                    handleRemove( cb.getCacheName(), cb.getKey() );

                    // add the key to the attrNameSet

                    log.debug( "Adding to attrNameSet " );

                    getCacheManager();
                    GroupCache cache = ( GroupCache ) cacheMgr.getCache( cb.getCacheName() );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "cache = " + cache );
                    }
                    cache.updateGroupAttrNameSet( ( GroupAttrName ) cb.getKey(), ICache.REMOTE_INVOKATION, false );

                    log.debug( "Adding to attrNameSet " );
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
                    ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( irca.getCacheName() );
                    cache.update( cb, ICache.REMOTE_INVOKATION );

                }

            }
            else
            {
                super.handlePut( cb );
            }

        }
        catch ( Exception e )
        {
            log.error( e );
            // should probably throw ioe
            if ( e instanceof IOException )
            {
                throw ( IOException ) e;
            }
        }

        return;
    }


    /*
     * if ( debugcmd ) {
     * p( "PUTTING ELEMENT FROM REMOTE" );
     * }
     * / could put this in the group cache.
     * if (cb.getKey() instanceof GroupAttrName) {
     * try {
     * if ( log.isDebugEnabled() ) {
     * p( "removing gi for ga method" );
     * }
     * GroupAttrName gan = (GroupAttrName)cb.getKey();
     * GroupId groupId = new GroupId(cb.getCacheName(), gan.groupId );
     * /System.out.println( "removing gi for ga method" );
     * / avoids overhead of removegan method where the attributeset is retrieved first
     * GroupCache cache = (GroupCache)cacheMgr.getCache(irca.getCacheName());
     * /cache.remove(new Integer(gan.hashCode()), cache.REMOTE_INVOKATION);
     * cache.remove(gan, cache.REMOTE_INVOKATION);
     * / other auxiliaries may update and not remove
     * cache.remove(groupId, cache.REMOTE_INVOKATION);
     * / too slow? can avoid lock  and go directly to super.remove
     * /super.handleRemove(cb.getCacheName(), cb.getKey());
     * /GroupCache cache = (GroupCache)cacheMgr.getCache(irca.getCacheName());
     * /cache.remove(key, cache.REMOTE_INVOKATION, true);
     * / since the removeGAN call removes form the attrlist
     * / it must be called first or we loose 1 by saving 1 short
     * / since we are treating a put as a remove we must invlidate the list
     * / since it actually has 1 more element, otherwise the lsit would be 1 short
     * / probably better just to add it
     * /super.handleRemove(cb.getCacheName(), groupId);
     * /super.handleRemove( cb );
     * } catch ( Exception e ) {}
     * return;
     * } else {
     * super.handlePut(cb);
     * }
     * }
     */
    // override for new funcitonality
    // TODO: lazy init is too slow, find a better way
    /**
     * Gets the cacheManager attribute of the RemoteGroupCacheListener object
     */
    protected void getCacheManager()
    {
        try
        {
            if ( cacheMgr == null )
            {
                cacheMgr = CacheHub.getInstance();
                if ( log.isDebugEnabled() )
                {
                    log.debug( " groupcache cacheMgr = " + cacheMgr );
                }
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "already got groupcache cacheMgr = " + cacheMgr );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

}
