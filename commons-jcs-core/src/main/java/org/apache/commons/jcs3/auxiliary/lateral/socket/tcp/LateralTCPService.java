package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.lateral.LateralCommand;
import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * A lateral cache service implementation. Does not implement getGroupKey
 * TODO: Remove generics
 */
public class LateralTCPService<K, V>
    implements ICacheServiceNonLocal<K, V>
{
    /** The logger. */
    private static final Log log = LogManager.getLog( LateralTCPService.class );

    /** special configuration */
    private final boolean allowPut;
    private final boolean allowGet;
    private final boolean issueRemoveOnPut;

    /** Sends to another lateral. */
    private final LateralTCPSender sender;

    /** use the vmid by default */
    private long listenerId = CacheInfo.listenerId;

    /**
     * Constructor for the LateralTCPService object
     * <p>
     * @param lca ITCPLateralCacheAttributes the configuration object
     * @throws IOException
     *
     * @deprecated Specify serializer
     */
    @Deprecated
    public LateralTCPService( final ITCPLateralCacheAttributes lca )
        throws IOException
    {
        this(lca, new StandardSerializer());
    }

    /**
     * Constructor for the LateralTCPService object
     * <p>
     * @param lca ITCPLateralCacheAttributes the configuration object
     * @param serializer the serializer to use when sending
     * @throws IOException
     * @since 3.1
     */
    public LateralTCPService( final ITCPLateralCacheAttributes lca, final IElementSerializer serializer )
        throws IOException
    {
        this.allowGet = lca.isAllowGet();
        this.allowPut = lca.isAllowPut();
        this.issueRemoveOnPut = lca.isIssueRemoveOnPut();

        try
        {
            sender = new LateralTCPSender( lca, serializer );

            log.debug( "Created sender to [{0}]", lca::getTcpServer);
        }
        catch ( final IOException e )
        {
            // log.error( "Could not create sender", e );
            // This gets thrown over and over in recovery mode.
            // The stack trace isn't useful here.
            log.error( "Could not create sender to [{0}] -- {1}", lca::getTcpServer, e::getMessage);
            throw e;
        }
    }

    /**
     * @param item
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> item )
        throws IOException
    {
        update( item, getListenerId() );
    }

    /**
     * If put is allowed, we will issue a put. If issue put on remove is configured, we will issue a
     * remove. Either way, we create a lateral element descriptor, which is essentially a JCS TCP
     * packet. It describes what operation the receiver should take when it gets the packet.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal#update(org.apache.commons.jcs3.engine.behavior.ICacheElement,
     *      long)
     */
    @Override
    public void update( final ICacheElement<K, V> item, final long requesterId )
        throws IOException
    {
        // if we don't allow put, see if we should remove on put
        if ( !this.allowPut &&
            // if we can't remove on put, and we can't put then return
            !this.issueRemoveOnPut )
        {
            return;
        }

        // if we shouldn't remove on put, then put
        if ( !this.issueRemoveOnPut )
        {
            final LateralElementDescriptor<K, V> led =
                    new LateralElementDescriptor<>(item, LateralCommand.UPDATE, requesterId);
            sender.send( led );
        }
        // else issue a remove with the hashcode for remove check on
        // on the other end, this will be a server config option
        else
        {
            log.debug( "Issuing a remove for a put" );

            // set the value to null so we don't send the item
            final CacheElement<K, V> ce = new CacheElement<>( item.getCacheName(), item.getKey(), null );
            final LateralElementDescriptor<K, V> led =
                    new LateralElementDescriptor<>(ce, LateralCommand.REMOVE, requesterId);
            led.valHashCode = item.getVal().hashCode();
            sender.send( led );
        }
    }

    /**
     * Uses the default listener id and calls the next remove method.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheService#remove(String, Object)
     */
    @Override
    public void remove( final String cacheName, final K key )
        throws IOException
    {
        remove( cacheName, key, getListenerId() );
    }

    /**
     * Wraps the key in a LateralElementDescriptor.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal#remove(String, Object, long)
     */
    @Override
    public void remove( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        final CacheElement<K, V> ce = new CacheElement<>( cacheName, key, null );
        final LateralElementDescriptor<K, V> led =
                new LateralElementDescriptor<>(ce, LateralCommand.REMOVE, requesterId);
        sender.send( led );
    }

    /**
     * Does nothing.
     * <p>
     * @throws IOException
     */
    @Override
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }

    /**
     * Will close the connection.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void dispose( final String cacheName )
        throws IOException
    {
        sender.dispose();
    }

    /**
     * @param cacheName
     * @param key
     * @return ICacheElement&lt;K, V&gt; if found.
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key )
        throws IOException
    {
        return get( cacheName, key, getListenerId() );
    }

    /**
     * If get is allowed, we will issues a get request.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement&lt;K, V&gt; if found.
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        // if get is not allowed return
        if ( this.allowGet )
        {
            final CacheElement<K, V> ce = new CacheElement<>( cacheName, key, null );
            final LateralElementDescriptor<K, V> led =
                    new LateralElementDescriptor<>(ce, LateralCommand.GET);
            // led.requesterId = requesterId; // later
            @SuppressWarnings("unchecked") // Need to cast from Object
            final
            ICacheElement<K, V> response = (ICacheElement<K, V>)sender.sendAndReceive( led );
            return response;
        }
        // nothing needs to be done
        return null;
    }

    /**
     * If allow get is true, we will issue a getmatching query.
     * <p>
     * @param cacheName
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, getListenerId() );
    }

    /**
     * If allow get is true, we will issue a getmatching query.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId - our identity
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    @Override
    @SuppressWarnings("unchecked") // Need to cast from Object
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        // if get is not allowed return
        if ( !this.allowGet ) {
            // nothing needs to be done
            return null;
        }
        final CacheElement<String, String> ce = new CacheElement<>( cacheName, pattern, null );
        final LateralElementDescriptor<String, String> led =
                new LateralElementDescriptor<>(ce, LateralCommand.GET_MATCHING);
        // led.requesterId = requesterId; // later

        final Object response = sender.sendAndReceive( led );
        if ( response != null )
        {
            return (Map<K, ICacheElement<K, V>>) response;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param cacheName
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys )
        throws IOException
    {
        return getMultiple( cacheName, keys, getListenerId() );
    }

    /**
     * This issues a separate get for each item.
     * <p>
     * TODO We should change this. It should issue one request.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
        throws IOException
    {
        final Map<K, ICacheElement<K, V>> elements = new HashMap<>();

        if ( keys != null && !keys.isEmpty() )
        {
            for (final K key : keys)
            {
                final ICacheElement<K, V> element = get( cacheName, key, requesterId );

                if ( element != null )
                {
                    elements.put( key, element );
                }
            }
        }
        return elements;
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @param cacheName the name of the cache region
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    @SuppressWarnings("unchecked") // Need cast from Object
    public Set<K> getKeySet(final String cacheName) throws IOException
    {
        final CacheElement<String, String> ce = new CacheElement<>(cacheName, null, null);
        final LateralElementDescriptor<String, String> led =
                new LateralElementDescriptor<>(ce, LateralCommand.GET_KEYSET);
        // led.requesterId = requesterId; // later
        final Object response = sender.sendAndReceive(led);
        if (response != null)
        {
            return (Set<K>) response;
        }

        return null;
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName )
        throws IOException
    {
        removeAll( cacheName, getListenerId() );
    }

    /**
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName, final long requesterId )
        throws IOException
    {
        final CacheElement<String, String> ce = new CacheElement<>( cacheName, "ALL", null );
        final LateralElementDescriptor<String, String> led =
                new LateralElementDescriptor<>(ce, LateralCommand.REMOVEALL, requesterId);
        sender.send( led );
    }

    /**
     * Test
     * @param args
     *
     * @deprecated Use unit tests
     */
    @Deprecated
    public static void main( final String args[] )
    {
        try
        {
            final LateralTCPSender sender = new LateralTCPSender( new TCPLateralCacheAttributes() );

            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            final BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            while ( notDone )
            {
                System.out.println( "enter message:" );
                message = br.readLine();

                if (message == null)
                {
                    notDone = false;
                    continue;
                }

                final CacheElement<String, String> ce = new CacheElement<>( "test", "test", message );
                final LateralElementDescriptor<String, String> led = new LateralElementDescriptor<>( ce );
                sender.send( led );
            }
        }
        catch ( final IOException e )
        {
            System.out.println( e.toString() );
        }
    }

    /**
     * @param listernId The listernId to set.
     */
    protected void setListenerId( final long listernId )
    {
        this.listenerId = listernId;
    }

    /**
     * @return Returns the listernId.
     */
    protected long getListenerId()
    {
        return listenerId;
    }
}
