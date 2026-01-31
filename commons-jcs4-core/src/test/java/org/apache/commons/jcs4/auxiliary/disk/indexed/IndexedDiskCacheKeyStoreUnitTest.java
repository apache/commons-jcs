package org.apache.commons.jcs4.auxiliary.disk.indexed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.junit.jupiter.api.Test;

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

/**
 * Test store and load keys.
 */
class IndexedDiskCacheKeyStoreUnitTest
{

    /**
     * Add some elements, remove 1, call optimize, verify that the removed isn't present.
     *
     * We should also compare the data file sizes.
     *
     * @throws Exception
     */
    @Test
    void testOptiimize()
        throws Exception
    {
        final IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testOptimize" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/KeyStoreUnitTest" );
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>( cattr );

        disk.processRemoveAll();

        final int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            final ICacheElement<String, String> element = new CacheElement<>( cattr.getCacheName(), "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.processUpdate( element );
        }

        final long preAddRemoveSize = disk.getDataFileSize();

        final IElementAttributes eAttr = new ElementAttributes();
        eAttr.setIsSpool( true );
        final ICacheElement<String, String> elementSetup = new CacheElement<>( cattr.getCacheName(), "key:A", "data:A" );
        elementSetup.setElementAttributes( eAttr );
        disk.processUpdate( elementSetup );

        final ICacheElement<String, String> elementRet = disk.processGet( "key:A" );
        assertNotNull( elementRet, "postsave, Should have received an element." );
        assertEquals( "data:A", elementRet.getVal(), "postsave, element is wrong." );

        disk.remove( "key:A" );

        final long preSize = disk.getDataFileSize();
        // synchronous versoin
        disk.optimizeFile(); //deoptimizeRealTime();
        final long postSize = disk.getDataFileSize();

        assertTrue( postSize < preSize, "Should be smaller. postsize=" + postSize + " preSize=" + preSize );
        assertEquals( preAddRemoveSize, postSize,
                      "Should be the same size after optimization as before add and remove." );

        for ( int i = 0; i < cnt; i++ )
        {
            final ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( element, "postsave, Should have received an element." );
            assertEquals( "data:" + i, element.getVal(), "postsave, element is wrong." );
        }
    }

    /**
     * Add some keys, store them, load them from disk, then check to see that we
     * can get the items.
     *
     * @throws Exception
     */
    @Test
    void testStoreKeys()
        throws Exception
    {
        final IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testStoreKeys" );
        cattr.setMaxKeySize( 100 );
        cattr.setDiskPath( "target/test-sandbox/KeyStoreUnitTest" );
        final IndexedDiskCache<String, String> disk = new IndexedDiskCache<>( cattr );

        disk.processRemoveAll();

        final int cnt = 25;
        for ( int i = 0; i < cnt; i++ )
        {
            final IElementAttributes eAttr = new ElementAttributes();
            eAttr.setIsSpool( true );
            final ICacheElement<String, String> element = new CacheElement<>( cattr.getCacheName(), "key:" + i, "data:" + i );
            element.setElementAttributes( eAttr );
            disk.processUpdate( element );
        }

        for ( int i = 0; i < cnt; i++ )
        {
            final ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( element, "presave, Should have received an element." );
            assertEquals( "data:" + i, element.getVal(), "presave, element is wrong." );
        }

        disk.saveKeys();

        disk.loadKeys();

        assertEquals( cnt, disk.getSize(), "The disk is the wrong size." );

        for ( int i = 0; i < cnt; i++ )
        {
            final ICacheElement<String, String> element = disk.processGet( "key:" + i );
            assertNotNull( element, "postsave, Should have received an element." );
            assertEquals( "data:" + i, element.getVal(), "postsave, element is wrong." );
        }

        disk.dump();

    }
}
