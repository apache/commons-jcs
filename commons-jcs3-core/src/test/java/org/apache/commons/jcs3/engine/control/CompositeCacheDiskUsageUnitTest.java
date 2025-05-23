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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.CompositeCacheAttributes;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheType.CacheType;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the disk usage settings for the CompositeCache.
 */
class CompositeCacheDiskUsageUnitTest
{
    /**
     * Used to test the disk cache functionality.
     */
    public static class MockAuxCache<K, V>
        extends AbstractAuxiliaryCache<K, V>
    {
        /** The last item passed to update. */
        public ICacheElement<K, V> lastUpdatedItem;

        /** The number of times update was called. */
        public int updateCount;

        /** The type that should be returned from getCacheType. */
        public CacheType cacheType = CacheType.DISK_CACHE;

        /** @throws IOException */
        @Override
        public void dispose()
            throws IOException
        {
            // noop
        }

        /**
         * @param key
         * @return ICacheElement
         * @throws IOException
         */
        @Override
        public ICacheElement<K, V> get( final K key )
            throws IOException
        {
            return null;
        }

        /**
         * @return the AuxiliaryCacheAttributes.
         */
        @Override
        public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
        {
            return null;
        }

        /** @return null */
        @Override
        public String getCacheName()
        {
            return null;
        }

        /**
         * Returns the setup cache type. This allows you to use this mock as multiple cache types.
         *
         * @see org.apache.commons.jcs3.engine.behavior.ICacheType#getCacheType()
         * @return cacheType
         */
        @Override
        public CacheType getCacheType()
        {
            return cacheType;
        }

        /** @return null */
        @Override
        public String getEventLoggingExtraInfo()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @return null
         * @throws IOException
         */
        @Override
        public Set<K> getKeySet( )
            throws IOException
        {
            return null;
        }

        /**
         * @param pattern
         * @return Collections.EMPTY_MAP;
         * @throws IOException
         */
        @Override
        public Map<K, ICacheElement<K, V>> getMatching(final String pattern)
            throws IOException
        {
            return Collections.emptyMap();
        }

        /**
         * Gets multiple items from the cache based on the given set of keys.
         *
         * @param keys
         * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is
         *         no data in cache for any of these keys
         */
        @Override
        public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
        {
            return new HashMap<>();
        }

        /** @return 0 */
        @Override
        public int getSize()
        {
            return 0;
        }

        /** @return null */
        @Override
        public IStats getStatistics()
        {
            return null;
        }

        /** @return null */
        @Override
        public String getStats()
        {
            return null;
        }

        /** @return 0 */
        @Override
        public CacheStatus getStatus()
        {
            return CacheStatus.ALIVE;
        }

        /**
         * @param key
         * @return false
         * @throws IOException
         */
        @Override
        public boolean remove( final K key )
            throws IOException
        {
            return false;
        }

        /** @throws IOException */
        @Override
        public void removeAll()
            throws IOException
        {
            // noop
        }

        /** Resets counters and catchers. */
        public void reset()
        {
            updateCount = 0;
            lastUpdatedItem = null;
        }

        /**
         * @param cacheEventLogger
         */
        @Override
        public void setCacheEventLogger( final ICacheEventLogger cacheEventLogger )
        {
            // TODO Auto-generated method stub

        }

        /**
         * @param elementSerializer
         */
        @Override
        public void setElementSerializer( final IElementSerializer elementSerializer )
        {
            // TODO Auto-generated method stub

        }

        /**
         * @param ce
         * @throws IOException
         */
        @Override
        public void update( final ICacheElement<K, V> ce )
            throws IOException
        {
            lastUpdatedItem = ce;
            updateCount++;
        }

    }

    private static final String CACHE_NAME = "testSpoolAllowed";

    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheUsagePattern.ccf" );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to swap. Call spool. Verify that the
     * item is put to disk.
     */
    @Test
    void testSpoolAllowed()
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.SWAP );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;
        cache.setAuxCaches(Arrays.asList(mock));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( 1, mock.updateCount, "Wrong number of calls to the disk cache update." );
        assertEquals( inputElement, mock.lastUpdatedItem, "Wrong element updated." );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to not swap. Call spool. Verify that the
     * item is not put to disk.
     */
    @Test
    void testSpoolNotAllowed()
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;
        cache.setAuxCaches(Arrays.asList(mock));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( 0, mock.updateCount, "Wrong number of calls to the disk cache update." );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     *
     * @throws CacheException
     */
    @Test
    void testSwapConfig()
        throws CacheException
    {
        final CacheAccess<String, String> swap = JCS.getInstance( "Swap" );
        assertEquals( ICompositeCacheAttributes.DiskUsagePattern.SWAP, swap.getCacheAttributes()
            .getDiskUsagePattern() );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries.
     * Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately.
     * @throws IOException
     */
    @Test
    void testUpdateAllowed()
        throws IOException
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;
        cache.setAuxCaches(Arrays.asList(mock));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( 1, mock.updateCount, "Wrong number of calls to the disk cache update." );
        assertEquals( inputElement, mock.lastUpdatedItem, "Wrong element updated." );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries with
     * local only set to false. Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately. The local setting should have no impact on whether the item goes to disk.
     *
     * @throws IOException
     */
    @Test
    void testUpdateAllowed_localFalse()
        throws IOException
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;
        cache.setAuxCaches(Arrays.asList(mock));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( 1, mock.updateCount, "Wrong number of calls to the disk cache update." );
        assertEquals( inputElement, mock.lastUpdatedItem, "Wrong element updated." );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries.
     * Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately.
     * @throws IOException
     */
    @Test
    void testUpdateAllowed_withOtherCaches()
        throws IOException
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;

        final MockAuxCache<String, String> mockLateral = new MockAuxCache<>();
        mockLateral.cacheType = CacheType.LATERAL_CACHE;
        cache.setAuxCaches(Arrays.asList(mock, mockLateral));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( 1, mock.updateCount, "Wrong number of calls to the disk cache update." );
        assertEquals( inputElement, mock.lastUpdatedItem, "Wrong element updated." );

        assertEquals( 1, mockLateral.updateCount, "Wrong number of calls to the lateral cache update." );
        assertEquals( inputElement, mockLateral.lastUpdatedItem, "Wrong element updated with lateral." );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     *
     * @throws CacheException
     */
    @Test
    void testUpdateConfig()
        throws CacheException
    {
        final CacheAccess<String, String> swap = JCS.getInstance( "Update" );
        assertEquals( ICompositeCacheAttributes.DiskUsagePattern.UPDATE, swap.getCacheAttributes()
            .getDiskUsagePattern() );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to SWAP. Call updateAuxiliaries. Verify
     * that the item is not put to disk.
     * <p>
     * This tests that the items are not put to disk on a normal put when the usage pattern is set
     * to SWAP.
     *
     * @throws IOException
     */
    @Test
    void testUpdateNotAllowed()
        throws IOException
    {
        // SETUP
        final ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.SWAP );

        final IElementAttributes attr = new ElementAttributes();

        final CompositeCache<String, String> cache = new CompositeCache<>( cattr, attr );

        final MockAuxCache<String, String> mock = new MockAuxCache<>();
        mock.cacheType = CacheType.DISK_CACHE;
        cache.setAuxCaches(Arrays.asList(mock));

        final ICacheElement<String, String> inputElement = new CacheElement<>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( 0, mock.updateCount, "Wrong number of calls to the disk cache update." );
    }

}
