package org.apache.jcs.auxiliary.remote.server;

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

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.RemoteCacheListenerMockImpl;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Since the server does not know that it is a server, it is easy to unit test. The factory does all
 * the rmi work.
 * <p>
 * @author Aaron Smuts
 */
public class RemoteCacheServerUnitTest
    extends TestCase
{

    /**
     * Add a listner. Pass the id of 0, verify that the server sets a new listener id. Do another
     * and verify that the second gets an id of 2.
     * <p>
     * @throws Exception
     */
    public void testAddListenerToCache()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        RemoteCacheListenerMockImpl mockListener1 = new RemoteCacheListenerMockImpl();
        RemoteCacheListenerMockImpl mockListener2 = new RemoteCacheListenerMockImpl();

        String cacheName = "testAddListener";

        // DO WORK
        server.addCacheListener( cacheName, mockListener1 );
        server.addCacheListener( cacheName, mockListener2 );

        // VERIFY
        assertEquals( "Wrong listener id.", 1, mockListener1.getListenerId() );
        assertEquals( "Wrong listener id.", 2, mockListener2.getListenerId() );
    }

    /**
     * Add a listner. Pass the id of 0, verify that the server sets a new listener id. Do another
     * and verify that the second gets an id of 2.
     * <p>
     * @throws Exception
     */
    public void testAddListenerToAll()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        RemoteCacheListenerMockImpl mockListener1 = new RemoteCacheListenerMockImpl();
        RemoteCacheListenerMockImpl mockListener2 = new RemoteCacheListenerMockImpl();

        // DO WORK
        // don't specify the cache name
        server.addCacheListener( mockListener1 );
        server.addCacheListener( mockListener2 );

        // VERIFY
        assertEquals( "Wrong listener id.", 1, mockListener1.getListenerId() );
        assertEquals( "Wrong listener id.", 2, mockListener2.getListenerId() );
    }

    /**
     * Add a listner. Pass the id of 0, verify that the server sets a new listener id. Do another
     * and verify that the second gets an id of 2. Call remove Listener and verify that it is
     * removed.
     * <p>
     * @throws Exception
     */
    public void testAddListenerToAllThenRemove()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        RemoteCacheListenerMockImpl mockListener1 = new RemoteCacheListenerMockImpl();
        RemoteCacheListenerMockImpl mockListener2 = new RemoteCacheListenerMockImpl();

        String cacheName = "testAddListenerToAllThenRemove";

        // DO WORK
        server.addCacheListener( cacheName, mockListener1 );
        server.addCacheListener( cacheName, mockListener2 );

        // VERIFY
        assertEquals( "Wrong number of listeners.", 2, server.getCacheListeners( cacheName ).eventQMap.size() );
        assertEquals( "Wrong listener id.", 1, mockListener1.getListenerId() );
        assertEquals( "Wrong listener id.", 2, mockListener2.getListenerId() );

        // DO WORK
        server.removeCacheListener( cacheName, mockListener1.getListenerId() );
        assertEquals( "Wrong number of listeners.", 1, server.getCacheListeners( cacheName ).eventQMap.size() );
    }

    /**
     * Add a listner. Pass the id of 0, verify that the server sets a new listener id. Do another
     * and verify that the second gets an id of 2. Call remove Listener and verify that it is
     * removed.
     * <p>
     * @throws Exception
     */
    public void testAddListenerToAllThenRemove_clusterType()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        RemoteCacheListenerMockImpl mockListener1 = new RemoteCacheListenerMockImpl();
        mockListener1.remoteType = IRemoteCacheServerAttributes.CLUSTER;
        RemoteCacheListenerMockImpl mockListener2 = new RemoteCacheListenerMockImpl();
        mockListener2.remoteType = IRemoteCacheServerAttributes.CLUSTER;

        String cacheName = "testAddListenerToAllThenRemove";

        // DO WORK
        server.addCacheListener( cacheName, mockListener1 );
        server.addCacheListener( cacheName, mockListener2 );

        // VERIFY
        assertEquals( "Wrong number of listeners.", 0, server.getCacheListeners( cacheName ).eventQMap.size() );
        assertEquals( "Wrong number of listeners.", 2, server.getClusterListeners( cacheName ).eventQMap.size() );
        assertEquals( "Wrong listener id.", 1, mockListener1.getListenerId() );
        assertEquals( "Wrong listener id.", 2, mockListener2.getListenerId() );

        // DO WORK
        server.removeCacheListener( cacheName, mockListener1.getListenerId() );
        assertEquals( "Wrong number of listeners.", 1, server.getClusterListeners( cacheName ).eventQMap.size() );
    }

    /**
     * Register a listener and then verify that it is called when we put using a different listener
     * id.
     * @throws Exception
     */
    public void testSimpleRegisterListenerAndPut()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );

        RemoteCacheListenerMockImpl mockListener = new RemoteCacheListenerMockImpl();
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        String cacheName = "testSimpleRegisterListenerAndPut";
        server.addCacheListener( cacheName, mockListener );

        // DO WORK
        List inputItems = new LinkedList();
        int numToPut = 10;

        for ( int i = 0; i < numToPut; i++ )
        {
            ICacheElement element = new CacheElement( cacheName, String.valueOf( i ), new Long( i ) );
            inputItems.add( element );
            server.update( element, 9999 );
        }

        Thread.sleep( 100 );
        Thread.yield();
        Thread.sleep( 100 );

        // VERIFY
        assertEquals( "Wrong number of items put to listener.", numToPut, mockListener.putItems.size() );
        for ( int i = 0; i < numToPut; i++ )
        {
            assertEquals( "Wrong item.", inputItems.get( i ), mockListener.putItems.get( i ) );
        }
    }

    /**
     * Register a listener and then verify that it is called when we put using a different listener
     * id. The updates should come from a cluster listener and local cluster consistency should be
     * true.
     * <p>
     * @throws Exception
     */
    public void testSimpleRegisterListenerAndPut_FromClusterWithLCC()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setLocalClusterConsistency( true );
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        // this is to get the listenr id for inserts.
        RemoteCacheListenerMockImpl clusterListener = new RemoteCacheListenerMockImpl();
        clusterListener.remoteType = IRemoteCacheAttributes.CLUSTER;

        // this should get the updates
        RemoteCacheListenerMockImpl localListener = new RemoteCacheListenerMockImpl();
        localListener.remoteType = IRemoteCacheAttributes.LOCAL;

        String cacheName = "testSimpleRegisterListenerAndPut_FromClusterWithLCC";
        server.addCacheListener( cacheName, clusterListener );
        server.addCacheListener( cacheName, localListener );

        // DO WORK
        List inputItems = new LinkedList();
        int numToPut = 10;

        for ( int i = 0; i < numToPut; i++ )
        {
            ICacheElement element = new CacheElement( cacheName, String.valueOf( i ), new Long( i ) );
            inputItems.add( element );
            // update using the cluster listener id
            server.update( element, clusterListener.getListenerId() );
        }

        Thread.sleep( 100 );
        Thread.yield();
        Thread.sleep( 100 );

        // VERIFY
        assertEquals( "Wrong number of items put to listener.", numToPut, localListener.putItems.size() );
        for ( int i = 0; i < numToPut; i++ )
        {
            assertEquals( "Wrong item.", inputItems.get( i ), localListener.putItems.get( i ) );
        }
    }

    /**
     * Register a listener and then verify that it is called when we put using a different listener
     * id.
     * @throws Exception
     */
    public void testSimpleRegisterListenerAndRemove()
        throws Exception
    {
        // SETUP
        IRemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
        rcsa.setConfigFileName( "/TestRemoteCacheServer.ccf" );

        RemoteCacheListenerMockImpl mockListener = new RemoteCacheListenerMockImpl();
        RemoteCacheServer server = new RemoteCacheServer( rcsa );

        String cacheName = "testSimpleRegisterListenerAndPut";
        server.addCacheListener( cacheName, mockListener );

        // DO WORK
        int numToPut = 10;

        for ( int i = 0; i < numToPut; i++ )
        {
            // use a junk listener id
            server.remove( cacheName, String.valueOf( i ), 9999 );
        }

        Thread.sleep( 100 );
        Thread.yield();
        Thread.sleep( 100 );

        // VERIFY
        assertEquals( "Wrong number of items removed from listener.", numToPut, mockListener.removedKeys.size() );
        for ( int i = 0; i < numToPut; i++ )
        {
            assertEquals( "Wrong key.", String.valueOf( i ), mockListener.removedKeys.get( i ) );
        }
    }

}
