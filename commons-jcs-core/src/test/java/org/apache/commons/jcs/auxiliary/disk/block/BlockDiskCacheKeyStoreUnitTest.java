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

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;

/**
 * Tests for the keyStore.
 * <p>
 *
 * @author Aaron Smuts
 */
public class BlockDiskCacheKeyStoreUnitTest
        extends TestCase
{
    /** Directory name */
    private final String rootDirName = "target/test-sandbox/block";

    /**
     * Put a bunch of keys in the key store and verify that they are present.
     * <p>
     *
     * @throws Exception
     */
    public void testPutKeys()
            throws Exception
    {
        // SETUP
        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName("testPutKeys");
        attributes.setDiskPath(rootDirName);
        attributes.setMaxKeySize(1000);
        attributes.setBlockSizeBytes(2000);

        innerTestPutKeys(attributes);
    }

    public void testPutKeysSize()
            throws Exception
    {
        // SETUP
        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName("testPutKeys");
        attributes.setDiskPath(rootDirName);
        attributes.setMaxKeySize(100000);
        attributes.setBlockSizeBytes(1024);
        attributes.setDiskLimitType(DiskLimitType.SIZE);

        innerTestPutKeys(attributes);
    }

    private void innerTestPutKeys(BlockDiskCacheAttributes attributes)
    {
        BlockDiskCache<String, String> blockDiskCache = new BlockDiskCache<String, String>(attributes);
        BlockDiskKeyStore<String> keyStore = new BlockDiskKeyStore<String>(attributes, blockDiskCache);

        // DO WORK
        int numElements = 100;
        for (int i = 0; i < numElements; i++)
        {
            keyStore.put(String.valueOf(i), new int[i]);
        }
        // System.out.println( "testPutKeys " + keyStore );

        // VERIFY
        assertEquals("Wrong number of keys", numElements, keyStore.size());
        for (int i = 0; i < numElements; i++)
        {
            int[] result = keyStore.get(String.valueOf(i));
            assertEquals("Wrong array returned.", i, result.length);
        }
    }

    /**
     * Verify that we can load keys that we saved. Add a bunch. Save them. Clear
     * the memory key hash. Load the keys. Verify.
     * <p>
     *
     * @throws Exception
     */
    public void testSaveLoadKeys()
            throws Exception
    {
        // SETUP
        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName("testSaveLoadKeys");
        attributes.setDiskPath(rootDirName);
        attributes.setMaxKeySize(10000);
        attributes.setBlockSizeBytes(2000);

        testSaveLoadKeysInner(attributes);
    }

    public void testSaveLoadKeysSize()
            throws Exception
    {
        // SETUP
        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName("testSaveLoadKeys");
        attributes.setDiskPath(rootDirName);
        attributes.setMaxKeySize(10000);
        attributes.setBlockSizeBytes(2000);

        testSaveLoadKeysInner(attributes);
    }

    private void testSaveLoadKeysInner(BlockDiskCacheAttributes attributes)
    {
        BlockDiskKeyStore<String> keyStore = new BlockDiskKeyStore<String>(attributes, null);

        // DO WORK
        int numElements = 1000;
        int blockIndex = 0;
        // Random random = new Random( 89 );
        for (int i = 0; i < numElements; i++)
        {
            int blocks = i;// random.nextInt( 10 );

            // fill with reasonable data to make verify() happy
            int[] block1 = new int[blocks];
            int[] block2 = new int[blocks];
            for (int j = 0; j < blocks; j++)
            {
                block1[j] = blockIndex++;
                block2[j] = blockIndex++;
            }
            keyStore.put(String.valueOf(i), block1);
            keyStore.put(String.valueOf(i), block2);
        }
        // System.out.println( "testSaveLoadKeys " + keyStore );

        // VERIFY
        assertEquals("Wrong number of keys", numElements, keyStore.size());

        // DO WORK
        keyStore.saveKeys();
        keyStore.clearMemoryMap();

        // VERIFY
        assertEquals("Wrong number of keys after clearing memory", 0, keyStore.size());

        // DO WORK
        keyStore.loadKeys();

        // VERIFY
        assertEquals("Wrong number of keys after loading", numElements, keyStore.size());
        for (int i = 0; i < numElements; i++)
        {
            int[] result = keyStore.get(String.valueOf(i));
            assertEquals("Wrong array returned.", i, result.length);
        }
    }

    public void testObjectLargerThanMaxSize()
    {
        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName("testObjectLargerThanMaxSize");
        attributes.setDiskPath(rootDirName);
        attributes.setMaxKeySize(1000);
        attributes.setBlockSizeBytes(2000);
        attributes.setDiskLimitType(DiskLimitType.SIZE);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        BlockDiskKeyStore<String> keyStore = new BlockDiskKeyStore<String>(attributes, new BlockDiskCache(attributes));

        keyStore.put("1", new int[1000]);
        keyStore.put("2", new int[1000]);
        assertNull(keyStore.get("1"));
        assertNotNull(keyStore.get("2"));
    }
}
