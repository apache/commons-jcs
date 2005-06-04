package org.apache.jcs.engine;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used for Cache-to-Cache messaging purposes.
 *  
 */
public class CacheAdaptor
    implements ICacheListener
{
    private final static Log log = LogFactory.getLog( CacheAdaptor.class );

    private final ICache cache;

    /** Description of the Field */
    protected long listenerId = 0;

    /**
     * Sets the listenerId attribute of the CacheAdaptor object
     * 
     * @param id
     *            The new listenerId value
     */
    public void setListenerId( long id )
        throws IOException
    {
        this.listenerId = id;
        log.debug( "listenerId = " + id );
    }

    /**
     * Gets the listenerId attribute of the CacheAdaptor object
     * 
     * @return The listenerId value
     */
    public long getListenerId()
        throws IOException
    {
        return this.listenerId;
    }

    /**
     * Constructor for the CacheAdaptor object
     * 
     * @param cache
     */
    public CacheAdaptor( ICache cache )
    {
        this.cache = cache;
    }

    /** Description of the Method */
    public void handlePut( ICacheElement item )
        throws IOException
    {
        try
        {
            //cache.put(item.getKey(), item.getVal());
            //cache.update( (CacheElement)item );// .put(item.getKey(),
            // item.getVal());
            cache.update( item );
        }
        catch ( Exception e )
        {

        }
    }

    /** Description of the Method */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        cache.remove( key );
    }

    /** Description of the Method */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        cache.removeAll();
    }

    /** Description of the Method */
    public void handleDispose( String cacheName )
        throws IOException
    {
        cache.dispose();
    }
}
