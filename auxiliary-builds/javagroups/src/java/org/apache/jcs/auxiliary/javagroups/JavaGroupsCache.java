package org.apache.jcs.auxiliary.javagroups;

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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.control.CompositeCache;
import org.javagroups.Channel;
import org.javagroups.Message;
import org.javagroups.blocks.RequestHandler;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

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
 * @author <a href="james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class JavaGroupsCache implements AuxiliaryCache, RequestHandler
{
    private final static Log log = LogFactory.getLog( JavaGroupsCache.class );

    private String cacheName;
    private int status;

    private CompositeCache cache;

    private Channel channel;

    private Listener listener;

    public JavaGroupsCache( CompositeCache cache, Channel channel )
        throws Exception
    {
        this.cache = cache;

        this.cacheName = cache.getCacheName();
        this.channel = channel;

        // Connect channel to the 'group' for our region name

        channel.connect( cacheName );

        // The adapter listens to the channel and fires MessageListener events
        // on this object.

        listener = new Listener( channel, this, cacheName );

        listener.start();

        // If all the above succeed, the cache is now alive.

        this.status = CacheConstants.STATUS_ALIVE;

        log.info( "Initialized for cache: " + cacheName );
    }

    public void send( ICacheElement element, int command )
    {
        Request request = new Request( element, command );

        try
        {
            channel.send( new Message( null, null, request ) );
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
    public void update( ICacheElement ce ) throws IOException
    {
        send( ce, Request.UPDATE );
    }

    /**
     * Not implemented, we expect that if an element is available to one of our
     * peers, it has already been shared by a previous update.
     *
     * NOTE: This could be easily implemented using a MessageDispatcher and
     * ResponseCollector, which would be usefull when the caches get out of
     * sync (for example, if one member is rebooted).
     *
     * @param key Ignored
     * @return Always returns null
     * @throws IOException Never thrown by this implementation
     */
    public ICacheElement get( Serializable key ) throws IOException
    {
        return null;
    }

    /**
     * Sends a request to all peers to remove the element having the provided
     * key.
     *
     * @param key Key of element to be removed
     * @throws IOException Never thrown by this implementation
     */
    public boolean remove( Serializable key ) throws IOException
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
        listener.terminate();

        status = CacheConstants.STATUS_DISPOSED;
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
    public Set getGroupKeys( String group )
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

    // ---------------------------------------------------------- inner classes

    /**
     * Object for messages, wraps the command type (update, remove, or remove
     * all) and original cache element to distribute.
     */
    static class Request implements Serializable
    {
        public final static int UPDATE = 1;
        public final static int REMOVE = 2;
        public final static int REMOVE_ALL = 3;

        private ICacheElement cacheElement;
        private int command;

        public Request( ICacheElement cacheElement, int command )
        {
            this.cacheElement = cacheElement;
            this.command = command;
        }

        public ICacheElement getCacheElement()
        {
            return cacheElement;
        }

        public int getCommand()
        {
            return command;
        }
    }

    /**
     * Listens for messages on the provided channel and dispatches them to the
     * CompositeCache that this auxiliary was created with. Also, when
     * terminated is resposible for disconnecting and closing the Channel.
     */
    static class Listener extends Thread
    {
        boolean running = true;

        RequestHandler handler;

        Channel channel;
        Object object;

        public Listener( Channel channel,
                         RequestHandler handler,
                         String cacheName )
        {
            super( "JavaGroupsCache.Listener: " + cacheName );

            this.channel = channel;
            this.handler = handler;
        }

        public void run()
        {
            while ( running )
            {
                // Get next object from channel, blocking indefinately

                try
                {
                    object = channel.receive( 0 );
                }
                catch ( Exception e )
                {
                    // If we are still supposed to be running, pause and
                    // continue.

                    if ( running )
                    {
                        try
                        {
                            Thread.sleep( 1000 );
                        }
                        catch ( Exception ignored )
                        {
                            // Ignored
                        }
                    }
                }


                // Ignore anything that is not a message

                if ( object instanceof Message )
                {
                    handler.handle( ( Message ) object );
                }
            }

            // When terminated, clean up the channel. Termination should be
            // initiated by the dispose() method of the enclosing
            // JavaGroupsCache

            channel.disconnect();
            channel.close();
        }

        public void terminate()
        {
            running = false;

            this.interrupt();

            try
            {
                this.join( 1000 );
            }
            catch ( InterruptedException e )
            {
                // Ignored, not much we can do anymore.
            }
        }
    }
}
