/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp.behavior;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

/**
 * Used to retrieve and update the disk cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IJISPCacheService extends ICacheService
{

    /** Puts a cache item to the cache. */
    public void update( ICacheElement item, byte requesterId )
        throws IOException;


    /** Removes the given key from the specified cache. */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException;


    /** Remove all keys from the sepcified cache. */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException;

}
