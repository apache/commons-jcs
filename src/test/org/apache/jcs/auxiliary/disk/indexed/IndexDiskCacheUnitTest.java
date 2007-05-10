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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;

/**
 * Tests for common functionality.
 * <p>
 * @author Aaron Smuts
 */
public class IndexDiskCacheUnitTest
    extends TestCase
{
    /**
     * Simply verify that we can put items in the disk cache and retrieve them.
     */
    public void testSimplePutAndGet()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testSimplePutAndGet" );
        cattr.setMaxKeySize( 1000 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 999;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testSimplePutAndGet", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( "key:" + i );
            assertNotNull( "Should have recevied an element.", element );
            assertEquals( "Element is wrong.", "data:" + i, element.getVal() );
        }

        System.out.println( disk.getStats() );
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     */
    public void testRemoveItems()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemoveItems" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testRemoveItems", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        // remove each
        for ( int i = 0; i < cnt; i++ )
        {
            disk.remove( "key:" + i );
            ICacheElement element = disk.doGet( "key:" + i );
            assertNull( "Should not have recevied an element.", element );
        }
    }

    /**
     * Verify that we don't override the largest item.
     */
    public void testRecycleBin()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemoveItems" );
        cattr.setMaxRecycleBinSize( 2 );
        cattr.setOptimizeAtRemoveCount( 7 );
        cattr.setMaxKeySize( 5 );
        cattr.setMaxPurgatorySize( 0 );
        cattr.setDiskPath( "target/test-sandbox/BreakIndexTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        String[] test = { "a", "bb", "ccc", "dddd", "eeeee", "ffffff", "ggggggg", "hhhhhhhhh", "iiiiiiiiii" };
        String[] expect = { null, "bb", "ccc", null, null, "ffffff", null, "hhhhhhhhh", "iiiiiiiiii" };

        System.out.println( "------------------------- testRecycleBin " );

        for ( int i = 0; i < 6; i++ )
        {
            ICacheElement element = new CacheElement( "testRecycleBin", "key:" + test[i], test[i] );
            System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
            disk.doUpdate( element );
        }

        for ( int i = 3; i < 5; i++ )
        {
            System.out.println( "About to remove " + "key:" + test[i] + " i = " + i );
            disk.remove( "key:" + test[i] );
        }

        // there was a bug where 7 would try to be put in the empty slot left by 4's removal, but it
        // will not fit.
        for ( int i = 7; i < 9; i++ )
        {
            ICacheElement element = new CacheElement( "testRecycleBin", "key:" + test[i], test[i] );
            System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
            disk.doUpdate( element );
        }

        try
        {
            for ( int i = 0; i < 9; i++ )
            {
                ICacheElement element = disk.get( "key:" + test[i] );
                if ( element != null )
                {
                    System.out.println( "element = " + element.getVal() );
                }
                else
                {
                    System.out.println( "null --" + "key:" + test[i] );
                }

                String expectedValue = expect[i];
                if ( expectedValue == null )
                {
                    assertNull( "Expected a null element", element );
                }
                else
                {
                    assertNotNull( "The element for key [" + "key:" + test[i] + "] should not be null. i = " + i,
                                   element );
                    assertEquals( "Elements contents do not match expected", element.getVal(), expectedValue );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Should not get an exception: " + e.toString() );
        }

        disk.removeAll();
    }

    /**
     * Verify that the overlap check returns true when there are no overlaps.
     */
    public void testCheckForDedOverlaps_noOverlap()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testCheckForDedOverlaps_noOverlap" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for ( int i = 0; i < numDescriptors; i++ )
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor( pos, i * 2 );
            pos = pos + ( i * 2 ) + IndexedDisk.RECORD_HEADER;
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps( sortedDescriptors );

        // VERIFY
        assertTrue( "There should be no overlap. it should be ok", result );
    }

    /**
     * Verify that the overlap check returns false when there are overlaps.
     */
    public void testCheckForDedOverlaps_overlaps()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testCheckForDedOverlaps_overlaps" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for ( int i = 0; i < numDescriptors; i++ )
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor( pos, i * 2 );
            // don't add the header + IndexedDisk.RECORD_HEADER;
            pos = pos + ( i * 2 );
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps( sortedDescriptors );

        // VERIFY
        assertFalse( "There should be overlaps. it should be not ok", result );
    }

    /**
     * Verify that the file size is as expected.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testFileSize()
        throws IOException, InterruptedException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testFileSize" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numberToInsert = 20;
        int bytes = 24;
        ICacheElement[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects( numberToInsert, bytes, cattr
            .getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        long expectedSize = DiskTestObjectUtil.totalSize( elements, numberToInsert );
        long resultSize = disk.getDataFileSize();

        System.out.println( "testFileSize stats " + disk.getStats() );

        assertEquals( "Wrong file size", expectedSize, resultSize );
    }

    /**
     * Verify that items are added to the recyle bin on removal.
     * <p>
     * @throws IOException
     * @throws InterruptedException
     */
    public void testRecyleBinSize()
        throws IOException, InterruptedException
    {
        // SETUP
        int numberToInsert = 20;

        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRecyleBinSize" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        cattr.setMaxRecycleBinSize( numberToInsert );
        cattr.setOptimizeAtRemoveCount( numberToInsert );
        cattr.setMaxKeySize( numberToInsert * 2 );
        cattr.setMaxPurgatorySize( numberToInsert );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int bytes = 24;
        ICacheElement[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects( numberToInsert, bytes, cattr
            .getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        // remove half
        int numberToRemove = elements.length / 2;
        for ( int i = 0; i < numberToRemove; i++ )
        {
            disk.doRemove( elements[i].getKey() );
        }

        // verify that the recyle bin has the correct amount.
        assertEquals( "The recycle bin should have the number removed.", numberToRemove, disk.getRecyleBinSize() );
    }

    /**
     * Verify that items of the same size use recyle bin spots. Setup the receyle bin by removing
     * some items. Add some of the same size. Verify that the recyle count is the number added.
     * <p>
     * @throws IOException
     * @throws InterruptedException
     */
    public void testRecyleBinUsage()
        throws IOException, InterruptedException
    {
        // SETUP
        int numberToInsert = 20;

        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRecyleBinUsage" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        cattr.setMaxRecycleBinSize( numberToInsert );
        cattr.setOptimizeAtRemoveCount( numberToInsert );
        cattr.setMaxKeySize( numberToInsert * 2 );
        cattr.setMaxPurgatorySize( numberToInsert );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        // we will reuse these
        int bytes = 24;
        ICacheElement[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects( numberToInsert, bytes, cattr
            .getCacheName() );

        // Add some to the disk
        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        // remove half of those added
        int numberToRemove = elements.length / 2;
        for ( int i = 0; i < numberToRemove; i++ )
        {
            disk.doRemove( elements[i].getKey() );
        }

        // verify that the recyle bin has the correct amount.
        assertEquals( "The recycle bin should have the number removed.", numberToRemove, disk.getRecyleBinSize() );

        // add half as many as we removed. These should all use spots in the recycle bin.
        int numberToAdd = numberToRemove / 2;
        for ( int i = 0; i < numberToAdd; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        // verify that we used the correct number of spots
        assertEquals( "The recycle bin should have the number removed." + disk.getStats(), numberToAdd, disk
            .getRecyleCount() );
    }

    /**
     * Verify that the data size is as expected after a remove and after a put that should use the
     * spots.
     * <p>
     * @throws IOException
     * @throws InterruptedException
     */
    public void testBytesFreeSize()
        throws IOException, InterruptedException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testBytesFreeSize" );
        cattr.setDiskPath( "target/test-sandbox/UnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        int numberToInsert = 20;
        int bytes = 24;
        ICacheElement[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects( numberToInsert, bytes, cattr
            .getCacheName() );

        for ( int i = 0; i < elements.length; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        // remove half of those added
        int numberToRemove = elements.length / 2;
        for ( int i = 0; i < numberToRemove; i++ )
        {
            disk.doRemove( elements[i].getKey() );
        }

        long expectedSize = DiskTestObjectUtil.totalSize( elements, numberToRemove );
        long resultSize = disk.getBytesFree();

        System.out.println( "testBytesFreeSize stats " + disk.getStats() );

        assertEquals( "Wrong bytes free size" + disk.getStats(), expectedSize, resultSize );

        // add half as many as we removed. These should all use spots in the recycle bin.
        int numberToAdd = numberToRemove / 2;
        for ( int i = 0; i < numberToAdd; i++ )
        {
            disk.doUpdate( elements[i] );
        }

        long expectedSize2 = DiskTestObjectUtil.totalSize( elements, numberToAdd );
        long resultSize2 = disk.getBytesFree();
        assertEquals( "Wrong bytes free size" + disk.getStats(), expectedSize2, resultSize2 );
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     */
    public void testRemove_PartialKey()
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemove_PartialKey" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testRemove_PartialKey", i + ":key", "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        // verif each
        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( i + ":key" );
            assertNotNull( "Shoulds have recevied an element.", element );
        }

        // remove each
        for ( int i = 0; i < cnt; i++ )
        {
            disk.remove( i + ":" );
            ICacheElement element = disk.doGet( i + ":key" );
            assertNull( "Should not have recevied an element.", element );
        }
    }

    /**
     * Verify that group members are removed if we call remove with a group.
     */
    public void testRemove_Group()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testRemove_Group" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/IndexDiskCacheUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        String cacheName = "testRemove_Group_Region";
        String groupName = "testRemove_Group";

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            GroupAttrName groupAttrName = getGroupAttrName( cacheName, groupName, i + ":key" );

            CacheElement element = new CacheElement( cacheName, groupAttrName, "data:" + i );

            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            element.setElementAttributes( eAttr );

            disk.doUpdate( element );
        }

        // verify each
        for ( int i = 0; i < cnt; i++ )
        {
            GroupAttrName groupAttrName = getGroupAttrName( cacheName, groupName, i + ":key" );
            ICacheElement element = disk.doGet( groupAttrName );
            assertNotNull( "Should have recevied an element.", element );
        }

        // DO WORK
        // remove the group
        GroupId gid = new GroupId( cacheName, groupName );
        disk.remove( gid );

        for ( int i = 0; i < cnt; i++ )
        {
            GroupAttrName groupAttrName = getGroupAttrName( cacheName, groupName, i + ":key" );
            ICacheElement element = disk.doGet( groupAttrName );

            // VERIFY
            assertNull( "Should not have recevied an element.", element );
        }

    }

    /**
     * Internal method used for group functionality.
     * <p>
     * @param cacheName
     * @param group
     * @param name
     * @return GroupAttrName
     */
    private GroupAttrName getGroupAttrName( String cacheName, String group, Object name )
    {
        GroupId gid = new GroupId( cacheName, group );
        return new GroupAttrName( gid, name );
    }
}
