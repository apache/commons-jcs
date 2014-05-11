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

import junit.framework.TestCase;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;

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
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>( cattr );

        disk.processRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement<String, String> element = new CacheElement<String, String>( cattr.getCacheName(), "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.processUpdate( element );
        }

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( "presave, Should have received an element.", element );
            assertEquals( "presave, element is wrong.", "data:" + i, element.getVal() );
        }

        disk.saveKeys();

        disk.loadKeys();

        assertEquals( "The disk is the wrong size.", cnt, disk.getSize() );

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( "postsave, Should have received an element.", element );
            assertEquals( "postsave, element is wrong.", "data:" + i, element.getVal() );
        }

        disk.dump();

    }


    /**
     * Add some elements, remove 1, call optimize, verify that the removed isn't present.
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
        cattr.setCacheName( "testOptimize" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/KeyStoreUnitTest" );
        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>( cattr );

        disk.processRemoveAll();

        int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            ICacheElement<String, String> element = new CacheElement<String, String>( cattr.getCacheName(), "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.processUpdate( element );
        }

        long preAddRemoveSize = disk.getDataFileSize();

        IElementAttributes eAttr = new ElementAttributes();
        eAttr.setIsSpool( true );
        ICacheElement<String, String> elementSetup = new CacheElement<String, String>( cattr.getCacheName(), "key:" + "A", "data:" + "A" );
        elementSetup.setElementAttributes( eAttr );
        disk.processUpdate( elementSetup );

        ICacheElement<String, String> elementRet = disk.processGet( "key:" + "A" );
        assertNotNull( "postsave, Should have received an element.", elementRet );
        assertEquals( "postsave, element is wrong.", "data:" + "A", elementRet.getVal() );

        disk.remove( "key:" + "A" );

        long preSize = disk.getDataFileSize();
        // synchronous versoin
        disk.optimizeFile(); //deoptimizeRealTime();
        long postSize = disk.getDataFileSize();

        assertTrue( "Should be smaller. postsize="+postSize+" preSize="+preSize, postSize < preSize );
        assertEquals( "Should be the same size after optimization as before add and remove.", preAddRemoveSize, postSize );

        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( "postsave, Should have received an element.", element );
            assertEquals( "postsave, element is wrong.", "data:" + i, element.getVal() );
        }
    }
}
