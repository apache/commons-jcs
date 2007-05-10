package org.apache.jcs.auxiliary.disk.block;

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

/**
 * Tests for the keyStore.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskCacheKeyStoreUnitTest
    extends TestCase
{
    /** Directory name */
    private String rootDirName = "target/test-sandbox/block";

    /**
     * Put a bunch of keys inthe key store and verify that they are present.
     * <p>
     * @throws Exception
     */
    public void testPutKeys()
        throws Exception
    {
        // SETUP
        String regionName = "testPutKeys";
        int maxKeys = 1000;
        int bytesPerBlock = 2000;

        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName( regionName );
        attributes.setDiskPath( rootDirName );
        attributes.setMaxKeySize( maxKeys );
        attributes.setBlockSizeBytes( bytesPerBlock );

        BlockDiskCache blockDiskCache = new BlockDiskCache( attributes );

        BlockDiskKeyStore keyStore = new BlockDiskKeyStore( attributes, blockDiskCache );

        // DO WORK
        int numElements = 100;
        for ( int i = 0; i < numElements; i++ )
        {
            keyStore.put( String.valueOf( i ), new int[i] );
        }
        System.out.println( "testPutKeys " + keyStore );

        // VERIFY
        assertEquals( "Wrong number of keys", numElements, keyStore.size() );
        for ( int i = 0; i < numElements; i++ )
        {
            int[] result = keyStore.get( String.valueOf( i ) );
            assertEquals( "Wrong array returned.", i, result.length );
        }
    }

    /**
     * Verify that we can load keys that we saved. Add a bunch. Save them. Clear the memory keyhash.
     * Load the keys. Verify.
     * <p>
     * @throws Exception
     */
    public void testSaveLoadKeys()
        throws Exception
    {
        // SETUP
        String regionName = "testSaveLoadKeys";
        int maxKeys = 10000;
        int bytesPerBlock = 2000;

        BlockDiskCacheAttributes attributes = new BlockDiskCacheAttributes();
        attributes.setCacheName( regionName );
        attributes.setDiskPath( rootDirName );
        attributes.setMaxKeySize( maxKeys );
        attributes.setBlockSizeBytes( bytesPerBlock );

        BlockDiskCache blockDiskCache = new BlockDiskCache( attributes );

        BlockDiskKeyStore keyStore = new BlockDiskKeyStore( attributes, blockDiskCache );

        // DO WORK
        int numElements = 1000;
        //Random random = new Random( 89 );
        for ( int i = 0; i < numElements; i++ )
        {
            int blocks = i;//random.nextInt( 10 );
            keyStore.put( String.valueOf( i ), new int[blocks] );
            keyStore.put( String.valueOf( i ), new int[i] );
        }
        System.out.println( "testSaveLoadKeys " + keyStore );

        // VERIFY
        assertEquals( "Wrong number of keys", numElements, keyStore.size() );

        // DO WORK
        keyStore.saveKeys();
        keyStore.clearMemoryMap();

        // VERIFY
        assertEquals( "Wrong number of keys after clearing memory", 0, keyStore.size() );

        // DO WORK
        keyStore.loadKeys();

        // VERIFY
        assertEquals( "Wrong number of keys after loading", numElements, keyStore.size() );
        for ( int i = 0; i < numElements; i++ )
        {
            int[] result = keyStore.get( String.valueOf( i ) );
            assertEquals( "Wrong array returned.", i, result.length );
        }
    }
}
