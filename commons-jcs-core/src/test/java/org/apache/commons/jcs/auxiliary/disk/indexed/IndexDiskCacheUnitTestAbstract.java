package org.apache.commons.jcs.auxiliary.disk.indexed;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.auxiliary.disk.DiskTestObject;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.control.group.GroupAttrName;
import org.apache.commons.jcs.engine.control.group.GroupId;
import org.apache.commons.jcs.utils.timing.SleepUtil;

import junit.framework.TestCase;

/**
 * Tests for common functionality.
 * <p>
 *
 * @author Aaron Smuts
 */
public abstract class IndexDiskCacheUnitTestAbstract extends TestCase
{
    public abstract IndexedDiskCacheAttributes getCacheAttributes();

    /**
     * Simply verify that we can put items in the disk cache and retrieve them.
     *
     * @throws IOException
     */
    public void testSimplePutAndGet() throws IOException
    {
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testSimplePutAndGet");
        cattr.setMaxKeySize(1000);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>(cattr);

        disk.processRemoveAll();

        int cnt = 999;
        for (int i = 0; i < cnt; i++)
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            ICacheElement<String, String> element = new CacheElement<String, String>("testSimplePutAndGet", "key:" + i, "data:" + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        for (int i = 0; i < cnt; i++)
        {
            ICacheElement<String, String> element = disk.processGet("key:" + i);
            assertNotNull("Should have received an element.", element);
            assertEquals("Element is wrong.", "data:" + i, element.getVal());
        }

        // Test that getMultiple returns all the expected values
        Set<String> keys = new HashSet<String>();
        for (int i = 0; i < cnt; i++)
        {
            keys.add("key:" + i);
        }

        Map<String, ICacheElement<String, String>> elements = disk.getMultiple(keys);
        for (int i = 0; i < cnt; i++)
        {
            ICacheElement<String, String> element = elements.get("key:" + i);
            assertNotNull("element " + i + ":key is missing", element);
            assertEquals("value key:" + i, "data:" + i, element.getVal());
        }
        // System.out.println( disk.getStats() );
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     *
     * @throws IOException
     */
    public void testRemoveItems() throws IOException
    {
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveItems");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>(cattr);

        disk.processRemoveAll();

        int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            ICacheElement<String, String> element = new CacheElement<String, String>("testRemoveItems", "key:" + i, "data:" + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        // remove each
        for (int i = 0; i < cnt; i++)
        {
            disk.remove("key:" + i);
            ICacheElement<String, String> element = disk.processGet("key:" + i);
            assertNull("Should not have received an element.", element);
        }
    }

    /**
     * Verify that we don't override the largest item.
     * <p>
     *
     * @throws IOException
     */

    /**
     * Verify that the overlap check returns true when there are no overlaps.
     */
    public void testCheckForDedOverlaps_noOverlap()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testCheckForDedOverlaps_noOverlap");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>(cattr);

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for (int i = 0; i < numDescriptors; i++)
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor(pos, i * 2);
            pos = pos + (i * 2) + IndexedDisk.HEADER_SIZE_BYTES;
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps(sortedDescriptors);

        // VERIFY
        assertTrue("There should be no overlap. it should be ok", result);
    }

    /**
     * Verify that the overlap check returns false when there are overlaps.
     */
    public void testCheckForDedOverlaps_overlaps()
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testCheckForDedOverlaps_overlaps");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>(cattr);

        int numDescriptors = 5;
        int pos = 0;
        IndexedDiskElementDescriptor[] sortedDescriptors = new IndexedDiskElementDescriptor[numDescriptors];
        for (int i = 0; i < numDescriptors; i++)
        {
            IndexedDiskElementDescriptor descriptor = new IndexedDiskElementDescriptor(pos, i * 2);
            // don't add the header + IndexedDisk.RECORD_HEADER;
            pos = pos + (i * 2);
            sortedDescriptors[i] = descriptor;
        }

        // DO WORK
        boolean result = disk.checkForDedOverlaps(sortedDescriptors);

        // VERIFY
        assertFalse("There should be overlaps. it should be not ok", result);
    }

    /**
     * Verify that the file size is as expected.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testFileSize() throws IOException, InterruptedException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testFileSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<Integer, DiskTestObject>(cattr);

        int numberToInsert = 20;
        int bytes = 24;
        ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (int i = 0; i < elements.length; i++)
        {
            disk.processUpdate(elements[i]);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        long expectedSize = DiskTestObjectUtil.totalSize(elements, numberToInsert);
        long resultSize = disk.getDataFileSize();

        // System.out.println( "testFileSize stats " + disk.getStats() );

        assertEquals("Wrong file size", expectedSize, resultSize);
    }

    /**
     * Verify that items are added to the recycle bin on removal.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testRecyleBinSize() throws IOException, InterruptedException
    {
        // SETUP
        int numberToInsert = 20;

        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRecyleBinSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        cattr.setOptimizeAtRemoveCount(numberToInsert);
        cattr.setMaxKeySize(numberToInsert * 2);
        cattr.setMaxPurgatorySize(numberToInsert);
        IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<Integer, DiskTestObject>(cattr);

        int bytes = 1;
        ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (int i = 0; i < elements.length; i++)
        {
            disk.processUpdate(elements[i]);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half
        int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        // verify that the recycle bin has the correct amount.
        assertEquals("The recycle bin should have the number removed.", numberToRemove, disk.getRecyleBinSize());
    }

    /**
     * Verify that items of the same size use recycle bin spots. Setup the recycle bin by removing
     * some items. Add some of the same size. Verify that the recycle count is the number added.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testRecyleBinUsage() throws IOException, InterruptedException
    {
        // SETUP
        int numberToInsert = 20;

        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRecyleBinUsage");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        cattr.setOptimizeAtRemoveCount(numberToInsert);
        cattr.setMaxKeySize(numberToInsert * 2);
        cattr.setMaxPurgatorySize(numberToInsert);
        IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<Integer, DiskTestObject>(cattr);

        // we will reuse these
        int bytes = 1;
        ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        // Add some to the disk
        for (int i = 0; i < elements.length; i++)
        {
            disk.processUpdate(elements[i]);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half of those added
        int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        // verify that the recycle bin has the correct amount.
        assertEquals("The recycle bin should have the number removed.", numberToRemove, disk.getRecyleBinSize());

        // add half as many as we removed. These should all use spots in the recycle bin.
        int numberToAdd = numberToRemove / 2;
        for (int i = 0; i < numberToAdd; i++)
        {
            disk.processUpdate(elements[i]);
        }

        // verify that we used the correct number of spots
        assertEquals("The recycle bin should have the number removed." + disk.getStats(), numberToAdd, disk.getRecyleCount());
    }

    /**
     * Verify that the data size is as expected after a remove and after a put that should use the
     * spots.
     * <p>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testBytesFreeSize() throws IOException, InterruptedException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testBytesFreeSize");
        cattr.setDiskPath("target/test-sandbox/UnitTest");
        IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<Integer, DiskTestObject>(cattr);

        int numberToInsert = 20;
        int bytes = 24;
        ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil.createCacheElementsWithTestObjects(numberToInsert,
            bytes, cattr.getCacheName());

        for (int i = 0; i < elements.length; i++)
        {
            disk.processUpdate(elements[i]);
        }

        Thread.yield();
        Thread.sleep(100);
        Thread.yield();

        // remove half of those added
        int numberToRemove = elements.length / 2;
        for (int i = 0; i < numberToRemove; i++)
        {
            disk.processRemove(elements[i].getKey());
        }

        long expectedSize = DiskTestObjectUtil.totalSize(elements, numberToRemove);
        long resultSize = disk.getBytesFree();

        // System.out.println( "testBytesFreeSize stats " + disk.getStats() );

        assertEquals("Wrong bytes free size" + disk.getStats(), expectedSize, resultSize);

        // add half as many as we removed. These should all use spots in the recycle bin.
        int numberToAdd = numberToRemove / 2;
        for (int i = 0; i < numberToAdd; i++)
        {
            disk.processUpdate(elements[i]);
        }

        long expectedSize2 = DiskTestObjectUtil.totalSize(elements, numberToAdd);
        long resultSize2 = disk.getBytesFree();
        assertEquals("Wrong bytes free size" + disk.getStats(), expectedSize2, resultSize2);
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     * <p>
     *
     * @throws IOException
     */
    public void testRemove_PartialKey() throws IOException
    {
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_PartialKey");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>(cattr);

        disk.processRemoveAll();

        int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            ICacheElement<String, String> element = new CacheElement<String, String>("testRemove_PartialKey", i + ":key", "data:"
                + i);
            element.setElementAttributes(eAttr);
            disk.processUpdate(element);
        }

        // verif each
        for (int i = 0; i < cnt; i++)
        {
            ICacheElement<String, String> element = disk.processGet(i + ":key");
            assertNotNull("Shoulds have received an element.", element);
        }

        // remove each
        for (int i = 0; i < cnt; i++)
        {
            disk.remove(i + ":");
            ICacheElement<String, String> element = disk.processGet(i + ":key");
            assertNull("Should not have received an element.", element);
        }
        // https://issues.apache.org/jira/browse/JCS-67
        assertEquals("Recylenbin should not have more elements than we removed. Check for JCS-67", cnt, disk.getRecyleBinSize());
    }

    /**
     * Verify that group members are removed if we call remove with a group.
     *
     * @throws IOException
     */
    public void testRemove_Group() throws IOException
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_Group");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<GroupAttrName<String>, String> disk = new IndexedDiskCache<GroupAttrName<String>, String>(cattr);

        disk.processRemoveAll();

        String cacheName = "testRemove_Group_Region";
        String groupName = "testRemove_Group";

        int cnt = 25;
        for (int i = 0; i < cnt; i++)
        {
            GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            CacheElement<GroupAttrName<String>, String> element = new CacheElement<GroupAttrName<String>, String>(cacheName,
                groupAttrName, "data:" + i);

            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool(true);
            element.setElementAttributes(eAttr);

            disk.processUpdate(element);
        }

        // verify each
        for (int i = 0; i < cnt; i++)
        {
            GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            ICacheElement<GroupAttrName<String>, String> element = disk.processGet(groupAttrName);
            assertNotNull("Should have received an element.", element);
        }

        // DO WORK
        // remove the group
        disk.remove(getGroupAttrName(cacheName, groupName, null));

        for (int i = 0; i < cnt; i++)
        {
            GroupAttrName<String> groupAttrName = getGroupAttrName(cacheName, groupName, i + ":key");
            ICacheElement<GroupAttrName<String>, String> element = disk.processGet(groupAttrName);

            // VERIFY
            assertNull("Should not have received an element.", element);
        }

    }

    /**
     * Internal method used for group functionality.
     * <p>
     *
     * @param cacheName
     * @param group
     * @param name
     * @return GroupAttrName
     */
    private GroupAttrName<String> getGroupAttrName(String cacheName, String group, String name)
    {
        GroupId gid = new GroupId(cacheName, group);
        return new GroupAttrName<String>(gid, name);
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    public void testUpdate_EventLogging_simple() throws Exception
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testUpdate_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);
        diskCache.processRemoveAll();

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        ICacheElement<String, String> item = new CacheElement<String, String>("region", "key", "value");

        // DO WORK
        diskCache.update(item);

        SleepUtil.sleepAtLeast(200);

        // VERIFY
        assertEquals("Start should have been called.", 1, cacheEventLogger.startICacheEventCalls);
        assertEquals("End should have been called.", 1, cacheEventLogger.endICacheEventCalls);
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    public void testGet_EventLogging_simple() throws Exception
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testGet_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);
        diskCache.processRemoveAll();

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.get("key");

        // VERIFY
        assertEquals("Start should have been called.", 1, cacheEventLogger.startICacheEventCalls);
        assertEquals("End should have been called.", 1, cacheEventLogger.endICacheEventCalls);
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    public void testGetMultiple_EventLogging_simple() throws Exception
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testGetMultiple_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);
        diskCache.processRemoveAll();

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        Set<String> keys = new HashSet<String>();
        keys.add("junk");

        // DO WORK
        diskCache.getMultiple(keys);

        // VERIFY
        // 1 for get multiple and 1 for get.
        assertEquals("Start should have been called.", 2, cacheEventLogger.startICacheEventCalls);
        assertEquals("End should have been called.", 2, cacheEventLogger.endICacheEventCalls);
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    public void testRemove_EventLogging_simple() throws Exception
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveAll_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);
        diskCache.processRemoveAll();

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.remove("key");

        // VERIFY
        assertEquals("Start should have been called.", 1, cacheEventLogger.startICacheEventCalls);
        assertEquals("End should have been called.", 1, cacheEventLogger.endICacheEventCalls);
    }

    /**
     * Verify event log calls.
     * <p>
     *
     * @throws Exception
     */
    public void testRemoveAll_EventLogging_simple() throws Exception
    {
        // SETUP
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveAll_EventLogging_simple");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTestCEL");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);
        diskCache.processRemoveAll();

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        diskCache.setCacheEventLogger(cacheEventLogger);

        // DO WORK
        diskCache.remove("key");

        // VERIFY
        assertEquals("Start should have been called.", 1, cacheEventLogger.startICacheEventCalls);
        assertEquals("End should have been called.", 1, cacheEventLogger.endICacheEventCalls);
    }

    /**
     * Test the basic get matching.
     * <p>
     *
     * @throws Exception
     */
    public void testPutGetMatching_SmallWait() throws Exception
    {
        // SETUP
        int items = 200;

        String cacheName = "testPutGetMatching_SmallWait";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        // DO WORK
        for (int i = 0; i <= items; i++)
        {
            diskCache.update(new CacheElement<String, String>(cacheName, i + ":key", cacheName + " data " + i));
        }
        Thread.sleep(500);

        Map<String, ICacheElement<String, String>> matchingResults = diskCache.getMatching("1.8.+");

        // VERIFY
        assertEquals("Wrong number returned", 10, matchingResults.size());
        // System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        // System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }

    /**
     * Test the basic get matching. With no wait this will all come from purgatory.
     * <p>
     *
     * @throws Exception
     */
    public void testPutGetMatching_NoWait() throws Exception
    {
        // SETUP
        int items = 200;

        String cacheName = "testPutGetMatching_NoWait";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        // DO WORK
        for (int i = 0; i <= items; i++)
        {
            diskCache.update(new CacheElement<String, String>(cacheName, i + ":key", cacheName + " data " + i));
        }

        Map<String, ICacheElement<String, String>> matchingResults = diskCache.getMatching("1.8.+");

        // VERIFY
        assertEquals("Wrong number returned", 10, matchingResults.size());
        // System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
        // System.out.println( "\nAFTER TEST \n" + diskCache.getStats() );
    }

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    public void testUTF8String() throws Exception
    {
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();

        // System.out.println( "The string contains " + string.length() + " characters" );

        String cacheName = "testUTF8String";

        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<String, String>(cacheName, "x", string));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        ICacheElement<String, String> afterElement = diskCache.get("x");
        assertNotNull(afterElement);
        // System.out.println( "afterElement = " + afterElement );
        String after = afterElement.getVal();

        assertNotNull(after);
        assertEquals("wrong string after retrieval", string, after);
    }

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    public void testUTF8ByteArray() throws Exception
    {
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();
        // System.out.println( "The string contains " + string.length() + " characters" );
        String UTF8 = "UTF-8";
        byte[] bytes = string.getBytes(UTF8);

        String cacheName = "testUTF8ByteArray";

        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, byte[]> diskCache = new IndexedDiskCache<String, byte[]>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<String, byte[]>(cacheName, "x", bytes));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        ICacheElement<String, byte[]> afterElement = diskCache.get("x");
        assertNotNull(afterElement);
        // System.out.println( "afterElement = " + afterElement );
        byte[] after = afterElement.getVal();

        assertNotNull(after);
        assertEquals("wrong bytes after retrieval", string, new String(after, UTF8));
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    public void testProcessUpdate_Simple() throws IOException
    {
        // SETUP
        String cacheName = "testProcessUpdate_Simple";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        String key = "myKey";
        String value = "myValue";
        ICacheElement<String, String> ce = new CacheElement<String, String>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce);
        ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull("Should have a result", result);
        long fileSize = diskCache.getDataFileSize();
        assertTrue("File should be greater than 0", fileSize > 0);
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    public void testProcessUpdate_SameKeySameSize() throws IOException
    {
        // SETUP
        String cacheName = "testProcessUpdate_SameKeySameSize";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        String key = "myKey";
        String value = "myValue";
        ICacheElement<String, String> ce1 = new CacheElement<String, String>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        ICacheElement<String, String> ce2 = new CacheElement<String, String>(cacheName, key, value);
        diskCache.processUpdate(ce2);
        ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull("Should have a result", result);
        long fileSize2 = diskCache.getDataFileSize();
        assertEquals("File should be the same", fileSize1, fileSize2);
        int binSize = diskCache.getRecyleBinSize();
        assertEquals("Should be nothing in the bin.", 0, binSize);
    }

    /**
     * Verify the item makes it to disk.
     * <p>
     *
     * @throws IOException
     */
    public void testProcessUpdate_SameKeySmallerSize() throws IOException
    {
        // SETUP
        String cacheName = "testProcessUpdate_SameKeySmallerSize";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        String key = "myKey";
        String value = "myValue";
        String value2 = "myValu";
        ICacheElement<String, String> ce1 = new CacheElement<String, String>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        ICacheElement<String, String> ce2 = new CacheElement<String, String>(cacheName, key, value2);
        diskCache.processUpdate(ce2);
        ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull("Should have a result", result);
        long fileSize2 = diskCache.getDataFileSize();
        assertEquals("File should be the same", fileSize1, fileSize2);
        int binSize = diskCache.getRecyleBinSize();
        assertEquals("Should be nothing in the bin.", 0, binSize);
    }

    /**
     * Verify that the old slot gets in the recycle bin.
     * <p>
     *
     * @throws IOException
     */
    public void testProcessUpdate_SameKeyBiggerSize() throws IOException
    {
        // SETUP
        String cacheName = "testProcessUpdate_SameKeyBiggerSize";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/IndexDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        String key = "myKey";
        String value = "myValue";
        String value2 = "myValue2";
        ICacheElement<String, String> ce1 = new CacheElement<String, String>(cacheName, key, value);

        // DO WORK
        diskCache.processUpdate(ce1);
        long fileSize1 = diskCache.getDataFileSize();

        // DO WORK
        ICacheElement<String, String> ce2 = new CacheElement<String, String>(cacheName, key, value2);
        diskCache.processUpdate(ce2);
        ICacheElement<String, String> result = diskCache.processGet(key);

        // VERIFY
        assertNotNull("Should have a result", result);
        long fileSize2 = diskCache.getDataFileSize();
        assertTrue("File should be greater.", fileSize1 < fileSize2);
        int binSize = diskCache.getRecyleBinSize();
        assertEquals("Should be one in the bin.", 1, binSize);
    }

    public void testLoadFromDisk() throws Exception
    {
        for (int i = 0; i < 15; i++)
        { // usually after 2 time it fails
            oneLoadFromDisk();
        }
    }

    public void oneLoadFromDisk() throws Exception
    {
        // initialize object to be stored
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();

        // initialize cache
        String cacheName = "testLoadFromDisk";
        IndexedDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        IndexedDiskCache<String, String> diskCache = new IndexedDiskCache<String, String>(cattr);

        // DO WORK
        for (int i = 0; i < 50; i++)
        {
            diskCache.update(new CacheElement<String, String>(cacheName, "x" + i, string));
        }
        // Thread.sleep(1000);
        // VERIFY
        diskCache.dispose();
        // Thread.sleep(1000);

        diskCache = new IndexedDiskCache<String, String>(cattr);

        for (int i = 0; i < 50; i++)
        {
            ICacheElement<String, String> afterElement = diskCache.get("x" + i);
            assertNotNull("Missing element from cache. Cache size: " + diskCache.getSize() + " element: x" + i, afterElement);
            assertEquals("wrong string after retrieval", string, afterElement.getVal());
        }
    }
}
