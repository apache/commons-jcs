package org.apache.commons.jcs.auxiliary.disk.block;

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

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

/** Unit tests for the Block Disk Cache */
public abstract class BlockDiskCacheUnitTestAbstract extends TestCase
{
    public abstract BlockDiskCacheAttributes getCacheAttributes();

    public void testPutGetMatching_SmallWait() throws Exception
    {
        // SETUP
        int items = 200;

        String cacheName = "testPutGetMatching_SmallWait";
        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, String> diskCache = new BlockDiskCache<String, String>(cattr);

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
        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, String> diskCache = new BlockDiskCache<String, String>(cattr);

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
     * Verify that the block disk cache can handle a big string.
     * <p>
     *
     * @throws Exception
     */
    public void testChunk_BigString() throws Exception
    {
        String string = "This is my big string ABCDEFGH";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append("|" + i + ":" + sb.toString()); // big string
        }
        string = sb.toString();

        StandardSerializer elementSerializer = new StandardSerializer();
        byte[] data = elementSerializer.serialize(string);

        File file = new File("target/test-sandbox/BlockDiskCacheUnitTest/testChunk_BigString.data");

        BlockDisk blockDisk = new BlockDisk(file, 200, elementSerializer);

        int numBlocksNeeded = blockDisk.calculateTheNumberOfBlocksNeeded(data);
        // System.out.println( numBlocksNeeded );

        // get the individual sub arrays.
        byte[][] chunks = blockDisk.getBlockChunks(data, numBlocksNeeded);

        byte[] resultData = new byte[0];

        for (short i = 0; i < chunks.length; i++)
        {
            byte[] chunk = chunks[i];
            byte[] newTotal = new byte[data.length + chunk.length];
            // copy data into the new array
            System.arraycopy(data, 0, newTotal, 0, data.length);
            // copy the chunk into the new array
            System.arraycopy(chunk, 0, newTotal, data.length, chunk.length);
            // swap the new and old.
            resultData = newTotal;
        }

        Serializable result = elementSerializer.deSerialize(resultData, null);
        // System.out.println( result );
        assertEquals("wrong string after retrieval", string, result);
    }

    /**
     * Verify that the block disk cache can handle a big string.
     * <p>
     *
     * @throws Exception
     */
    public void testPutGet_BigString() throws Exception
    {
        String string = "This is my big string ABCDEFGH";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(" " + i + sb.toString()); // big string
        }
        string = sb.toString();

        String cacheName = "testPutGet_BigString";

        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, String> diskCache = new BlockDiskCache<String, String>(cattr);

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

        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, String> diskCache = new BlockDiskCache<String, String>(cattr);

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

        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(200);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, byte[]> diskCache = new BlockDiskCache<String, byte[]>(cattr);

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
        assertEquals("wrong bytes after retrieval", bytes.length, after.length);
        // assertEquals( "wrong bytes after retrieval", bytes, after );
        // assertEquals( "wrong bytes after retrieval", string, new String( after, UTF8 ) );

    }

    /**
     * Verify that the block disk cache can handle utf encoded strings.
     * <p>
     *
     * @throws Exception
     */
    public void testUTF8StringAndBytes() throws Exception
    {
        X before = new X();
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
        before.string = string;
        before.bytes = string.getBytes(UTF8);

        String cacheName = "testUTF8StringAndBytes";

        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(500);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, X> diskCache = new BlockDiskCache<String, X>(cattr);

        // DO WORK
        diskCache.update(new CacheElement<String, X>(cacheName, "x", before));

        // VERIFY
        assertNotNull(diskCache.get("x"));
        Thread.sleep(1000);
        ICacheElement<String, X> afterElement = diskCache.get("x");
        // System.out.println( "afterElement = " + afterElement );
        X after = (afterElement.getVal());

        assertNotNull(after);
        assertEquals("wrong string after retrieval", string, after.string);
        assertEquals("wrong bytes after retrieval", string, new String(after.bytes, UTF8));

    }

    public void testLoadFromDisk() throws Exception
    {
        for (int i = 0; i < 20; i++)
        { // usually after 2 time it fails
            oneLoadFromDisk();
        }
    }

    public void oneLoadFromDisk() throws Exception
    {
        // initialize object to be stored
        X before = new X();
        String string = "IÒtÎrn‚tiÙn‡lizÊti¯n";
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < 4; i++)
        {
            sb.append(sb.toString()); // big string
        }
        string = sb.toString();
        String UTF8 = "UTF-8";
        before.string = string;
        before.bytes = string.getBytes(UTF8);

        // initialize cache
        String cacheName = "testLoadFromDisk";
        BlockDiskCacheAttributes cattr = getCacheAttributes();
        cattr.setCacheName(cacheName);
        cattr.setMaxKeySize(100);
        cattr.setBlockSizeBytes(500);
        cattr.setDiskPath("target/test-sandbox/BlockDiskCacheUnitTest");
        BlockDiskCache<String, X> diskCache = new BlockDiskCache<String, X>(cattr);

        // DO WORK
        for (int i = 0; i < 50; i++)
        {
            diskCache.update(new CacheElement<String, X>(cacheName, "x" + i, before));
        }
        diskCache.dispose();

        // VERIFY
        diskCache = new BlockDiskCache<String, X>(cattr);

        for (int i = 0; i < 50; i++)
        {
            ICacheElement<String, X> afterElement = diskCache.get("x" + i);
            assertNotNull("Missing element from cache. Cache size: " + diskCache.getSize() + " element: x" + i, afterElement);
            X after = (afterElement.getVal());

            assertNotNull(after);
            assertEquals("wrong string after retrieval", string, after.string);
            assertEquals("wrong bytes after retrieval", string, new String(after.bytes, UTF8));
        }

        diskCache.dispose();
    }

    /** Holder for a string and byte array. */
    static class X implements Serializable
    {
        /** ignore */
        private static final long serialVersionUID = 1L;

        /** Test string */
        String string;

        /*** test byte array. */
        byte[] bytes;
    }
}
