package org.apache.commons.jcs3.auxiliary.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.TestLogConfigurationUtil;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.behavior.IDiskCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.junit.Test;

/** Tests for the abstract disk cache. It's largely tested by actual instances. */
public class AbstractDiskCacheUnitTest
{
    /** Concrete, testable instance. */
    protected static class AbstractDiskCacheTestInstance<K, V>
        extends AbstractDiskCache<K, V>
    {
        /** Internal map */
        protected Map<K, ICacheElement<K, V>> map = new HashMap<>();

        /** Used by the abstract aux class */
        protected IDiskCacheAttributes diskCacheAttributes;

        /**
         * Creates the disk cache.
         * <p>
         * @param attr
         */
        public AbstractDiskCacheTestInstance( final IDiskCacheAttributes attr )
        {
            super( attr );
            diskCacheAttributes = attr;
            setAlive(true);
        }

        /**
         * @return null
         */
        @Override
        public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
        {
            return diskCacheAttributes;
        }

        /**
         * The location on disk
         * <p>
         * @return "memory"
         */
        @Override
        protected String getDiskLocation()
        {
            return "memory";
        }

        /**
         * Return the keys in this cache.
         * <p>
         * @see org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache#getKeySet()
         */
        @Override
        public Set<K> getKeySet() throws IOException
        {
            return new HashSet<>(map.keySet());
        }

        /**
         * @return map.size()
         */
        @Override
        public int getSize()
        {
            return map.size();
        }

        /**
         * @throws IOException
         */
        @Override
        protected void processDispose()
            throws IOException
        {
            //System.out.println( "processDispose" );
        }

        /**
         * @param key
         * @return ICacheElement
         * @throws IOException
         */
        @Override
        protected ICacheElement<K, V> processGet( final K key )
            throws IOException
        {
            //System.out.println( "processGet: " + key );
            return map.get( key );
        }

        /**
         * @param pattern
         * @return Collections.EMPTY_MAP
         * @throws IOException
         */
        @Override
        protected Map<K, ICacheElement<K, V>> processGetMatching( final String pattern )
            throws IOException
        {
            return Collections.emptyMap();
        }

        /**
         * @param key
         * @return false
         * @throws IOException
         */
        @Override
        protected boolean processRemove( final K key )
            throws IOException
        {
            return map.remove( key ) != null;
        }

        /**
         * @throws IOException
         */
        @Override
        protected void processRemoveAll()
            throws IOException
        {
            //System.out.println( "processRemoveAll" );
            map.clear();
        }

        /**
         * @param cacheElement
         * @throws IOException
         */
        @Override
        protected void processUpdate( final ICacheElement<K, V> cacheElement )
            throws IOException
        {
            //System.out.println( "processUpdate: " + cacheElement );
            map.put( cacheElement.getKey(), cacheElement );
        }
    }

    /**
     * Verify that alive is set to false..
     * <p>
     * @throws IOException
     */
    @Test
    public void testDispose()
        throws IOException
    {
        // SETUP
        final String cacheName = "testDispose";
        final IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setCacheName( cacheName );

        final AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<>( diskCacheAttributes );

        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.dispose();

        // VERIFY
        assertFalse( "disk cache should not be alive.", diskCache.isAlive() );
        assertEquals( "Status should be disposed", CacheStatus.DISPOSED, diskCache.getStatus() );
    }

    /**
     * Verify that removeAll is allowed.
     * <p>
     * @throws IOException
     */
    @Test
    public void testRemoveAll_allowed()
        throws IOException
    {
        // SETUP
        final IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setAllowRemoveAll( true );

        final AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<>( diskCacheAttributes );

        final String cacheName = "testRemoveAll_allowed";
        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.removeAll();

        // VERIFY
        assertNull( "Item should not be in the map.", diskCache.get( key ) );
    }

    /**
     * Verify that removeAll is prohibited.
     * <p>
     * @throws IOException
     */
    @Test
    public void testRemoveAll_notAllowed()
        throws IOException
    {
        // SETUP
        final StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, AbstractDiskCache.class.getName() );

        final IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setAllowRemoveAll( false );

        final AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<>( diskCacheAttributes );

        final String cacheName = "testRemoveAll_notAllowed";
        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.removeAll();
        final String result = stringWriter.toString();

        // VERIFY
        assertTrue( "Should say not allowed.", result.indexOf( "set to false" ) != -1 );
        assertNotNull( "Item should be in the map.", diskCache.get( key ) );
    }

    /**
     * Verify that update and get work.
     * <p>
     * @throws IOException
     */
    @Test
    public void testUpdateGet_allowed()
        throws IOException
    {
        // SETUP
        final String cacheName = "testUpdateGet_allowed";
        final IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setCacheName( cacheName );

        final AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<>( diskCacheAttributes );

        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        final ICacheElement<String, String> result = diskCache.get( key );

        // VERIFY
        //System.out.println( diskCache.getStats() );
        assertNotNull( "Item should be in the map.", result );
    }
}
