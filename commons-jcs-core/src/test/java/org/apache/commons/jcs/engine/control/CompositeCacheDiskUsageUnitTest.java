package org.apache.commons.jcs.engine.control;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheType.CacheType;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs.engine.stats.behavior.IStats;

/**
 * Tests of the disk usage settings for the CompositeCache.
 * <p>
 * @author Aaron Smuts
 */
public class CompositeCacheDiskUsageUnitTest
    extends TestCase
{
    private static final String CACHE_NAME = "testSpoolAllowed";

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheUsagePattern.ccf" );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     * <p>
     * @throws CacheException
     */
    public void testSwapConfig()
        throws CacheException
    {
        CacheAccess<String, String> swap = JCS.getInstance( "Swap" );
        assertEquals( ICompositeCacheAttributes.DiskUsagePattern.SWAP, swap.getCacheAttributes()
            .getDiskUsagePattern() );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     * <p>
     * @throws CacheException
     */
    public void testUpdateConfig()
        throws CacheException
    {
        CacheAccess<String, String> swap = JCS.getInstance( "Update" );
        assertEquals( ICompositeCacheAttributes.DiskUsagePattern.UPDATE, swap.getCacheAttributes()
            .getDiskUsagePattern() );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to swap. Call spool. Verify that the
     * item is put to disk.
     */
    public void testSpoolAllowed()
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.SWAP );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCount );
        assertEquals( "Wrong element updated.", inputElement, mock.lastUpdatedItem );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to not swap. Call spool. Verify that the
     * item is not put to disk.
     */
    public void testSpoolNotAllowed()
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 0, mock.updateCount );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries.
     * Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately.
     * @throws IOException
     */
    public void testUpdateAllowed()
        throws IOException
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCount );
        assertEquals( "Wrong element updated.", inputElement, mock.lastUpdatedItem );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries with
     * local only set to false. Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately. The local setting should have no impact on whether the item goes to disk.
     * <p>
     * @throws IOException
     */
    public void testUpdateAllowed_localFalse()
        throws IOException
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCount );
        assertEquals( "Wrong element updated.", inputElement, mock.lastUpdatedItem );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to SWAP. Call updateAuxiliaries. Verify
     * that the item is not put to disk.
     * <p>
     * This tests that the items are not put to disk on a normal put when the usage pattern is set
     * to SWAP.
     * <p>
     * @throws IOException
     */
    public void testUpdateNotAllowed()
        throws IOException
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.SWAP );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 0, mock.updateCount );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to UPDATE. Call updateAuxiliaries.
     * Verify that the item is put to disk.
     * <p>
     * This tests that the items are put to disk on a normal put when the usage pattern is set
     * appropriately.
     * @throws IOException
     */
    public void testUpdateAllowed_withOtherCaches()
        throws IOException
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setCacheName(CACHE_NAME);
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DiskUsagePattern.UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache<String, String> cache = new CompositeCache<String, String>( cattr, attr );

        MockAuxCache<String, String> mock = new MockAuxCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        MockAuxCache<String, String> mockLateral = new MockAuxCache<String, String>();
        mockLateral.cacheType = CacheType.LATERAL_CACHE;

        @SuppressWarnings("unchecked")
        AuxiliaryCache<String, String>[] auxArray = new AuxiliaryCache[] { mock, mockLateral };
        cache.setAuxCaches( auxArray );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCount );
        assertEquals( "Wrong element updated.", inputElement, mock.lastUpdatedItem );

        assertEquals( "Wrong number of calls to the lateral cache update.", 1, mockLateral.updateCount );
        assertEquals( "Wrong element updated with lateral.", inputElement, mockLateral.lastUpdatedItem );
    }

    /**
     * Used to test the disk cache functionality.
     * <p>
     * @author Aaron Smuts
     */
    public static class MockAuxCache<K, V>
        extends AbstractAuxiliaryCache<K, V>
    {
        /** The last item passed to update. */
        public ICacheElement<K, V> lastUpdatedItem;

        /** The number of times update was called. */
        public int updateCount = 0;

        /** The type that should be returned from getCacheType. */
        public CacheType cacheType = CacheType.DISK_CACHE;

        /** Resets counters and catchers. */
        public void reset()
        {
            updateCount = 0;
            lastUpdatedItem = null;
        }

        /**
         * @param ce
         * @throws IOException
         */
        @Override
        public void update( ICacheElement<K, V> ce )
            throws IOException
        {
            lastUpdatedItem = ce;
            updateCount++;
        }

        /**
         * @param key
         * @return ICacheElement
         * @throws IOException
         */
        @Override
        public ICacheElement<K, V> get( K key )
            throws IOException
        {
            return null;
        }

        /**
         * Gets multiple items from the cache based on the given set of keys.
         * <p>
         * @param keys
         * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is
         *         no data in cache for any of these keys
         */
        @Override
        public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
        {
            return new HashMap<K, ICacheElement<K, V>>();
        }

        /**
         * @param key
         * @return false
         * @throws IOException
         */
        @Override
        public boolean remove( K key )
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

        /** @throws IOException */
        @Override
        public void dispose()
            throws IOException
        {
            // noop
        }

        /** @return 0 */
        @Override
        public int getSize()
        {
            return 0;
        }

        /** @return 0 */
        @Override
        public CacheStatus getStatus()
        {
            return CacheStatus.ALIVE;
        }

        /** @return null */
        @Override
        public String getCacheName()
        {
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

        /**
         * Returns the setup cache type. This allows you to use this mock as multiple cache types.
         * <p>
         * @see org.apache.commons.jcs.engine.behavior.ICacheType#getCacheType()
         * @return cacheType
         */
        @Override
        public CacheType getCacheType()
        {
            return cacheType;
        }

        /**
         * @return Returns the AuxiliaryCacheAttributes.
         */
        @Override
        public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
        {
            return null;
        }

        /**
         * @param cacheEventLogger
         */
        @Override
        public void setCacheEventLogger( ICacheEventLogger cacheEventLogger )
        {
            // TODO Auto-generated method stub

        }

        /**
         * @param elementSerializer
         */
        @Override
        public void setElementSerializer( IElementSerializer elementSerializer )
        {
            // TODO Auto-generated method stub

        }

        /** @return null */
        @Override
        public String getEventLoggingExtraInfo()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @param pattern
         * @return Collections.EMPTY_MAP;
         * @throws IOException
         */
        @Override
        public Map<K, ICacheElement<K, V>> getMatching(String pattern)
            throws IOException
        {
            return Collections.emptyMap();
        }


    }

}
