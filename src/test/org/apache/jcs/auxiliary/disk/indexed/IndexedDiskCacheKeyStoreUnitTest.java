package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Test store and load keys.
 * 
 * @author Aaron Smuts
 * 
 */
public class IndexedDiskCacheKeyStoreUnitTest
    extends TestCase
{

    /**
     * Add some keys, store them, load them from disk, then check to see that we
     * can get the items.
     * 
     * @throws Exception
     * 
     */
    public void testStoreKeys()
        throws Exception
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testStoreKeys" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/KeyStoreUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testStoreKeys", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( "key:" + i );
            assertNotNull( "presave, Should have recevied an element.", element );
            assertEquals( "presave, element is wrong.", "data:" + i, element.getVal() );
        }

        disk.saveKeys();

        disk.loadKeys();

        assertEquals( "The disk is the wrong size.", cnt, disk.getSize() );

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( "key:" + i );
            assertNotNull( "postsave, Should have recevied an element.", element );
            assertEquals( "postsave, element is wrong.", "data:" + i, element.getVal() );
        }

        disk.dump();
        
    }

    
    /**
     * Add some elements, remove 1, call optiiize, verify that the removed isn't present.
     * 
     * We should also compare the data file sizes. . . .
     * 
     * @throws Exception
     * 
     */
    public void testOptiimize()
        throws Exception
    {
        IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testOptiimize" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/KeyStoreUnitTest" );
        IndexedDiskCache disk = new IndexedDiskCache( cattr );

        disk.doRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement element = new CacheElement( "testOptiimize", "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.doUpdate( element );
        }
        
        long preAddRemoveSize = disk.getDataFileSize();
        
        IElementAttributes eAttr = new ElementAttributes();
        eAttr.setIsSpool( true );
        ICacheElement elementSetup = new CacheElement( "testOptiimize", "key:" + "A", "data:" + "A" );
        elementSetup.setElementAttributes( eAttr );
        disk.doUpdate( elementSetup );
                
        ICacheElement elementRet = disk.doGet( "key:" + "A" );
        assertNotNull( "postsave, Should have recevied an element.", elementRet );
        assertEquals( "postsave, element is wrong.", "data:" + "A", elementRet.getVal() );
        
        disk.remove( "key:" + "A" );

        long preSize = disk.getDataFileSize();
        // synchronous versoin
        disk.optimizeRealTime();
        long postSize = disk.getDataFileSize();
        
        System.out.println( "preAddRemoveSize " + preAddRemoveSize );
        System.out.println( "preSize " + preSize );
        System.out.println( "postSize " + postSize );
        
        assertTrue( "Should be smaller.", postSize < preSize );
        assertEquals( "Should be the same size after optimization as before add and remove.", preAddRemoveSize, postSize );
              
        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = disk.doGet( "key:" + i );
            assertNotNull( "postsave, Should have recevied an element.", element );
            assertEquals( "postsave, element is wrong.", "data:" + i, element.getVal() );
        }
        
        
        
    }
        
        
}
