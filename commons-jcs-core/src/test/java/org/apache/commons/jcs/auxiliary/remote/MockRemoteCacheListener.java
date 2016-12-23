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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

/**
 * For testing.
 * <p>
 * @author Aaron Smuts
 */
public class MockRemoteCacheListener<K, V>
    implements IRemoteCacheListener<K, V>
{
    /** Setup the listener id that this will return. */
    private long listenerId;

    /** Setup the listener ip that this will return. */
    public String localAddress;

    /** Number of times handlePut was called. */
    public int putCount;

    /** List of ICacheElements passed to handlePut. */
    public List<ICacheElement<K, V>> putItems = new LinkedList<ICacheElement<K,V>>();

    /** List of Serializable objects passed to handleRemove. */
    public List<K> removedKeys = new LinkedList<K>();

    /** Number of times handleRemote was called. */
    public int removeCount;

    /** The type of remote listener */
    public RemoteType remoteType = RemoteType.LOCAL;

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
     * returns the listener id, which can be setup.
     * @return listenerId
     * @throws IOException
     */
    @Override
    public long getListenerId()
        throws IOException
    {
        return listenerId;
    }

    /**
     * @return localAddress
     * @throws IOException
     */
    @Override
    public String getLocalHostAddress()
        throws IOException
    {
        return localAddress;
    }

    /**
     * Return the setup remoteType.
     * @return remoteType
     * @throws IOException
     */
    @Override
    public RemoteType getRemoteType()
        throws IOException
    {
        return remoteType;
    }

    /**
     * Allows you to setup the listener id.
     * <p>
     * @param id
     * @throws IOException
     */
    @Override
    public void setListenerId( long id )
        throws IOException
    {
        listenerId = id;
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleDispose( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * This increments the put count and adds the item to the putItem list.
     * <p>
     * @param item
     * @throws IOException
     */
    @Override
    public void handlePut( ICacheElement<K, V> item )
        throws IOException
    {
        putCount++;
        this.putItems.add( item );
    }

    /**
     * Increments the remove count and adds the key to the removedKeys list.
     * <p>
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void handleRemove( String cacheName, K key )
        throws IOException
    {
        removeCount++;
        removedKeys.add( key );
    }

    /**
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub
    }
}
