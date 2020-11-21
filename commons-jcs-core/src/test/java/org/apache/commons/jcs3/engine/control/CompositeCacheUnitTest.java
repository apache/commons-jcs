package org.apache.commons.jcs3.engine.control;

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

import junit.framework.TestCase;

import org.apache.commons.jcs3.auxiliary.MockAuxiliaryCache;
import org.apache.commons.jcs3.engine.memory.MockMemoryCache;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CompositeCacheAttributes;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheType.CacheType;
import java.io.IOException;
import java.util.Map;

/**
 * Tests that directly engage the composite cache.
 * <p>
 * @author Aaron Smuts
 */
public class CompositeCacheUnitTest
    extends TestCase
{
    /**
     * Verify that the freeMemoryElements method on the memory cache is called on shutdown if there
     * is a disk cache.
     * <p>
     * @throws IOException
     */
    public void testShutdownMemoryFlush()
        throws IOException
    {
        // SETUP
        final String cacheName = "testCacheName";
        final String mockMemoryCacheClassName = "org.apache.commons.jcs3.engine.memory.MockMemoryCache";
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( mockMemoryCacheClassName );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, Integer> cache = new CompositeCache<>( cattr, attr );

        final MockAuxiliaryCache<String, Integer> diskMock = new MockAuxiliaryCache<>();
        diskMock.cacheType = CacheType.DISK_CACHE;
        @SuppressWarnings("unchecked")
        final
        AuxiliaryCache<String, Integer>[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        final int numToInsert = 10;
        for ( int i = 0; i < numToInsert; i++ )
        {
            final ICacheElement<String, Integer> element = new CacheElement<>( cacheName, String.valueOf( i ), Integer.valueOf( i ) );
            cache.update( element, false );
        }

        cache.dispose();

        // VERIFY
        final MockMemoryCache<String, Integer> memoryCache = (MockMemoryCache<String, Integer>) cache.getMemoryCache();
        assertEquals( "Wrong number freed.", numToInsert, memoryCache.lastNumberOfFreedElements );
    }

    /**
     * Verify that the freeMemoryElements method on the memory cache is NOT called on shutdown if
     * there is NOT a disk cache.
     * <p>
     * @throws IOException
     */
    public void testShutdownMemoryFlush_noDisk()
        throws IOException
    {
        // SETUP
        final String cacheName = "testCacheName";
        final String mockMemoryCacheClassName = "org.apache.commons.jcs3.engine.memory.MockMemoryCache";
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( mockMemoryCacheClassName );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, Integer> cache = new CompositeCache<>( cattr, attr );

        final MockAuxiliaryCache<String, Integer> diskMock = new MockAuxiliaryCache<>();
        diskMock.cacheType = CacheType.REMOTE_CACHE;
        @SuppressWarnings("unchecked")
        final
        AuxiliaryCache<String, Integer>[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        final int numToInsert = 10;
        for ( int i = 0; i < numToInsert; i++ )
        {
            final ICacheElement<String, Integer> element = new CacheElement<>( cacheName, String.valueOf( i ), Integer.valueOf( i ) );
            cache.update( element, false );
        }

        cache.dispose();

        // VERIFY
        final MockMemoryCache<String, Integer> memoryCache = (MockMemoryCache<String, Integer>) cache.getMemoryCache();
        assertEquals( "Wrong number freed.", 0, memoryCache.lastNumberOfFreedElements );
    }

    /**
     * Verify we can get some matching elements..
     * <p>
     * @throws IOException
     */
    public void testGetMatching_Normal()
        throws IOException
    {
        // SETUP
        final int maxMemorySize = 1000;
        final String keyprefix1 = "MyPrefix1";
        final String keyprefix2 = "MyPrefix2";
        final String cacheName = "testGetMatching_Normal";
        final String memoryCacheClassName = "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache";
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( memoryCacheClassName );
        cattr.setMaxObjects( maxMemorySize );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, Integer> cache = new CompositeCache<>( cattr, attr );

        final MockAuxiliaryCache<String, Integer> diskMock = new MockAuxiliaryCache<>();
        diskMock.cacheType = CacheType.DISK_CACHE;
        @SuppressWarnings("unchecked")
        final
        AuxiliaryCache<String, Integer>[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        final int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            final ICacheElement<String, Integer> element = new CacheElement<>( cacheName, keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
            cache.update( element, false );
        }

        final int numToInsertPrefix2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix2; i++ )
        {
            final ICacheElement<String, Integer> element = new CacheElement<>( cacheName, keyprefix2 + String.valueOf( i ), Integer.valueOf( i ) );
            cache.update( element, false );
        }

        final Map<?, ?> result1 = cache.getMatching( keyprefix1 + "\\S+" );
        final Map<?, ?> result2 = cache.getMatching( keyprefix2 + "\\S+" );

        // VERIFY
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2:", numToInsertPrefix2, result2.size() );
    }

    /**
     * Verify we try a disk aux on a getMatching call.
     * <p>
     * @throws IOException
     */
    public void testGetMatching_NotOnDisk()
        throws IOException
    {
        // SETUP
        final int maxMemorySize = 0;
        final String cacheName = "testGetMatching_NotOnDisk";
        final String memoryCacheClassName = "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache";
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMemoryCacheName( memoryCacheClassName );
        cattr.setMaxObjects( maxMemorySize );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, Integer> cache = new CompositeCache<>( cattr, attr );

        final MockAuxiliaryCache<String, Integer> diskMock = new MockAuxiliaryCache<>();
        diskMock.cacheType = CacheType.DISK_CACHE;
        @SuppressWarnings("unchecked")
        final
        AuxiliaryCache<String, Integer>[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        cache.getMatching( "junk" );

        // VERIFY
        assertEquals( "Wrong number of calls", 1, diskMock.getMatchingCallCount );
    }

    /**
     * Verify we try a remote  aux on a getMatching call.
     * <p>
     * @throws IOException
     */
    public void testGetMatching_NotOnRemote()
        throws IOException
    {
        // SETUP
        final int maxMemorySize = 0;
        final String cacheName = "testGetMatching_NotOnDisk";
        final String memoryCacheClassName = "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache";
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMemoryCacheName( memoryCacheClassName );
        cattr.setMaxObjects( maxMemorySize );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, Integer> cache = new CompositeCache<>( cattr, attr );

        final MockAuxiliaryCache<String, Integer> diskMock = new MockAuxiliaryCache<>();
        diskMock.cacheType = CacheType.REMOTE_CACHE;
        @SuppressWarnings("unchecked")
        final
        AuxiliaryCache<String, Integer>[] aux = new AuxiliaryCache[] { diskMock };
        cache.setAuxCaches( aux );

        // DO WORK
        cache.getMatching( "junk" );

        // VERIFY
        assertEquals( "Wrong number of calls", 1, diskMock.getMatchingCallCount );
    }
}
