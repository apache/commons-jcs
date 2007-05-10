package org.apache.jcs.auxiliary.remote;

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

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.engine.CacheElementSerialized;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.CompositeCacheManagerMockImpl;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * Tests for the remote cache listener.
 * <p>
 * @author Aaron Smuts
 *
 */
public class RemoteCacheListenerUnitTest
    extends TestCase
{
    /**
     * Create a RemoteCacheListener with a mock cache manager.  Set remove on put to false.
     * Create a serialized element.  Call put on the listener.
     * Verify that the deserialized element is in the cache.
     *
     * @throws Exception
     */
    public void testUpdate()
        throws Exception
    {
        IRemoteCacheAttributes irca = new RemoteCacheAttributes();
        irca.setRemoveUponRemotePut( false );
        ICompositeCacheManager cacheMgr = new CompositeCacheManagerMockImpl();
        RemoteCacheListener listener = new RemoteCacheListener( irca, cacheMgr );

        String cacheName = "testName";
        String key = "key";
        String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( 34 );

        IElementSerializer elementSerializer = new StandardSerializer();

        ICacheElementSerialized element = new CacheElementSerialized( cacheName, key, elementSerializer
            .serialize( value ), attr );
        listener.handlePut( element );

        ICacheElement after = cacheMgr.getCache( cacheName ).get( key );

        assertNotNull( "Should have a deserialized object.", after );
        assertEquals( "Values should be the same.", value, after.getVal() );
        assertEquals( "Attributes should be the same.", attr.getMaxLifeSeconds(), after
            .getElementAttributes().getMaxLifeSeconds() );
        assertEquals( "Keys should be the same.", key, after.getKey() );
        assertEquals( "Cache name should be the same.", cacheName, after.getCacheName() );
    }

    /**
     * Create a RemoteCacheListener with a mock cache manager.  Set remove on put to false.
     * Create a serialized element.  Call put on the listener.
     * Verify that the deserialized element is in the cache.
     *
     * @throws Exception
     */
    public void testUpdate_RemoveOnPut()
        throws Exception
    {
        IRemoteCacheAttributes irca = new RemoteCacheAttributes();
        irca.setRemoveUponRemotePut( true );
        ICompositeCacheManager cacheMgr = new CompositeCacheManagerMockImpl();
        RemoteCacheListener listener = new RemoteCacheListener( irca, cacheMgr );

        String cacheName = "testName";
        String key = "key";
        String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( 34 );

        IElementSerializer elementSerializer = new StandardSerializer();

        ICacheElementSerialized element = new CacheElementSerialized( cacheName, key, elementSerializer
            .serialize( value ), attr );
        listener.handlePut( element );

        ICacheElement after = cacheMgr.getCache( cacheName ).get( key );

        assertNull( "Should not have a deserialized object since remove on put is true.", after );
    }

}
