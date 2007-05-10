package org.apache.jcs.engine.control;

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
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Tests of the disk usage settings for the CompositeCache.
 * <p>
 * @author Aaron Smuts
 */
public class CompositeCacheDiskUsageUnitTest
    extends TestCase
{
    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheUsagePattern.ccf" );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     * <p>
     * @throws CacheException
     */
    public void testSwapConfig() throws CacheException
    {
        JCS swap = JCS.getInstance( "Swap" );
        assertEquals( ICompositeCacheAttributes.DISK_USAGE_PATTERN_SWAP, swap.getCacheAttributes().getDiskUsagePattern() );
    }

    /**
     * Verify that the swap region is set to the correct pattern.
     * <p>
     * @throws CacheException
     */
    public void testUpdateConfig() throws CacheException
    {
        JCS swap = JCS.getInstance( "Update" );
        assertEquals( ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE, swap.getCacheAttributes().getDiskUsagePattern() );
    }

    /**
     * Setup a disk cache. Configure the disk usage pattern to swap. Call spool. Verify that the
     * item is put to disk.
     */
    public void testSpoolAllowed()
    {
        // SETUP
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_SWAP );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_SWAP );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
        cattr.setDiskUsagePattern( ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE );

        IElementAttributes attr = new ElementAttributes();

        CompositeCache cache = new CompositeCache( "testSpoolAllowed", cattr, attr );

        MockAuxCache mock = new MockAuxCache();
        mock.cacheType = AuxiliaryCache.DISK_CACHE;

        MockAuxCache mockLateral = new MockAuxCache();
        mockLateral.cacheType = AuxiliaryCache.LATERAL_CACHE;

        cache.setAuxCaches( new AuxiliaryCache[] { mock, mockLateral } );

        ICacheElement inputElement = new CacheElement( "testSpoolAllowed", "key", "value" );

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
    public class MockAuxCache
        implements AuxiliaryCache
    {
        private static final long serialVersionUID = 1L;

        /**
         * The last item passed to update.
         */
        public ICacheElement lastUpdatedItem;

        /**
         * The number of times update was called.
         */
        public int updateCount = 0;

        /**
         * The type that should be returned from getCacheType.
         */
        public int cacheType = AuxiliaryCache.DISK_CACHE;

        /**
         * Resets counters and catchers.
         */
        public void reset()
        {
            updateCount = 0;
            lastUpdatedItem = null;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#update(org.apache.jcs.engine.behavior.ICacheElement)
         */
        public void update( ICacheElement ce )
            throws IOException
        {
            lastUpdatedItem = ce;
            updateCount++;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#get(java.io.Serializable)
         */
        public ICacheElement get( Serializable key )
            throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#remove(java.io.Serializable)
         */
        public boolean remove( Serializable key )
            throws IOException
        {
            // TODO Auto-generated method stub
            return false;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#removeAll()
         */
        public void removeAll()
            throws IOException
        {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#dispose()
         */
        public void dispose()
            throws IOException
        {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#getSize()
         */
        public int getSize()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatus()
         */
        public int getStatus()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#getCacheName()
         */
        public String getCacheName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#getGroupKeys(java.lang.String)
         */
        public Set getGroupKeys( String group )
            throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
         */
        public IStats getStatistics()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.apache.jcs.engine.behavior.ICache#getStats()
         */
        public String getStats()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Returns the setup cache type. This allows you to use this mock as multiple cache types.
         * <p>
         * (non-Javadoc)
         * @see org.apache.jcs.engine.behavior.ICacheType#getCacheType()
         */
        public int getCacheType()
        {
            return cacheType;
        }

        /**
         * @return Returns the AuxiliaryCacheAttributes.
         */
        public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
        {
            return null;
        }
    }

}
