package org.apache.commons.jcs.auxiliary.lateral.xmlrpc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.commons.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Set;

/**
 * A lateral cache service implementation.
 *
 *      $
 */
public class LateralXMLRPCService
     implements ICacheServiceNonLocal, ILateralCacheObserver
{
    private static final Log log =
        LogFactory.getLog( LateralXMLRPCService.class );

    private ILateralCacheAttributes ilca;
    private LateralXMLRPCSender sender;

    /**
     * Constructor for the LateralXMLRPCService object
     *
     * @param lca
     * @throws IOException
     */
    public LateralXMLRPCService( ILateralCacheAttributes lca )
        throws IOException
    {
        this.ilca = lca;
        try
        {
            log.debug( "creating sender" );

            sender = new LateralXMLRPCSender( lca );

            log.debug( "created sender" );
        }
        catch ( IOException e )
        {
            //log.error( "Could not create sender", e );
            // This gets thrown over and over in recovery mode.
            // The stack trace isn't useful here.
            log.error( "Could not create sender to [" + lca.getTcpServer() + "] -- " + e.getMessage() );

            throw e;
        }
    }

    /**
     * @param item
     * @throws IOException
     */
    public void update( ICacheElement<K, V> item )
        throws IOException
    {
        update( item, LateralCacheInfo.listenerId );
    }

    /**
     * @param item
     * @param requesterId
     * @throws IOException
     */
    public void update( ICacheElement<K, V> item, long requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = led.UPDATE;
        sender.send( led );
    }

    /**
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void remove( String cacheName, K key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }

    /**
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void remove( String cacheName, K key, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVE;
        sender.send( led );
    }

    /**
     * @throws IOException
     */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }

    /**
     * Will close the connection.
     *
     * @param cache
     * @throws IOException
     */
    public void dispose( String cache )
        throws IOException
    {
        sender.dispose( cache );
    }

    /**
     * @return
     * @param key
     * @throws IOException
     */
    public Serializable get( String key )
        throws IOException
    {
        //p( "junk get" );
        //return get( cattr.cacheName, key, true );
        return null;
        // nothing needs to be done
    }

    /**
     * @return
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public ICacheElement<K, V> get( String cacheName, K key )
        throws IOException
    {
        //p( "get(cacheName,key,container)" );
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        //led.requesterId = requesterId; // later
        led.command = led.GET;
        return sender.sendAndReceive( led );
        //return null;
        // nothing needs to be done
    }

    /**
     * Gets the set of keys of objects currently in the group
     * throws UnsupportedOperationException
     */
    public Set<K> getGroupKeys(String cacheName, String group)
    {
        if (true)
        {
            throw new UnsupportedOperationException("Groups not implemented.");
        }
        return null;
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }

    /**
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVEALL;
        sender.send( led );
    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            LateralXMLRPCSender sender =
                new LateralXMLRPCSender( new LateralCacheAttributes() );

            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            BufferedReader br =
                new BufferedReader( new InputStreamReader( System.in ) );

            while ( notDone )
            {
                System.out.println( "enter mesage:" );
                message = br.readLine();
                CacheElement ce = new CacheElement( "test", "test", message );
                LateralElementDescriptor led = new LateralElementDescriptor( ce );
                sender.send( led );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
    }

    // ILateralCacheObserver methods, do nothing here since
    // the connection is not registered, the udp service is
    // is not registered.

    /**
     * @param cacheName The feature to be added to the CacheListener attribute
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /**
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /**
     * @param cacheName
     * @param obj
     * @throws IOException
     */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /**
     * @param obj
     * @throws IOException
     */
    public void removeCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

}

