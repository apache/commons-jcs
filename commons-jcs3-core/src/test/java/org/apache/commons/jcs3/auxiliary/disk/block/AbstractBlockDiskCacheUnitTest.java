package org.apache.commons.jcs3.auxiliary.disk.block;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.control.group.GroupId;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.junit.jupiter.api.Test;

/** Tests for the Block Disk Cache */
public abstract class AbstractBlockDiskCacheUnitTest{
    /** Holder for a string and byte array. */
    static class X implements Serializable
    {
        /** Ignore */
        private static final long serialVersionUID = 1L;

        /** Test string */
        String string;

        /** test byte array. */
        byte[] bytes;
    }

    public abstract BlockDiskCacheAttributes getCacheAttributes();

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
        final X before = new X();
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();
        before.string = string;
        before.bytes = string.getBytes(StandardCharsets.UTF_8);

        // initialize cache
        final String cacheName = "testLoadFromDisk";
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(500);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, X> diskCache = new BlockDiskCache<>(cattr);

        // DO WORK
        for (int i = 0; i < 50; i++)
        {
            diskCache.update(new CacheElement<>(cacheName, "x" + i, before));
        }
        diskCache.dispose();

        // VERIFY
        diskCache = new BlockDiskCache<>(cattr);

        for (int i = 0; i < 50; i++)
        {
            final ICacheElement<String, X> afterElement = diskCache.get("x" + i);
            assertNotNull( afterElement,
                           "Missing element from cache. Cache size: " + diskCache.getSize() + " element: x" + i );
            final X after = afterElement.getVal();

            assertNotNull(after);
            assertEquals( string, after.string, "wrong string after retrieval" );
            assertEquals( string, new String( after.bytes, StandardCharsets.UTF_8 ), "wrong bytes after retrieval" );
        }

        diskCache.dispose();
    }

    @Test
    public void testAppendToDisk() throws Exception
    {
        final String cacheName = "testAppendToDisk";
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(500);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, X> diskCache = new BlockDiskCache<>(cattr);
        diskCache.removeAll();
        final X value1 = new X();
        value1.string = "1234567890";
        final X value2 = new X();
        value2.string = "0987654321";
        diskCache.update(new CacheElement<>(cacheName, "1", value1));
        diskCache.dispose();
        diskCache = new BlockDiskCache<>(cattr);
        diskCache.update(new CacheElement<>(cacheName, "2", value2));
        diskCache.dispose();
        diskCache = new BlockDiskCache<>(cattr);
        assertTrue(diskCache.verifyDisk());
        assertEquals(2, diskCache.getKeySet().size());
        assertEquals(value1.string, diskCache.get("1").getVal().string);
        assertEquals(value2.string, diskCache.get("2").getVal().string);
    }

    /**
     * Verify that the block disk cache can handle a big string.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testChunk_BigString() throws Exception
    {
        String string = "This is my big string ABCDEFGH";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append("|" + i + ":" + sb.toString()); // big string
        }
        string = sb.toString();

        final StandardSerializer elementSerializer = new StandardSerializer();
        final byte[] data = elementSerializer.serialize(string);

        final File file = new File("target/test-sandbox/BlockDiskCacheUnitTest/testChunk_BigString.data");

        final BlockDisk blockDisk = new BlockDisk(file, 200, elementSerializer);

        final int numBlocksNeeded = blockDisk.calculateTheNumberOfBlocksNeeded(data);
        // System.out.println( numBlocksNeeded );

        // get the individual sub arrays.
        final byte[][] chunks = blockDisk.getBlockChunks(data, numBlocksNeeded);

        byte[] resultData = {};

        for (short i = 0; i < chunks.length; i++)
        {
            final byte[] chunk = chunks[i];
            final byte[] newTotal = new byte[data.length + chunk.length];
            // copy data into the new array
            System.arraycopy(data, 0, newTotal, 0, data.length);
            // copy the chunk into the new array
            System.arraycopy(chunk, 0, newTotal, data.length, chunk.length);
            // swap the new and old.
            resultData = newTotal;
        }

        final Serializable result = elementSerializer.deSerialize(resultData, null);
        // System.out.println( result );
        assertEquals( string, result, "wrong string after retrieval" );
        blockDisk.close();
    }

    @Test
    public void testLoadFromDisk() throws Exception
    {
        for (int i = 0; i < 20; i++)
        { // usually after 2 time it fails
            oneLoadFromDisk();
        }
    }

    /**
     * Verify that the block disk cache can handle a big string.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testPutGet_BigString() throws Exception
    {
        String string = "This is my big string ABCDEFGH";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(" " + i + sb.toString()); // big string
        }
        string = sb.toString();

        final String cacheName = "testPutGet_BigString";

        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> diskCache = new BlockDiskCache<>(cattr);

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
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> diskCache = new BlockDiskCache<>(cattr);

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

    @Test
    public void testPutGetMatching_SmallWait() throws Exception
    {
        // SETUP
        final int items = 200;

        final String cacheName = "testPutGetMatching_SmallWait";
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> diskCache = new BlockDiskCache<>(cattr);

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
     * Verify that group members are removed if we call remove with a group.
     *
     * @throws IOException
     */
    @Test
    public void testRemove_Group() throws IOException
    {
        // SETUP
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_Group");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<GroupAttrName<String>, String> disk = new BlockDiskCache<>(cattr);

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
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemove_PartialKey");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> disk = new BlockDiskCache<>(cattr);

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

        // verify each
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
    }

    /**
     * Add some items to the disk cache and then remove them one by one.
     *
     * @throws IOException
     */
    @Test
    public void testRemoveItems() throws IOException
    {
        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName("testRemoveItems");
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> disk = new BlockDiskCache<>(cattr);

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

        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, byte[]> diskCache = new BlockDiskCache<>(cattr);

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
        assertEquals( bytes.length, after.length, "wrong bytes after retrieval" );
        // assertEquals( "wrong bytes after retrieval", bytes, after );
        // assertEquals( "wrong bytes after retrieval", string, new String( after, StandardCharsets.UTF_8 ) );

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

        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, String> diskCache = new BlockDiskCache<>(cattr);

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

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testUTF8StringAndBytes() throws Exception
    {
        final X before = new X();
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();
        // System.out.println( "The string contains " + string.length() + " characters" );
        before.string = string;
        before.bytes = string.getBytes(StandardCharsets.UTF_8);

        final String cacheName = "testUTF8StringAndBytes";

        final BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(500);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        final BlockDiskCache<String, X> diskCache = new BlockDiskCache<>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<>(cacheName, "x", before));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        final ICacheElement<String, X> afterElement = diskCache.get("x");
        // System.out.println( "afterElement = " + afterElement );
        final X after = afterElement.getVal();

        assertNotNull(after);
        assertEquals( string, after.string, "wrong string after retrieval" );
        assertEquals( string, new String( after.bytes, StandardCharsets.UTF_8 ), "wrong bytes after retrieval" );

    }
}
