package org.apache.commons.jcs.auxiliary.javagroups;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.engine.CacheConstants;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheType;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javagroups.Address;
import org.javagroups.Channel;
import org.javagroups.MembershipListener;
import org.javagroups.Message;
import org.javagroups.View;
import org.javagroups.blocks.GroupRequest;
import org.javagroups.blocks.MessageDispatcher;
import org.javagroups.blocks.RequestHandler;
import org.javagroups.util.RspList;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.Vector;

/**
 * Auxiliary cache using javagroups. Expects to be created with a Channel,
 * the {@link JavaGroupsCacheFactory} is responsible for creating that channel.
 * To do so it uses configuration properties specified by an instance of
 * {@link JavaGroupsCacheAttributes}.
 * <p>
 * At creation time the provided channel is connected to a group having the
 * same name as the cache / region name this auxiliary is associated with.
 * update / remove / removeAll operations are broadcast to all members of the
 * group. A listener thread processes requests from other members of the group,
 * and dispatches to appropriate methods on the associated CompositeCache. </p>
 * <p>
 * Calls to get are currently ignored.
 * <p>
 * Messages are sent to peers asynchronously. Synchronous messaging could be
 * added using MessageDispatcher or RpcDispatcher. Combined with a get
 * implementation this could provide much higher cache consistency (but with
 * a substantial speed penalty).
 *
 * @version $Id$
 */
public class JavaGroupsCache
    implements AuxiliaryCache, RequestHandler, MembershipListener
{
    private final Log log = LogFactory.getLog( JavaGroupsCache.class );

    private String cacheName;
    private int status;

    private boolean getFromPeers;

    private CompositeCache cache;

    private Channel channel;

    private MessageDispatcher dispatcher;

    public JavaGroupsCache( CompositeCache cache,
                            Channel channel,
                            boolean getFromPeers )
        throws Exception
    {
        this.cache = cache;

        this.cacheName = cache.getCacheName();
        this.channel = channel;

        this.getFromPeers = getFromPeers;

        // The adapter listens to the channel and fires MessageListener events
        // on this object.

        dispatcher = new MessageDispatcher( channel, null, this, this );

        // Connect channel to the 'group' for our region name

        channel.setOpt( Channel.LOCAL, Boolean.FALSE );

        channel.connect( cacheName );

        // If all the above succeed, the cache is now alive.

        this.status = CacheStatus.ALIVE;

        log.info( "Initialized for cache: " + cacheName );
    }

    public void send( ICacheElement<K, V> element, int command )
    {
        Request request = new Request( element, command );

        try
        {
            dispatcher.castMessage( null,
                                    new Message( null, null, request ),
                                    GroupRequest.GET_NONE,
                                    0 );

        }
        catch ( Exception e )
        {
            log.error( "Failed to send JavaGroups message", e );
        }
    }

    // ----------------------------------------------- interface AuxiliaryCache

    /**
     * Sends the provided element to all peers (connected to the same channel
     * and region name).
     *
     * @param ce CacheElement to replicate
     * @throws IOException Never thrown by this implementation
     */
    public void update( ICacheElement<K, V> ce ) throws IOException
    {
        send( ce, Request.UPDATE );
    }

    /**
     * If 'getFromPeers' is true, this will attempt to get the requested
     * element from ant other members of the group.
     *
     * @param key
     * @return
     * @throws IOException Never thrown by this implementation
     */
    public ICacheElement<K, V> get( K key ) throws IOException
    {
        if ( getFromPeers )
        {
            CacheElement element = new CacheElement( cacheName, key, null );

            Request request = new Request( element, Request.GET );

            // Cast message and wait for all responses.

            // FIXME: we can stop waiting after the first not null response,
            //        that is more difficult to implement however.

            RspList responses =
                dispatcher.castMessage( null,
                                        new Message( null, null, request ),
                                        GroupRequest.GET_ALL,
                                        0 );

            // Get results only gives the responses which were not null

            Vector results = responses.getResults();

            // If there were any non null results, return the first

            if ( results.size() > 0 )
            {
                return ( ICacheElement<K, V> ) results.get( 0 );
            }
        }

        return null;
    }

    /**
     * Sends a request to all peers to remove the element having the provided
     * key.
     *
     * @param key Key of element to be removed
     * @throws IOException Never thrown by this implementation
     */
    public boolean remove( K key ) throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );

        send( ce, Request.REMOVE );

        return false;
    }

    /**
     * Sends a request to remove ALL elements from the peers
     *
     * @throws IOException Never thrown by this implementation
     */
    public void removeAll() throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, null, null );

        send( ce, Request.REMOVE_ALL );
    }

    /**
     * Dispose this cache, terminates the listener thread and disconnects the
     * channel from the group.
     *
     * @throws IOException
     */
    public void dispose() throws IOException
    {
        // This will join the scheduler thread and ensure everything terminates

        dispatcher.stop();

        // Now we can disconnect from the group and close the channel

        channel.disconnect();
        channel.close();

        status = CacheStatus.DISPOSED;

        log.info( "Disposed for cache: " + cacheName );
    }

    /**
     * Since this is a lateral, size is not defined.
     *
     * @return Always returns 0
     */
    public int getSize()
    {
        return 0;
    }

    /**
     * Returns the status of this auxiliary.
     *
     * @return One of the status constants from {@link CacheConstants}
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Accessor for cacheName property
     *
     * @return Name of cache / region this auxiliary is associated with.
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Not implemented (I believe since get is not supported, this should also
     * not be).
     *
     * @param group Ignored
     * @return Always reurns null
     */
    public Set<K> getGroupKeys( String group )
    {
        return null;
    }

    // --------------------------------------------------- interface ICacheType

    /**
     * Get the cache type (always Lateral).
     *
     * @return Always returns ICacheType.LATERAL_CACHE
     */
    public int getCacheType()
    {
        return ICacheType.LATERAL_CACHE;
    }

    // ----------------------------------------------- interface RequestHandler

    /**
     * Handles a message from a peer. The message should contain a Request,
     * and depending on the command this will call localUpdate, localRemove,
     * or localRemoveAll on the associated CompositeCache.
     *
     * @param msg The JavaGroups Message
     * @return Always returns null
     */
    public Object handle( Message msg )
    {
        try
        {
            Request request = ( Request ) msg.getObject();

            // Switch based on the command and invoke the
            // appropriate method on the associate composite cache

            switch ( request.getCommand() )
            {
                case Request.GET:

                    return cache.localGet( request.getCacheElement().getKey() );
                    // break;

                case Request.UPDATE:

                    cache.localUpdate( request.getCacheElement() );
                    break;

                case Request.REMOVE:

                    cache.localRemove( request.getCacheElement().getKey() );
                    break;

                case Request.REMOVE_ALL:

                    cache.localRemoveAll();
                    break;

                default:

                    log.error( "Recieved unknown command" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to process received JavaGroups message", e );
        }

        return null;
    }

    // ------------------------------------------- interface MembershipListener

    public void viewAccepted( View view )
    {
        log.info( "View Changed: " + String.valueOf( view ) );
    }

    public void suspect( Address suspectedAddress ) { }

    public void block() { }

    // ---------------------------------------------------------- inner classes

    /**
     * Object for messages, wraps the command type (update, remove, or remove
     * all) and original cache element to distribute.
     */
    static class Request implements Serializable
    {
        public static final int UPDATE = 1;
        public static final int REMOVE = 2;
        public static final int REMOVE_ALL = 3;
        public static final int GET = 5;

        private ICacheElement<K, V> cacheElement;
        private int command;

        public Request( ICacheElement<K, V> cacheElement, int command )
        {
            this.cacheElement = cacheElement;
            this.command = command;
        }

        public ICacheElement<K, V> getCacheElement()
        {
            return cacheElement;
        }

        public int getCommand()
        {
            return command;
        }
    }
}
