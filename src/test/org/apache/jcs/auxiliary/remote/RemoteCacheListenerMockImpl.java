package org.apache.jcs.auxiliary.remote;

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
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * For testing.
 * <p>
 * @author Aaron Smuts
 */
public class RemoteCacheListenerMockImpl
    implements IRemoteCacheListener
{
    /** Setup the listener id that this will return. */
    private long listenerId;

    /** Number of times handlePut was called. */
    public int putCount;

    /** List of ICacheElements passed to handlePut. */
    public List putItems = new LinkedList();

    /** List of Serializable objects passed to handleRemove. */
    public List removedKeys = new LinkedList();

    /** Number of times handleRemote was called. */
    public int removeCount;

    /** The type of remote listener */
    public int remoteType = IRemoteCacheAttributes.LOCAL;

    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub
    }

    /**
     * returns the listener id, which can be setup.
     */
    public long getListenerId()
        throws IOException
    {
        return listenerId;
    }

    public String getLocalHostAddress()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Return the setup remoteType.
     */
    public int getRemoteType()
        throws IOException
    {
        return remoteType;
    }

    /**
     * Allows you to setup the listener id.
     */
    public void setListenerId( long id )
        throws IOException
    {
        listenerId = id;
    }

    public void handleDispose( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * This increments the put count and adds the item to the putItem list.
     */
    public void handlePut( ICacheElement item )
        throws IOException
    {
        putCount++;
        this.putItems.add( item );
    }

    /**
     * Increments the remove count and adds the key to the removedKeys list.
     */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        removeCount++;
        removedKeys.add( key );
    }

    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
