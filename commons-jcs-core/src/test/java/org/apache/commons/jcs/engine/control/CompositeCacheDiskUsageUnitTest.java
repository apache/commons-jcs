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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.auxiliary.MockAuxiliaryCache;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheType.CacheType;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;

import junit.framework.TestCase;

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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCallCount );
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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.spoolToDisk( inputElement );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 0, mock.updateCallCount );
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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCallCount );
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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCallCount );
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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, true );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 0, mock.updateCallCount );
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

        MockAuxiliaryCache<String, String> mock = new MockAuxiliaryCache<String, String>();
        mock.cacheType = CacheType.DISK_CACHE;

        MockAuxiliaryCache<String, String> mockLateral = new MockAuxiliaryCache<String, String>();
        mockLateral.cacheType = CacheType.LATERAL_CACHE;

        @SuppressWarnings("unchecked")
        List<MockAuxiliaryCache<String, String>> aux = Arrays.asList( mock, mockLateral );
        cache.setAuxCaches( aux );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( CACHE_NAME, "key", "value" );

        // DO WORK
        cache.updateAuxiliaries( inputElement, false );

        // VERIFY
        assertEquals( "Wrong number of calls to the disk cache update.", 1, mock.updateCallCount );
        assertEquals( "Wrong element updated.", inputElement, mock.lastUpdatedItem );

        assertEquals( "Wrong number of calls to the lateral cache update.", 1, mockLateral.updateCallCount );
        assertEquals( "Wrong element updated with lateral.", inputElement, mockLateral.lastUpdatedItem );
    }
}
