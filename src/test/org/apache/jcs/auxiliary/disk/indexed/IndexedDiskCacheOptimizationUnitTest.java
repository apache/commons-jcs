package org.apache.jcs.auxiliary.disk.indexed;

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

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * Tests for the optimization routine.
 * <p>
 * @author Aaron Smuts
 */
public class IndexedDiskCacheOptimizationUnitTest
    extends TestCase
{
    /**
     * Set the optimize at remove count to 10. Add 20. Check the file size. Remove 10. Check the
     * times optimized. Check the file size.
     * @throws Exception
     */
    public void testBasicOptimization()
        throws Exception
    {
        // SETUP
        int removeCount = 50;

        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testOptimization" );
        cattr.setMaxKeySize( removeCount * 3 );
        cattr.setOptimizeAtRemoveCount( removeCount );
        cattr.setMaxRecycleBinSize( removeCount * 3 );
        cattr.setDiskPath( "target/test-sandbox/testOptimization" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.removeAll();

        int numberToInsert = removeCount * 2;
        ICacheElement[] elements = DiskTestObjectUtil
            .createCacheElementsWithTestObjectsOfVariableSizes( numberToInsert, cattr.getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.sleep( 1000 );
        long sizeBeforeRemove = disk.getDataFileSize();
        System.out.println( "file sizeBeforeRemove " + sizeBeforeRemove );
        System.out.println( "totalSize inserted " + DiskTestObjectUtil.totalSize( elements, numberToInsert ) );

        // DO WORK
        for ( int i = 0; i < removeCount; i++ )
        {
            disk.doRemove( new Integer( i ) );
        }

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        long sizeAfterRemove = disk.getDataFileSize();
        System.out.println( "file sizeAfterRemove " + sizeAfterRemove );
        long expectedSizeAfterRemove = DiskTestObjectUtil.totalSize( elements, removeCount, elements.length );
        System.out.println( "totalSize expected after remove " + expectedSizeAfterRemove );

        // test is prone to failure for timing reasons.
        if ( expectedSizeAfterRemove != sizeAfterRemove )
        {
            SleepUtil.sleepAtLeast( 2000 );
        }

        assertTrue( "The post optimization size should be smaller.", sizeAfterRemove < sizeBeforeRemove );
        assertEquals( "The file size is not as expected size.", expectedSizeAfterRemove, sizeAfterRemove );
    }
}
