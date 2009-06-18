package org.apache.jcs.auxiliary.disk.file;

import java.io.File;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.timing.SleepUtil;

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
        FileDiskCache diskCache = new FileDiskCache( cattr );
        File directory = diskCache.getDirectory();

        // VERIFY
        assertNotNull( "Should have a directory", directory );
        assertTrue( "Should have an existing directory", directory.exists() );
        assertTrue( "Directory should include the cache name. " + directory.getAbsolutePath(), directory
            .getAbsolutePath().indexOf( cacheName ) != -1 );
        assertTrue( "Directory should include the disk path. " + directory.getAbsolutePath(), directory
            .getAbsolutePath().indexOf( "DiskFileCacheUnitTest" ) != -1 );
        assertTrue( "Should be alive", diskCache.getStatus() == CacheConstants.STATUS_ALIVE );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        // DO WORK
        diskCache.dispose();

        // VERIFY
        assertTrue( "Should not be alive", diskCache.getStatus() == CacheConstants.STATUS_DISPOSED );
    }

    /**
     * Verify initialization.
     * <p>
     * @throws Exception
     */
    public void testInitialization_JunkFileName()
        throws Exception
    {
        // SETUP
        String cacheName = "testInitialization_JunkFileName";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/DiskFileCacheUnitTest%$&*#@" );

        // DO WORK
        FileDiskCache diskCache = new FileDiskCache( cattr );
        File directory = diskCache.getDirectory();

        // VERIFY
        assertNotNull( "Should have a directory", directory );
        assertFalse( "Should have an existing directory", directory.exists() );
        assertTrue( "Should not be alive", diskCache.getStatus() == CacheConstants.STATUS_DISPOSED );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        diskCache.removeAll();
        diskCache.update( new CacheElement( cacheName, "key1", "Data" ) );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        diskCache.update( new CacheElement( cacheName, "key1", "Data" ) );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        // DO WORK
        ICacheElement result = diskCache.get( "key" );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        diskCache.update( new CacheElement( cacheName, "key1", "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement result = diskCache.get( "key1" );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement result = diskCache.get( "key0" );

        // VERIFY
        assertNotNull( "Should NOT be null.", result );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        for ( int i = 0; i <= maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement result = diskCache.get( "key0" );

        // VERIFY
        assertNull( "Should be null.", result );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );
        diskCache.removeAll();

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        for ( int i = maxNumberOfFiles - 1; i >= 0; i-- )
        {
            SleepUtil.sleepAtLeast( 5 );
            diskCache.get( "key" + i );
        }
        SleepUtil.sleepAtLeast( 100 );
        
        // This will push it over.  number 9, the youngest, but LRU item should be removed
        diskCache.update( new CacheElement( cacheName, "key" + maxNumberOfFiles, "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement result = diskCache.get( "key9" );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );
        diskCache.removeAll();

        for ( int i = 0; i < maxNumberOfFiles; i++ )
        {
            diskCache.update( new CacheElement( cacheName, "key" + i, "Data" ) );
        }
        SleepUtil.sleepAtLeast( 100 );

        for ( int i = maxNumberOfFiles - 1; i >= 0; i-- )
        {
            SleepUtil.sleepAtLeast( 5 );
            diskCache.get( "key" + i );
        }
        SleepUtil.sleepAtLeast( 100 );
        
        // This will push it over.  number 0, the oldest should be removed
        diskCache.update( new CacheElement( cacheName, "key" + maxNumberOfFiles, "Data" ) );
        SleepUtil.sleepAtLeast( 100 );

        // DO WORK
        ICacheElement result = diskCache.get( "key0" );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        diskCache.update( new CacheElement( cacheName, "key1", "Data" ) );
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
        FileDiskCache diskCache = new FileDiskCache( cattr );

        String string = "This is my big string ABCDEFGH";
        StringBuffer sb = new StringBuffer();
        sb.append( string );
        for ( int i = 0; i < 4; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        string = sb.toString();

        // DO WORK
        diskCache.update( new CacheElement( cacheName, "x", string ) );
        SleepUtil.sleepAtLeast( 300 );

        // VERIFY
        ICacheElement afterElement = diskCache.get( "x" );
        assertNotNull( afterElement );
        System.out.println( "afterElement = " + afterElement );
        String after = (String) afterElement.getVal();

        assertNotNull( after );
        assertEquals( "wrong string after retrieval", string, after );
    }
}
