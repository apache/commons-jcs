package org.apache.commons.jcs.auxiliary.remote;

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

import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.access.exception.ObjectExistsException;
import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheObserver;
import org.apache.commons.jcs.engine.behavior.ICacheService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Manual tester.
 */
public class RemoteCacheClientTester
    implements IRemoteCacheListener<String, String>, IRemoteCacheConstants, Remote
{
    /** the observer */
    protected ICacheObserver watch;

    /** the service */
    protected ICacheService<String, String> cache;

    /** The registry host name. */
    final String host;

    /** The registry port number. */
    final int port;

    /** call count */
    final int count;

    /** Description of the Field */
    protected static long listenerId = 0;

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
     * Constructor for the RemoteCacheClientTest object
     * @param count
     * @throws MalformedURLException
     * @throws NotBoundException
     * @throws IOException
     */
    public RemoteCacheClientTester( int count )
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
    public RemoteCacheClientTester( int count, boolean write, boolean read, boolean delete )
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
    public RemoteCacheClientTester( String host, int port, int count, boolean write, boolean read, boolean delete )
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
            UnicastRemoteObject.exportObject( this );
        }
        catch ( ExportException e )
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
        String registry = RemoteUtils.getNamingURL(host, port, service);

        p( "looking up server " + registry );

        Object obj = Naming.lookup( registry );

        p( "server found" );

        cache = (ICacheService<String, String>) obj;
        watch = (ICacheObserver) obj;

        p( "subscribing to the server" );

        watch.addCacheListener( "testCache", this );
        ICacheElement<String, String> cb = new CacheElement<String, String>( "testCache", "testKey", "testVal" );

        for ( int i = 0; i < count; i++ )
        {
            cb = new CacheElement<String, String>( "testCache", "" + i, "" + i );

            if ( delete )
            {
                p( "deleting a cache item from the server " + i );

                cache.remove( cb.getCacheName(), cb.getKey() );
            }
            if ( write )
            {
                p( "putting a cache bean to the server " + i );

                try
                {
                    cache.update( cb );
                }
                catch ( ObjectExistsException oee )
                {
                    p( oee.toString() );
                }
            }
            if ( read )
            {
                try
                {
                    Object val = cache.get( cb.getCacheName(), cb.getKey() );
                    p( "get " + cb.getKey() + " returns " + val );
                }
                catch ( CacheException onfe )
                {
                    // nothing
                }
            }
        }
    }

    /**
     * @param cb
     * @throws IOException
     */
    @Override
    public void handlePut( ICacheElement<String, String> cb )
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
    public void handleRemove( String cacheName, String key )
        throws IOException
    {
        p( "handleRemove> cacheName=" + cacheName + ", key=" + key );
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        p( "handleRemove> cacheName=" + cacheName );
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleDispose( String cacheName )
        throws IOException
    {
        p( "handleDispose> cacheName=" + cacheName );
    }

    /*
     * public void handleRelease() throws IOException { p("handleRelease>"); }
     */
    /**
     * The main program for the RemoteCacheClientTest class
     * @param args The command line arguments
     * @throws Exception
     */
    public static void main( String[] args )
        throws Exception
    {
        int count = 0;
        boolean read = false;
        boolean write = false;
        boolean delete = false;

        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[i].startsWith( "-" ) )
            {
                if ( !read )
                {
                    read = args[i].indexOf( "r" ) != -1;
                }
                if ( !write )
                {
                    write = args[i].indexOf( "w" ) != -1;
                }
                if ( !delete )
                {
                    delete = args[i].indexOf( "d" ) != -1;
                }
            }
            else
            {
                count = Integer.parseInt( args[i] );
            }
        }
        new RemoteCacheClientTester( count, write, read, delete );
    }

    /**
     * Sets the listenerId attribute of the RemoteCacheClientTest object
     * @param id The new listenerId value
     * @throws IOException
     */
    @Override
    public void setListenerId( long id )
        throws IOException
    {
        listenerId = id;
        p( "listenerId = " + id );
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
     * Helper for output, this is an user run test class
     * @param s
     */
    private static void p( String s )
    {
        System.out.println( s );
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
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub
    }
}
