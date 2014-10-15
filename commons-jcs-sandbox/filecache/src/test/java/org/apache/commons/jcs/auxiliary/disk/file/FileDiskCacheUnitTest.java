package org.apache.commons.jcs.auxiliary.disk.file;

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
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.utils.timing.SleepUtil;

import java.io.File;

/** Unit tests for the disk file cache. */
public class FileDiskCacheUnitTest
    extends TestCase
{
    /**
     * Verify initialization.
     * <p>
     * @throws Exception
     */
    public void testInitialization_Normal()
        throws Exception
    {
        // SETUP
        String cacheName = "testInitialization_Normal";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );

        // DO WORK
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );
        File directory = diskCache.getDirectory();

        // VERIFY
        assertNotNull( "Should have a directory", directory );
        assertTrue( "Should have an existing directory", directory.exists() );
        assertTrue( "Directory should include the cache name. " + directory.getAbsolutePath(), directory
            .getAbsolutePath().indexOf( cacheName ) != -1 );
        assertTrue( "Directory should include the disk path. " + directory.getAbsolutePath(), directory
            .getAbsolutePath().indexOf( "DiskFileCacheUnitTest" ) != -1 );
        assertTrue( "Should be alive", diskCache.getStatus() == CacheStatus.ALIVE );
    }

    /**
     * Verify dispose.
     * <p>
     * @throws Exception
     */
    public void testDispose_Normal()
        throws Exception
    {
        // SETUP
        String cacheName = "testDispose_Normal";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        // DO WORK
        diskCache.dispose();

        // VERIFY
        assertTrue( "Should not be alive", diskCache.getStatus() == CacheStatus.DISPOSED );
    }

    /**
     * Verify initialization.
     * <p>
     * @throws Exception
     *
     * tv: Don't know why this is supposed to fail. Under MacOSX this directory name works fine.
     */
    public void OFFtestInitialization_JunkFileName()
        throws Exception
    {
        // SETUP
        String cacheName = "testInitialization_JunkFileName";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest%$&*#@" );

        // DO WORK
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );
        File directory = diskCache.getDirectory();

        // VERIFY
        assertNotNull( "Should have a directory", directory );
        assertFalse( "Should not have an existing directory", directory.exists() );
        assertTrue( "Should not be alive", diskCache.getStatus() == CacheStatus.DISPOSED );
    }

    /**
     * Verify getSize.
     * <p>
     * @throws Exception
     */
    public void testGetSize_Empty()
        throws Exception
    {
        // SETUP
        String cacheName = "testGetSize_Empty";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        diskCache.removeAll();

        // DO WORK
        int result = diskCache.getSize();

        // VERIFY
        assertEquals( "Should be empty.", 0, result );
    }

    /**
     * Verify getSize.
     * <p>
     * @throws Exception
     */
    public void testGetSize_OneItem()
        throws Exception
    {
        // SETUP
        String cacheName = "testGetSize_OneItem";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        diskCache.removeAll();
        diskCache.update( new CacheElement<String, String>( cacheName, "key1", "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        int result = diskCache.getSize();

        // VERIFY
        assertEquals( "Should not be empty.", 1, result );
    }

    /**
     * Verify remove all.
     * <p>
     * @throws Exception
     */
    public void testRemoveAll_OneItem()
        throws Exception
    {
        // SETUP
        String cacheName = "testRemoveAll_OneItem";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        diskCache.update( new CacheElement<String, String>( cacheName, "key1", "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        diskCache.removeAll();
        SleepUtil.sleepAtLeast( 100 );
        int result = diskCache.getSize();

        // VERIFY
        assertEquals( "Should be empty.", 0, result );
    }

    /**
     * Verify get.
     * <p>
     * @throws Exception
     */
    public void testGet_Empty()
        throws Exception
    {
        // SETUP
        String cacheName = "testGet_Empty";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        // DO WORK
        ICacheElement<String, String> result = diskCache.get( "key" );

        // VERIFY
        assertNull( "Should be null.", result );
    }

    /**
     * Verify get.
     * <p>
     * @throws Exception
     */
    public void testGet_Exists()
        throws Exception
    {
        // SETUP
        String cacheName = "testGet_Empty";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        diskCache.update( new CacheElement<String, String>( cacheName, "key1", "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement<String, String> result = diskCache.get( "key1" );

        // VERIFY
        assertNotNull( "Should NOT be null.", result );
    }

    /**
     * Verify RemoveIfLimitIsSetAndReached.
     * <p>
     * @throws Exception
     */
    public void testRemoveIfLimitIsSetAndReached_NotReached()
        throws Exception
    {
        // SETUP
        int maxNumberOfFiles = 10;
        String cacheName = "testRemoveIfLimitIsSetAndReached_NotReached";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        cattr.setMaxNumberOfFiles( maxNumberOfFiles );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement<String, String>( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        int stillCached = 0;
        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            final String key = "key" + i;
            ICacheElement<String, String> result = diskCache.get( key );
//            System.out.println("Entry "+ key+ " null? " + (result==null));
            if (result != null) {
                stillCached++;
            }
        }

        // VERIFY
        assertEquals("All files should still be cached", maxNumberOfFiles, stillCached);
    }

    /**
     * Verify RemoveIfLimitIsSetAndReached.
     * <p>
     * @throws Exception
     */
    public void testRemoveIfLimitIsSetAndReached_Reached()
        throws Exception
    {
        // SETUP
        int maxNumberOfFiles = 10;
        String cacheName = "testRemoveIfLimitIsSetAndReached_Reached";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        cattr.setMaxNumberOfFiles( maxNumberOfFiles );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        for ( int i = 0; i <= maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement<String, String>( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 500 );

        // DO WORK
        int stillCached = 0;
        for ( int i = 0; i <= maxNumberOfFiles; i++ )
        {
            final String key = "key" + i;
            ICacheElement<String, String> result = diskCache.get( key );
//            System.out.println("Entry "+ key+ " null? " + (result==null));
            if (result != null) {
                stillCached++;
            }
        }

        // VERIFY
        assertEquals(maxNumberOfFiles, stillCached);
    }

    /**
     * Verify RemoveIfLimitIsSetAndReached. Since touch on get is true, the LRU and not the oldest
     * shoudl be removed.
     * <p>
     * @throws Exception
     */
    public void testRemoveIfLimitIsSetAndReached_Reached_TouchTrue()
        throws Exception
    {
        // SETUP
        int maxNumberOfFiles = 10;
        String cacheName = "testRemoveIfLimitIsSetAndReached_Reached_TouchTrue";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        cattr.setMaxNumberOfFiles( maxNumberOfFiles );
        cattr.setTouchOnGet( true );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );
        diskCache.removeAll();

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement<String, String>( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        for ( int i = maxNumberOfFiles - 1; i >= 0; i-- )
        {
            // tv: The Mac file system has 1 sec resolution, so this is the minimum value
            // to make this test work.
            SleepUtil.sleepAtLeast( 501 );
            ICacheElement<String, String> ice = diskCache.get( "key" + i );
            assertNotNull("Value of key" + i + " should not be null", ice);
        }

        SleepUtil.sleepAtLeast( 100 );

        // This will push it over.  number 9, the youngest, but LRU item should be removed
        diskCache.update( new CacheElement<String, String>( cacheName, "key" + maxNumberOfFiles, "Data" ) );
        SleepUtil.sleepAtLeast( 501 );

        // DO WORK
        ICacheElement<String, String> result = diskCache.get( "key9" );

        // VERIFY
        assertNull( "Should be null.", result );
    }

    /**
     * Verify RemoveIfLimitIsSetAndReached. Since touch on get is false, the the oldest
     * should be removed.
     * <p>
     * @throws Exception
     */
    public void testRemoveIfLimitIsSetAndReached_Reached_TouchFalse()
        throws Exception
    {
        // SETUP
        int maxNumberOfFiles = 10;
        String cacheName = "testRemoveIfLimitIsSetAndReached_Reached_TouchTrue";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        cattr.setMaxNumberOfFiles( maxNumberOfFiles );
        cattr.setTouchOnGet( false );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );
        diskCache.removeAll();

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement<String, String>( cacheName, "key" + i, "Data" ) );
            if (i == 0 ){
                SleepUtil.sleepAtLeast( 1001 ); // ensure the first file is seen as older
            }
        }
        SleepUtil.sleepAtLeast( 500 );

        for ( int i = maxNumberOfFiles - 1; i >= 0; i-- )
        {
            SleepUtil.sleepAtLeast( 5 );
            diskCache.get( "key" + i );
        }
        SleepUtil.sleepAtLeast( 100 );

        // This will push it over.  number 0, the oldest should be removed
        diskCache.update( new CacheElement<String, String>( cacheName, "key" + maxNumberOfFiles, "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement<String, String> result = diskCache.get( "key0" );

        // VERIFY
        assertNull( "Should be null.", result );
    }

    /**
     * Verify file.
     * <p>
     * @throws Exception
     */
    public void testFile_NoSPecialCharacters()
        throws Exception
    {
        // SETUP
        String cacheName = "testFile";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        String key = "simplestring";

        // DO WORK
        File result = diskCache.file( key );

        // VERIFY
        assertEquals( "Wrong string.", key, result.getName() );
    }

    /**
     * Verify file.
     * <p>
     * @throws Exception
     */
    public void testFile_Space()
        throws Exception
    {
        // SETUP
        String cacheName = "testFile";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        String key = "simple string";

        // DO WORK
        File result = diskCache.file( key );

        // VERIFY
        assertEquals( "Wrong string.", "simple_string", result.getName() );
    }

    /**
     * Verify file.
     * <p>
     * @throws Exception
     */
    public void testFile_SpecialCharacter()
        throws Exception
    {
        // SETUP
        String cacheName = "testFile";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        String key = "simple%string";

        // DO WORK
        File result = diskCache.file( key );

        // VERIFY
        assertEquals( "Wrong string.", "simple_string", result.getName() );
    }

    /**
     * Verify idempotence.
     * <p>
     * @throws Exception
     */
    public void testFile_WithFile()
        throws Exception
    {
        // SETUP
        String cacheName = "testFile";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        String key = "simple%string";
        File firstResult = diskCache.file( key );

        // DO WORK
        File result = diskCache.file( firstResult.getName() );

        // VERIFY
        assertEquals( "Wrong string.", "simple_string", result.getName() );
    }

    /**
     * Verify remove.
     * <p>
     * @throws Exception
     */
    public void testRemove_OneItem()
        throws Exception
    {
        // SETUP
        String cacheName = "testRemove_OneItem";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        diskCache.update( new CacheElement<String, String>( cacheName, "key1", "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        diskCache.remove( "key1" );
        SleepUtil.sleepAtLeast( 100 );
        int result = diskCache.getSize();

        // VERIFY
        assertEquals( "Should be empty.", 0, result );
    }

    /**
     * Verify that the disk file cache can handle a big string.
     * <p>
     * @throws Exception
     */
    public void testPutGet_BigString()
        throws Exception
    {
        // SETUP
        String cacheName = "testPutGet_BigString";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest" );
        FileDiskCache<String, String> diskCache = new FileDiskCache<String, String>( cattr );

        String string = "This is my big string ABCDEFGH";
        StringBuilder sb = new StringBuilder();
        sb.append( string );
        for ( int i = 0; i < 4; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        string = sb.toString();

        // DO WORK
        diskCache.update( new CacheElement<String, String>( cacheName, "x", string ) );
        SleepUtil.sleepAtLeast( 300 );

        // VERIFY
        ICacheElement<String, String> afterElement = diskCache.get( "x" );
        assertNotNull( afterElement );
        String after = afterElement.getVal();

        assertNotNull( "afterElement = " + afterElement, after );
        assertEquals( "wrong string after retrieval", string, after );
    }
}
