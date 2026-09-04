package org.apache.commons.jcs4.engine.memory.lru;

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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs4.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs4.log.Log;

/**
 * This is a test memory manager using the stock {@link LinkedHashMap}.
 */
public class LHMLRUMemoryCache<K, V>
    extends AbstractMemoryCache<K, V>
{
    /** The Logger. */
    private static final Log log = Log.getLog( LRUMemoryCache.class );

    static
    {
        cacheImplementationName = "LHMLRU Memory Cache";
    }

    /**
     * Implements removeEldestEntry from {@link LinkedHashMap}.
     */
    protected class LHMSpooler extends LinkedHashMap<K, MemoryElementDescriptor<K, V>>
    {
        /** Don't change. */
        private static final long serialVersionUID = -1255907868906762484L;

        /**
         * Initialize to a small size--for now, 1/2 of max 3rd variable "true" indicates that it
         * should be access and not time governed. This could be configurable.
         */
        public LHMSpooler()
        {
            super( (int) ( getCacheAttributes().MaxObjects() * .5 ), .75F, true );
        }

        /**
         * Remove eldest. Automatically called by LinkedHashMap.
         *
         * @param eldest
         * @return true if removed
         */
        @Override
        protected boolean removeEldestEntry( final Map.Entry<K, MemoryElementDescriptor<K, V>> eldest )
        {
            final ICacheElement<K, V> element = eldest.getValue().getCacheElement();

            if ( size() <= getCacheAttributes().MaxObjects() )
            {
                return false;
            }
            log.debug( "LHMLRU max size: {0}. Spooling element, key: {1}",
                    () -> getCacheAttributes().MaxObjects(), element::key);

            waterfall(element);

            log.debug("LHMLRU size: {0}", getSize());
            return true;
        }
    }

    /**
     * Returns a LHMSpooler
     *
     * @return new LHMSpooler()
     */
    @Override
    protected Map<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new LHMSpooler();
    }

    /**
     * Wrap the cache element into an appropriate memory element descriptor
     *
     * @param ce The cache element
     * @return The memory element descriptor
     */
    @Override
    protected MemoryElementDescriptor<K, V> wrap(ICacheElement<K, V> ce)
    {
        return new MemoryElementDescriptor<>(ce);
    }

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void lockedGetElement(final MemoryElementDescriptor<K, V> me)
    {
        // empty
    }

    /**
     * Update control structures after update
     * (guarded by the lock)
     *
     * @param newNode The memory element descriptor of the current cache element
     * @param oldNode The memory element descriptor of the previous cache element
     */
    @Override
    protected void lockedUpdateElement(MemoryElementDescriptor<K, V> newNode,
            MemoryElementDescriptor<K, V> oldNode)
    {
        // empty
    }

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    @Override
    protected void lockedRemoveAll()
    {
        // empty
    }

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void lockedRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        // empty
    }

    /**
     * This can't be implemented.
     *
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    @Override
    protected int lockedFreeElements(final int numberToFree) throws IOException
    {
        return 0;
    }
}
