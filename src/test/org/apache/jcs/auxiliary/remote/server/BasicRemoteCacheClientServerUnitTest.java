package org.apache.jcs.auxiliary.remote.server;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache License, Version
 * 2.0 (the "License") you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.remote.RemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.RemoteCacheManager;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCacheManagerMockImpl;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * These tests startup the remote server and make requests to it.
 * <p>
 * @author Aaron Smuts
 */
public class BasicRemoteCacheClientServerUnitTest
    extends TestCase
{

    /**
     * Verify that we can start the remote cache server. Send an item to the remote. Verify that the
     * remote put count goes up. If we go through JCS, the manager will be shared and we will get
     * into an endless loop. We will use a mock cache manager instead.
     * <p>
     * The remote server uses the real JCS. We can verify that items are added to JCS behind the
     * server by calling get. We cannot access it directly via JCS since it is serialized.
     * <p>
     * This test uses a mock injected client to test a normal server.
     * <p>
     * @throws Exception
     */
    public void testSinglePut()
        throws Exception
    {
        // SETUP
        String configFile = "TestRemoteCacheClientServer.ccf";
        RemoteCacheServer server = RemoteCacheServerStartupUtil.startServerUsingProperties( configFile );

        CompositeCacheManagerMockImpl compositeCacheManager = new CompositeCacheManagerMockImpl();

        RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteHost( "localhost" );
        attributes.setLocalPort( 1202 );
        attributes.setRemotePort( 1101 );

        RemoteCacheManager remoteCacheManager = RemoteCacheManager.getInstance( attributes, compositeCacheManager );
        String regionName = "testSinglePut";
        AuxiliaryCache cache = remoteCacheManager.getCache( regionName );

        // DO WORK
        ICacheElement element = new CacheElement( regionName, "key", "value" );
        cache.update( element );
        SleepUtil.sleepAtLeast( 50 );

        // VERIFY
        System.out.println( server.getStats() );
        assertEquals( "Wrong number of puts", 1, server.getPutCount() );

        // DO WORK
        ICacheElement result = cache.get( "key" );

        // VERIFY
        assertEquals( "Wrong element.", element.getVal(), result.getVal() );
    }

}
