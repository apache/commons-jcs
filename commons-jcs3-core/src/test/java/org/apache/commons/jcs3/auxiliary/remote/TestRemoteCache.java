package org.apache.commons.jcs3.auxiliary.remote;

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

import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs3.auxiliary.remote.server.RemoteCacheServerFactory;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs3.engine.control.MockElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 */
public class TestRemoteCache
{
    /** The logger */
    private static final Log log = LogManager.getLog( TestRemoteCache.class );

    /**
     * Start the cache.
     */
    public TestRemoteCache()
    {
        try
        {
            System.out.println( "main> creating registry on the localhost" );
            RemoteUtils.createRegistry( 1101 );
            final Properties config = RemoteUtils.loadProps("/TestRemoteServer.ccf");

            RemoteCacheServerFactory.startup( "localhost", 1101, config);
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestRemoteClient.ccf" );
    }

    /**
     * @throws Exception
     */
    @Test
    @Ignore
    public void testSimpleSend()
        throws Exception
    {
        log.info( "testSimpleSend" );

        final CacheAccess<String, String> cache = JCS.getInstance( "testCache" );

        log.info( "cache = " + cache );

        for ( int i = 0; i < 1000; i++ )
        {
//            System.out.println( "puttting " + i );
            cache.put( "key" + i, "data" + i );
//            System.out.println( "put " + i );
            log.info( "put " + i );
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testService()
        throws Exception
    {

        Thread.sleep( 100 );

        final ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();

        final RemoteCacheAttributes rca = new RemoteCacheAttributes();
        rca.setRemoteLocation( "localhost", 1101 );
        rca.setCacheName( "testCache" );

        final RemoteCacheFactory factory = new RemoteCacheFactory();
        factory.initialize();
        final RemoteCacheManager mgr = factory.getManager( rca, cacheMgr, new MockCacheEventLogger(), new MockElementSerializer() );
        final AuxiliaryCache<String, String> cache = mgr.getCache( rca );

        final int numMes = 100;
        for ( int i = 0; i < numMes; i++ )
        {
            final String message = "adsfasasfasfasdasf";
            final CacheElement<String, String> ce = new CacheElement<>( "key" + 1, "data" + i, message );
            cache.update( ce );
//            System.out.println( "put " + ce );
        }

        // Thread.sleep( 2000 );

        /*
         * // the receiver instance. JCS cacheReceiver = JCS.getInstance(
         * "testCache" );
         *
         * log.info( "cache = " + cache );
         *
         * for ( int i = 0; i < numMes; i++ ) { System.out.println( "getting " +
         * i ); Object data = cacheReceiver.get( "key" + i );
         * System.out.println( i + " = " + data ); }
         */
    }
}
