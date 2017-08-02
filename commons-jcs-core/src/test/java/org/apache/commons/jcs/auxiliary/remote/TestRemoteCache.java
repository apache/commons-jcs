package org.apache.commons.jcs.auxiliary.remote;

import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.auxiliary.remote.server.RemoteCacheServerFactory;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @author Aaron SMuts
 */
public class TestRemoteCache
    extends TestCase
{
    /** The logger */
    private static final Log log = LogFactory.getLog( TestRemoteCache.class );

    /**
     * Start the cache.
     */
    public TestRemoteCache()
    {
        super();
        try
        {
            System.out.println( "main> creating registry on the localhost" );
            RemoteUtils.createRegistry( 1101 );
            Properties config = RemoteUtils.loadProps("/TestRemoteServer.ccf");

            RemoteCacheServerFactory.startup( "localhost", 1101, config);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestRemoteClient.ccf" );
    }

    /**
     * @throws Exception
     *
     *
     */
    public void skiptestSimpleSend()
        throws Exception
    {
        log.info( "testSimpleSend" );

        CacheAccess<String, String> cache = JCS.getInstance( "testCache" );

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
    public void testService()
        throws Exception
    {

        Thread.sleep( 100 );

        ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();

        RemoteCacheAttributes rca = new RemoteCacheAttributes();
        rca.setRemoteLocation( "localhost", 1101 );
        rca.setCacheName( "testCache" );

        RemoteCacheFactory factory = new RemoteCacheFactory();
        factory.initialize();
        RemoteCacheManager mgr = factory.getManager( rca, cacheMgr, new MockCacheEventLogger(), new MockElementSerializer() );
        AuxiliaryCache<String, String> cache = mgr.getCache( rca );

        int numMes = 100;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement<String, String> ce = new CacheElement<String, String>( "key" + 1, "data" + i, message );
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
