package org.apache.commons.jcs.auxiliary.remote;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;

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
 * Tests for RemoteCacheNoWaitFacade.
 */
public class RemoteCacheNoWaitFacadeUnitTest
    extends TestCase
{
    /**
     * Verify that we can add an item.
     */
    public void testAddNoWait_InList()
    {
        // SETUP
        List<RemoteCacheNoWait<String, String>> noWaits = new ArrayList<RemoteCacheNoWait<String,String>>();
        IRemoteCacheAttributes cattr = new RemoteCacheAttributes();
        cattr.setCacheName( "testCache1" );

        RemoteCache<String, String> client = new RemoteCache<String, String>(cattr, null, null, null);
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );
        noWaits.add( noWait );

        RemoteCacheNoWaitFacade<String, String> facade = new RemoteCacheNoWaitFacade<String, String>(noWaits, cattr, null, null, null );
        
        // VERIFY
        assertEquals( "Should have one entry.", 1, facade.noWaits.size() );
        assertTrue( "Should be in the list.", facade.noWaits.contains( noWait ) );
        assertSame( "Should have same facade.", facade, ((RemoteCache<String, String>)facade.noWaits.get(0).getRemoteCache()).getFacade() );
    }
}
