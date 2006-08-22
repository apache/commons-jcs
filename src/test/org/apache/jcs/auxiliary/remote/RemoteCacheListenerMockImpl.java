package org.apache.jcs.auxiliary.remote;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache
 * License, Version 2.0 (the "License") you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * For testing.
 * <p>
 * @author admin
 */
public class RemoteCacheListenerMockImpl
    implements IRemoteCacheListener
{
    /** Setup the listener id that this will return. */
    private long listenerId;

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

    public int getRemoteType()
        throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
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

    public void handlePut( ICacheElement item )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
