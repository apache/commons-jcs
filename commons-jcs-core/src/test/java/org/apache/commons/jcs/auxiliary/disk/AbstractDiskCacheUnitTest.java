package org.apache.commons.jcs.auxiliary.disk;

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

import junit.framework.TestCase;

import org.apache.commons.jcs.TestLogConfigurationUtil;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.disk.behavior.IDiskCacheAttributes;
import org.apache.commons.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;

/** Tests for the abstract disk cache. It's largely tested by actual instances. */
public class AbstractDiskCacheUnitTest
    extends TestCase
{
    /**
     * Verify that update and get work.
     * <p>
     * @throws IOException
     */
    public void testUpdateGet_allowed()
        throws IOException
    {
        // SETUP
        String cacheName = "testUpdateGet_allowed";
        IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setCacheName( cacheName );

        AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<String, String>( diskCacheAttributes );

        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        ICacheElement<String, String> result = diskCache.get( key );

        // VERIFY
        //System.out.println( diskCache.getStats() );
        assertNotNull( "Item should be in the map.", result );
    }

    /**
     * Verify that alive is set to false..
     * <p>
     * @throws IOException
     */
    public void testDispose()
        throws IOException
    {
        // SETUP
        String cacheName = "testDispose";
        IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setCacheName( cacheName );

        AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<String, String>( diskCacheAttributes );

        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.dispose();

        // VERIFY
        assertFalse( "disk cache should not be alive.", diskCache.isAlive() );
        assertEquals( "Status should be disposed", CacheStatus.DISPOSED, diskCache.getStatus() );
    }

    /**
     * Verify that removeAll is prohibited.
     * <p>
     * @throws IOException
     */
    public void testRemoveAll_notAllowed()
        throws IOException
    {
        // SETUP
        StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, AbstractDiskCache.class.getName() );

        IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setAllowRemoveAll( false );

        AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<String, String>( diskCacheAttributes );

        String cacheName = "testRemoveAll_notAllowed";
        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.removeAll();
        String result = stringWriter.toString();

        // VERIFY
        assertTrue( "Should say not allowed.", result.indexOf( "set to false" ) != -1 );
        assertNotNull( "Item should be in the map.", diskCache.get( key ) );
    }

    /**
     * Verify that removeAll is allowed.
     * <p>
     * @throws IOException
     */
    public void testRemoveAll_allowed()
        throws IOException
    {
        // SETUP
        IDiskCacheAttributes diskCacheAttributes = new IndexedDiskCacheAttributes();
        diskCacheAttributes.setAllowRemoveAll( true );

        AbstractDiskCacheTestInstance<String, String> diskCache = new AbstractDiskCacheTestInstance<String, String>( diskCacheAttributes );

        String cacheName = "testRemoveAll_allowed";
        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );

        diskCache.update( cacheElement );

        // DO WORK
        diskCache.removeAll();

        // VERIFY
        assertNull( "Item should not be in the map.", diskCache.get( key ) );
    }

    /** Concrete, testable instance. */
    protected static class AbstractDiskCacheTestInstance<K, V>
        extends AbstractDiskCache<K, V>
    {
        /** Internal map */
        protected Map<K, ICacheElement<K, V>> map = new HashMap<K, ICacheElement<K, V>>();

        /** used by the abstract aux class */
        protected IDiskCacheAttributes diskCacheAttributes;

        /**
         * Creates the disk cache.
         * <p>
         * @param attr
         */
        public AbstractDiskCacheTestInstance( IDiskCacheAttributes attr )
        {
            super( attr );
            diskCacheAttributes = attr;
            setAlive(true);
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
         * @see org.apache.commons.jcs.auxiliary.disk.AbstractDiskCache#getKeySet()
         */
        @Override
        public Set<K> getKeySet() throws IOException
        {
            return new HashSet<K>(map.keySet());
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
        protected ICacheElement<K, V> processGet( K key )
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
        protected Map<K, ICacheElement<K, V>> processGetMatching( String pattern )
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
        protected boolean processRemove( K key )
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
        protected void processUpdate( ICacheElement<K, V> cacheElement )
            throws IOException
        {
            //System.out.println( "processUpdate: " + cacheElement );
            map.put( cacheElement.getKey(), cacheElement );
        }

        /**
         * @return null
         */
        @Override
        public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
        {
            return diskCacheAttributes;
        }
    }
}
