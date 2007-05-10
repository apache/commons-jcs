package org.apache.jcs.engine;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * Used for Cache-to-Cache messaging purposes. These are used in the balking
 * facades in the lateral and remote caches.
 */
public class CacheAdaptor
    implements ICacheListener
{
    private final static Log log = LogFactory.getLog( CacheAdaptor.class );

    private final ICache cache;

    /** The unique id of this listner. */
    protected long listenerId = 0;

    /**
     * Sets the listenerId attribute of the CacheAdaptor object
     * <p>
     * @param id
     *            The new listenerId value
     * @throws IOException
     */
    public void setListenerId( long id )
        throws IOException
    {
        this.listenerId = id;
        log.debug( "listenerId = " + id );
    }

    /**
     * Gets the listenerId attribute of the CacheAdaptor object
     * <p>
     * @return The listenerId value
     * @throws IOException
     */
    public long getListenerId()
        throws IOException
    {
        return this.listenerId;
    }

    /**
     * Constructor for the CacheAdaptor object
     * @param cache
     */
    public CacheAdaptor( ICache cache )
    {
        this.cache = cache;
    }

    /**
     * Puts an item into the cache.
     * <p>
     * @param item
     * @throws IOException
     */
    public void handlePut( ICacheElement item )
        throws IOException
    {
        try
        {
            cache.update( item );
        }
        catch ( Exception e )
        {
            // swallow
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemove(java.lang.String,
     *      java.io.Serializable)
     */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        cache.remove( key );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemoveAll(java.lang.String)
     */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        cache.removeAll();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleDispose(java.lang.String)
     */
    public void handleDispose( String cacheName )
        throws IOException
    {
        cache.dispose();
    }
}
