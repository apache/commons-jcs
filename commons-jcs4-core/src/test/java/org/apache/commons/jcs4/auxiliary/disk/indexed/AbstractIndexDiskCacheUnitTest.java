package org.apache.commons.jcs4.auxiliary.disk.indexed;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs4.auxiliary.disk.DiskTestObject;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.apache.commons.jcs4.engine.control.group.GroupAttrName;
import org.apache.commons.jcs4.engine.control.group.GroupId;
import org.apache.commons.jcs4.utils.timing.SleepUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for common functionality.
 */
public abstract class AbstractIndexDiskCacheUnitTest{
    public abstract IndexedDiskCacheAttributes getCacheAttributes();

    /**
     * Internal method used for group functionality.
     * <p>
     *
     * @param cacheName
     * @param group
     * @param name
     * @return GroupAttrName
     */
    private GroupAttrName<String> getGroupAttrName(final String cacheName, final String group, final String name)
    {
        final GroupId gid = new GroupId(cacheName, group);
        return new GroupAttrName<>(gid, name);
    }

    public void oneLoadFromDisk() throws Exception
    {
        // initialize object to be stored
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();

        // initialize cache
        final String cacheName = "testLoadFromDisk";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        // DO WORK
        for (int i = 0; i < 50; i++)
        {
            diskCache.update(new CacheElement<>(cacheName, "x" + i, string));
        }
        // Thread.sleep(1000);
        // VERIFY
        diskCache.dispose();
        // Thread.sleep(1000);

        diskCache = new IndexedDiskCache<>(cattr);

        for (int i = 0; i < 50; i++)
        {
            final ICacheElement<String, String> afterElement = diskCache.get("x" + i);
            assertNotNull( afterElement,
                           "Missing element from cache. Cache size: " + diskCache.getSize() + " element: x" + i );
            assertEquals( string, afterElement.getVal(), "wrong string after retrieval" );
        }
    }

    /**
     * Verify that we don't override the largest item.
     * <p>
     *
     * @throws IOException
     */

    /**
     * Verify that the data size is as expected after a remove and after a put that should use the
     * spots.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testBytesFreeSize() throws IOException, InterruptedException
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testBytesFreeSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        final IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<>(cattr);

        final int numberToInsert = 20;
        final int bytes = 24;
        final ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (final ICacheElement<Integer, DiskTestObject> element : elements) {
            disk.processUpdate(element);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half of those added
        final int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        final long expectedSize = DiskTestObjectUtil.totalSize(elements, numberToRemove);
        final long resultSize = disk.getBytesFree();

        // System.out.println( "testBytesFreeSize stats " + disk.getStats() );

        assertEquals( expectedSize, resultSize, "Wrong bytes free size" + disk.getStats() );

        // add half as many as we removed. These should all use spots in the recycle bin.
        final int numberToAdd = numberToRemove / 2;
        for (int i = 0; i < numberToAdd; i++)
        {
            disk.processUpdate(elements[i]);
        }

        final long expectedSize2 = DiskTestObjectUtil.totalSize(elements, numberToAdd);
        final long resultSize2 = disk.getBytesFree();
        assertEquals( expectedSize2, resultSize2, "Wrong bytes free size" + disk.getStats() );
    }

    /**
     * Verify that the overlap check returns true when there are no overlaps.
     */
    @Test
    public void testCheckForDedOverlaps_noOverlap()
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testCheckForDedOverlaps_noOverlap");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>(cattr);

        final int numDescriptors = 5;
        int pos = 0;
        final List<IndexedDiskElementDescriptor> sortedDescriptors = new ArrayList<>();
        for (int i = 0; i < numDescriptors; i++)
        {
            final IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor(pos, i * 2);
            pos = pos + i * 2 + IndexedDisk.HEADER_SIZE_BYTES;
            sortedDescriptors.add(descriptor);
        }

        // DO WORK
        final boolean result = disk.checkForDedOverlaps(sortedDescriptors);

        // VERIFY
        assertTrue( result, "There should be no overlap. it should be ok" );
    }

    /**
     * Verify that the overlap check returns false when there are overlaps.
     */
    @Test
    public void testCheckForDedOverlaps_overlaps()
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testCheckForDedOverlaps_overlaps");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>(cattr);

        final int numDescriptors = 5;
        int pos = 0;
        final List<IndexedDiskElementDescriptor> sortedDescriptors = new ArrayList<>();
        for (int i = 0; i < numDescriptors; i++)
        {
            final IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor(pos, i * 2);
            // don't add the header + IndexedDisk.RECORD_HEADER;
            pos = pos + i * 2;
            sortedDescriptors.add(descriptor);
        }

        // DO WORK
        final boolean result = disk.checkForDedOverlaps(sortedDescriptors);

        // VERIFY
        assertFalse( result, "There should be overlaps. it should be not ok" );
    }

    /**
     * Verify that the file size is as expected.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testFileSize() throws IOException, InterruptedException
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testFileSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        final IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<>(cattr);

        final int numberToInsert = 20;
        final int bytes = 24;
        final ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (final ICacheElement<Integer, DiskTestObject> element : elements) {
            disk.processUpdate(element);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        final long expectedSize = DiskTestObjectUtil.totalSize(elements, numberToInsert);
        final long resultSize = disk.getDataFileSize();

        // System.out.println( "testFileSize stats " + disk.getStats() );

        assertEquals( expectedSize, resultSize, "Wrong file size" );
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testGet_EventLogging_simple() throws Exception
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testGet_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);
        diskCache.processRemoveAll();

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.get("key");

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testGetMultiple_EventLogging_simple() throws Exception
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testGetMultiple_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);
        diskCache.processRemoveAll();

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        final Set<String> keys = new HashSet<>();
        keys.add("junk");

        // DO WORK
        diskCache.getMultiple(keys);

        // VERIFY
        // 1 for get multiple and 1 for get.
        assertEquals( 2, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 2, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    @Test
    public void testLoadFromDisk() throws Exception
    {
        for (int i = 0; i < 15; i++)
        { // usually after 2 time it fails
            oneLoadFromDisk();
        }
    }

    /**
     * Verify that the old slot gets in the recycle bin.
     * <p>
     *
     * @throws IOException
     */
    @Test
    public void testProcessUpdate_SameKeyBiggerSize() throws IOException
    {
        // SETUP
        final String cacheName = "testProcessUpdate_SameKeyBiggerSize";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        final String key = "myKey";
        final String value = "myValue";
        final String value2 = "myValue2";
        final ICacheElement<String, String> ce1 = new CacheElement<>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        final long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        final ICacheElement<String, String> ce2 = new CacheElement<>(cacheName, key, value2);
        diskCache.processUpdate(ce2);
        final ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull( result, "Should have a result" );
        final long fileSize2 = diskCache.getDataFileSize();
        assertTrue( fileSize1 < fileSize2, "File should be greater." );
        final int binSize = diskCache.getRecyleBinSize();
        assertEquals( 1, binSize, "Should be one in the bin." );
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    @Test
    public void testProcessUpdate_SameKeySameSize() throws IOException
    {
        // SETUP
        final String cacheName = "testProcessUpdate_SameKeySameSize";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        final String key = "myKey";
        final String value = "myValue";
        final ICacheElement<String, String> ce1 = new CacheElement<>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        final long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        final ICacheElement<String, String> ce2 = new CacheElement<>(cacheName, key, value);
        diskCache.processUpdate(ce2);
        final ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull( result, "Should have a result" );
        final long fileSize2 = diskCache.getDataFileSize();
        assertEquals( fileSize1, fileSize2, "File should be the same" );
        final int binSize = diskCache.getRecyleBinSize();
        assertEquals( 0, binSize, "Should be nothing in the bin." );
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    @Test
    public void testProcessUpdate_SameKeySmallerSize() throws IOException
    {
        // SETUP
        final String cacheName = "testProcessUpdate_SameKeySmallerSize";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        final String key = "myKey";
        final String value = "myValue";
        final String value2 = "myValu";
        final ICacheElement<String, String> ce1 = new CacheElement<>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        final long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        final ICacheElement<String, String> ce2 = new CacheElement<>(cacheName, key, value2);
        diskCache.processUpdate(ce2);
        final ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull( result, "Should have a result" );
        final long fileSize2 = diskCache.getDataFileSize();
        assertEquals( fileSize1, fileSize2, "File should be the same" );
        final int binSize = diskCache.getRecyleBinSize();
        assertEquals( 0, binSize, "Should be nothing in the bin." );
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    @Test
    public void testProcessUpdate_Simple() throws IOException
    {
        // SETUP
        final String cacheName = "testProcessUpdate_Simple";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        final String key = "myKey";
        final String value = "myValue";
        final ICacheElement<String, String> ce = new CacheElement<>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce);
        final ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull( result, "Should have a result" );
        final long fileSize = diskCache.getDataFileSize();
        assertTrue( fileSize > 0, "File should be greater than 0" );
    }

    /**
     * Test the basic get matching. With no wait this will all come from purgatory.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testPutGetMatching_NoWait() throws Exception
    {
        // SETUP
        final int items = 200;

        final String cacheName = "testPutGetMatching_NoWait";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        // DO WORK
        for (int i = 0; i < items; i++)
        {
            diskCache.update(new CacheElement<>(cacheName, i + ":key", cacheName + " data " + i));
        }

        final Map<String, ICacheElement<String, String>> matchingResults = diskCache.getMatching("1.8.+");

        // VERIFY
        assertEquals( 10, matchingResults.size(), "Wrong number returned" );
        // System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        // System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }

    /**
     * Test the basic get matching.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testPutGetMatching_SmallWait() throws Exception
    {
        // SETUP
        final int items = 200;

        final String cacheName = "testPutGetMatching_SmallWait";
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        // DO WORK
        for (int i = 0; i < items; i++)
        {
            diskCache.update(new CacheElement<>(cacheName, i + ":key", cacheName + " data " + i));
        }
        Thread.sleep(500);

        final Map<String, ICacheElement<String, String>> matchingResults = diskCache.getMatching("1.8.+");

        // VERIFY
        assertEquals( 10, matchingResults.size(), "Wrong number returned" );
        // System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        // System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }

    /**
     * Verify that items are added to the recycle bin on removal.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRecyleBinSize() throws IOException, InterruptedException
    {
        // SETUP
        final int numberToInsert = 20;

        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRecyleBinSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        cattr.setOptimizeAtRemoveCount(numberToInsert);
        cattr.setMaxKeySize(numberToInsert * 2);
        cattr.setMaxPurgatorySize(numberToInsert);
        final IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<>(cattr);

        final int bytes = 1;
        final ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (final ICacheElement<Integer, DiskTestObject> element : elements) {
            disk.processUpdate(element);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half
        final int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        // verify that the recycle bin has the correct amount.
        assertEquals( numberToRemove, disk.getRecyleBinSize(), "The recycle bin should have the number removed." );
    }

    /**
     * Verify that items of the same size use recycle bin spots. Setup the recycle bin by removing
     * some items. Add some of the same size. Verify that the recycle count is the number added.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRecyleBinUsage() throws IOException, InterruptedException
    {
        // SETUP
        final int numberToInsert = 20;

        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRecyleBinUsage");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        cattr.setOptimizeAtRemoveCount(numberToInsert);
        cattr.setMaxKeySize(numberToInsert * 2);
        cattr.setMaxPurgatorySize(numberToInsert);
        final IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<>(cattr);

        // we will reuse these
        final int bytes = 1;
        final ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        // Add some to the disk
        for (final ICacheElement<Integer, DiskTestObject> element : elements) {
            disk.processUpdate(element);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half of those added
        final int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        // verify that the recycle bin has the correct amount.
        assertEquals( numberToRemove, disk.getRecyleBinSize(), "The recycle bin should have the number removed." );

        // add half as many as we removed. These should all use spots in the recycle bin.
        final int numberToAdd = numberToRemove / 2;
        for (int i = 0; i < numberToAdd; i++)
        {
            disk.processUpdate(elements[i]);
        }

        // verify that we used the correct number of spots
        assertEquals( numberToAdd, disk.getRecyleCount(),
                      "The recycle bin should have the number removed." + disk.getStats() );
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testRemove_EventLogging_simple() throws Exception
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveAll_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);
        diskCache.processRemoveAll();

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.remove("key");

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify that group members are removed if we call remove with a group.
     *
     * @throws IOException
     */
    @Test
    public void testRemove_Group() throws IOException
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_Group");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<GroupAttrName<String>, String> disk = new IndexedDiskCache<>(cattr);

        disk.processRemoveAll();

        final String cacheName = "testRemove_Group_Region";
        final String groupName = "testRemove_Group";

        final int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            final GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            final CacheElement<GroupAttrName<String>, String> element = new CacheElement<>(cacheName,
                groupAttrName, "data:" + i);

            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            element.setElementAttributes(eAttr);

            disk.processUpdate(element);
        }

        // verify each
        for (int i = 0; i < cnt; i++)
        {
            final GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            final ICacheElement<GroupAttrName<String>, String> element = disk.processGet(groupAttrName);
            assertNotNull( element, "Should have received an element." );
        }

        // DO WORK
        // remove the group
        disk.remove(getGroupAttrName(cacheName, groupName, null));

        for (int i = 0; i < cnt; i++)
        {
            final GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            final ICacheElement<GroupAttrName<String>, String> element = disk.processGet(groupAttrName);

            // VERIFY
            assertNull( element, "Should not have received an element." );
        }

    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     * <p>
     *
     * @throws IOException
     */
    @Test
    public void testRemove_PartialKey() throws IOException
    {
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_PartialKey");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>(cattr);

        disk.processRemoveAll();

        final int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            final ICacheElement<String, String> element = new CacheElement<>("testRemove_PartialKey", i + ":key", "data:"
                + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        // verif each
        for (int i = 0; i < cnt; i++)
        {
            final ICacheElement<String, String> element = disk.processGet(i + ":key");
            assertNotNull( element, "Shoulds have received an element." );
        }

        // remove each
        for (int i = 0; i < cnt; i++)
        {
            disk.remove(i + ":");
            final ICacheElement<String, String> element = disk.processGet(i + ":key");
            assertNull( element, "Should not have received an element." );
        }
        // https://issues.apache.org/jira/browse/JCS-67
        assertEquals( cnt, disk.getRecyleBinSize(),
                      "Recylenbin should not have more elements than we removed. Check for JCS-67" );
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testRemoveAll_EventLogging_simple() throws Exception
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveAll_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);
        diskCache.processRemoveAll();

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.remove("key");

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     *
     * @throws IOException
     */
    @Test
    public void testRemoveItems() throws IOException
    {
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveItems");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>(cattr);

        disk.processRemoveAll();

        final int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            final ICacheElement<String, String> element = new CacheElement<>("testRemoveItems", "key:" + i, "data:" + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        // remove each
        for (int i = 0; i < cnt; i++)
        {
            disk.remove("key:" + i);
            final ICacheElement<String, String> element = disk.processGet("key:" + i);
            assertNull( element, "Should not have received an element." );
        }
    }

    /**
     * Simply verify that we can put items in the disk cache and retrieve them.
     *
     * @throws IOException
     */
    @Test
    public void testSimplePutAndGet() throws IOException
    {
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testSimplePutAndGet");
        cattr.setMaxKeySize(1000);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>(cattr);

        disk.processRemoveAll();

        final int cnt = 999;
        for (int i = 0; i < cnt; i++)
        {
            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            final ICacheElement<String, String> element = new CacheElement<>("testSimplePutAndGet", "key:" + i, "data:" + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        for (int i = 0; i < cnt; i++)
        {
            final ICacheElement<String, String> element = disk.processGet("key:" + i);
            assertNotNull( element, "Should have received an element." );
            assertEquals( "data:" + i, element.getVal(), "Element is wrong." );
        }

        // Test that getMultiple returns all the expected values
        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < cnt; i++)
        {
            keys.add("key:" + i);
        }

        final Map<String, ICacheElement<String, String>> elements = disk.getMultiple(keys);
        for (int i = 0; i < cnt; i++)
        {
            final ICacheElement<String, String> element = elements.get("key:" + i);
            assertNotNull( element, "element " + i + ":key is missing" );
            assertEquals( "data:" + i, element.getVal(), "value key:" + i );
        }
        // System.out.println( disk.getStats() );
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testUpdate_EventLogging_simple() throws Exception
    {
        // SETUP
        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testUpdate_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);
        diskCache.processRemoveAll();

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        final ICacheElement<String, String> item = new CacheElement<>("region", "key", "value");

        // DO WORK
        diskCache.update(item);

        SleepUtil.sleepAtLeast(200);

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testUTF8ByteArray() throws Exception
    {
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();
        // System.out.println( "The string contains " + string.length() + " characters" );
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        final String cacheName = "testUTF8ByteArray";

        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, byte[]> diskCache = new IndexedDiskCache<>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<>(cacheName, "x", bytes));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        final ICacheElement<String, byte[]> afterElement = diskCache.get("x");
        assertNotNull(afterElement);
        // System.out.println( "afterElement = " + afterElement );
        final byte[] after = afterElement.getVal();

        assertNotNull(after);
        assertEquals( string, new String( after, StandardCharsets.UTF_8 ), "wrong bytes after retrieval" );
    }

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testUTF8String() throws Exception
    {
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();

        // System.out.println( "The string contains " + string.length() + " characters" );

        final String cacheName = "testUTF8String";

        final IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        final IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<>(cacheName, "x", string));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        final ICacheElement<String, String> afterElement = diskCache.get("x");
        assertNotNull(afterElement);
        // System.out.println( "afterElement = " + afterElement );
        final String after = afterElement.getVal();

        assertNotNull(after);
        assertEquals( string, after, "wrong string after retrieval" );
    }
}
