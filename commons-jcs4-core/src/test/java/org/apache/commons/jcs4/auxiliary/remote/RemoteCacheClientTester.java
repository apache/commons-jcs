package org.apache.commons.jcs4.auxiliary.remote;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.jcs4.access.exception.CacheException;
import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs4.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheObserver;
import org.apache.commons.jcs4.engine.behavior.ICacheService;

/**
 * Manual tester.
 */
public class RemoteCacheClientTester
    implements IRemoteCacheListener<String, String>, IRemoteCacheConstants, Remote
{
    /** Description of the Field */
    protected static long listenerId;

    /*
     * public void handleRelease() throws IOException { p("handleRelease>"); }
     */
    /**
     * The main program for the RemoteCacheClientTest class
     * @param args The command line arguments
     * @throws Exception
     */
    public static void main( final String[] args )
        throws Exception
    {
        int count = 0;
        boolean read = false;
        boolean write = false;
        boolean delete = false;

        for (final String arg : args) {
            if ( arg.startsWith( "-" ) )
            {
                if ( !read )
                {
                    read = arg.indexOf( "r" ) != -1;
                }
                if ( !write )
                {
                    write = arg.indexOf( "w" ) != -1;
                }
                if ( !delete )
                {
                    delete = arg.indexOf( "d" ) != -1;
                }
            }
            else
            {
                count = Integer.parseInt( arg );
            }
        }
        new RemoteCacheClientTester( count, write, read, delete );
    }

    /**
     * Helper for output, this is an user run test class
     * @param s
     */
    private static void p( final String s )
    {
        System.out.println( s );
    }

    /** The observer */
    protected ICacheObserver watch;

    /** The service */
    protected ICacheService<String, String> cache;

    /** The registry host name. */
    final String host;

    /** The registry port number. */
    final int port;

    /** Call count */
    final int count;

    /**
     * Constructor for the RemoteCacheClientTest object
     * @param count
     * @throws MalformedURLException
     * @throws NotBoundException
     * @throws IOException
     */
    public RemoteCacheClientTester( final int count )
        throws MalformedURLException, NotBoundException, IOException
    {
        this( count, true, true, false );
    }

    /**
     * Constructor for the RemoteCacheClientTest object
     * @param count
     * @param write
     * @param read
     * @param delete
     * @throws MalformedURLException
     * @throws NotBoundException
     * @throws IOException
     */
    public RemoteCacheClientTester( final int count, final boolean write, final boolean read, final boolean delete )
        throws MalformedURLException, NotBoundException, IOException
    {
        this( "", Registry.REGISTRY_PORT, count, write, read, delete );
    }

    /**
     * Constructor for the RemoteCacheClientTest object
     * @param host
     * @param port
     * @param count
     * @param write
     * @param read
     * @param delete
     * @throws MalformedURLException
     * @throws NotBoundException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public RemoteCacheClientTester( final String host, final int port, final int count, final boolean write, final boolean read, final boolean delete )
        throws MalformedURLException, NotBoundException, IOException
    {
        this.count = count;
        this.host = host;
        this.port = port;
        // record export exception
        Exception ee = null;

        try
        {
            // Export this remote object to make it available to receive
            // incoming calls,
            // using an anonymous port.
            UnicastRemoteObject.exportObject(this, 0);
        }
        catch ( final ExportException e )
        {
            // use already exported object; remember exception
            ee = e;
            ee.printStackTrace();
        }
        String service = System.getProperty( REMOTE_CACHE_SERVICE_NAME );

        if ( service == null )
        {
            service = REMOTE_CACHE_SERVICE_VAL;
        }
        final String registry = RemoteUtils.getNamingURL(host, port, service);

        p( "looking up server " + registry );

        final Object obj = Naming.lookup( registry );

        p( "server found" );

        cache = (ICacheService<String, String>) obj;
        watch = (ICacheObserver) obj;

        p( "subscribing to the server" );

        watch.addCacheListener( "testCache", this );
        ICacheElement<String, String> cb = new CacheElement<>( "testCache", "testKey", "testVal" );

        for ( int i = 0; i < count; i++ )
        {
            cb = new CacheElement<>( "testCache", "" + i, "" + i );

            if ( delete )
            {
                p( "deleting a cache item from the server " + i );

                cache.remove( cb.cacheName(), cb.key() );
            }
            if ( write )
            {
                p( "putting a cache bean to the server " + i );

                try
                {
                    cache.update( cb );
                }
                catch ( final CacheException oee )
                {
                    p( oee.toString() );
                }
            }
            if ( read )
            {
                try
                {
                    final Object val = cache.get( cb.cacheName(), cb.key() );
                    p( "get " + cb.key() + " returns " + val );
                }
                catch ( final CacheException onfe )
                {
                    // nothing
                }
            }
        }
    }

    /**
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub
    }

    /**
     * Gets the listenerId attribute of the RemoteCacheClientTest object
     * @return The listenerId value
     * @throws IOException
     */
    @Override
    public long getListenerId()
        throws IOException
    {
        return listenerId;
    }

    /**
     * @return null
     * @throws IOException
     */
    @Override
    public String getLocalHostAddress()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the remoteType attribute of the RemoteCacheClientTest object
     * @return The remoteType value
     * @throws IOException
     */
    @Override
    public RemoteType getRemoteType()
        throws IOException
    {
        return RemoteType.LOCAL;
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleDispose( final String cacheName )
        throws IOException
    {
        p( "handleDispose> cacheName=" + cacheName );
    }

    /**
     * @param cb
     * @throws IOException
     */
    @Override
    public void handlePut( final ICacheElement<String, String> cb )
        throws IOException
    {
        p( "handlePut> cb=" + cb );
    }

    /**
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void handleRemove( final String cacheName, final String key )
        throws IOException
    {
        p( "handleRemove> cacheName=" + cacheName + ", key=" + key );
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleRemoveAll( final String cacheName )
        throws IOException
    {
        p( "handleRemove> cacheName=" + cacheName );
    }

    /**
     * Sets the listenerId attribute of the RemoteCacheClientTest object
     * @param id The new listenerId value
     * @throws IOException
     */
    @Override
    public void setListenerId( final long id )
        throws IOException
    {
        listenerId = id;
        p( "listenerId = " + id );
    }
}
