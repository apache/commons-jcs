package org.apache.commons.jcs.auxiliary.lateral.socket.tcp;

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
import org.apache.commons.jcs.auxiliary.lateral.LateralCache;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.logging.MockCacheEventLogger;
import org.apache.commons.jcs.utils.discovery.DiscoveredService;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

import java.util.ArrayList;

/** Test for the listener that observers UDP discovery events. */
public class LateralTCPDiscoveryListenerUnitTest
    extends TestCase
{
    /** the listener */
    private LateralTCPDiscoveryListener listener;

    /** the cache factory */
    private LateralTCPCacheFactory factory;

    /** The cache manager. */
    private CompositeCacheManager cacheMgr;

    /** The event logger. */
    protected MockCacheEventLogger cacheEventLogger;

    /** The serializer. */
    protected IElementSerializer elementSerializer;

    /** Create the listener for testing */
    @Override
    protected void setUp() throws Exception
    {
        factory = new LateralTCPCacheFactory();
        factory.initialize();

        cacheMgr = CompositeCacheManager.getInstance();
        cacheEventLogger = new MockCacheEventLogger();
        elementSerializer = new StandardSerializer();

        listener = new LateralTCPDiscoveryListener( factory.getName(), cacheMgr );
    }

    /**
     * Add a no wait facade.
     */
    public void testAddNoWaitFacade_NotInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_NotInList";

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );

        // DO WORK
        listener.addNoWaitFacade( cacheName, facade );

        // VERIFY
        assertTrue( "Should have the facade.", listener.containsNoWaitFacade( cacheName ) );
    }

    /**
     * Add a no wait to a known facade.
     */
    public void testAddNoWait_FacadeInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

        // DO WORK
        boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should have added the no wait.", result );
    }

    /**
     * Add a no wait from an unknown facade.
     */
    public void testAddNoWait_FacadeNotInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

        // DO WORK
        boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have added the no wait.", result );
    }

    /**
     * Remove a no wait from an unknown facade.
     */
    public void testRemoveNoWait_FacadeNotInList()
    {
        // SETUP
        String cacheName = "testRemoveNoWaitFacade_FacadeNotInList";
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have removed the no wait.", result );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveNoWait_FacadeInList_NoWaitNot()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have removed the no wait.", result );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveNoWait_FacadeInList_NoWaitIs()
    {
        // SETUP
        String cacheName = "testRemoveNoWaitFacade_FacadeInListNoWaitIs";

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );
        listener.addNoWait( noWait );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertTrue( "Should have removed the no wait.", result );
    }

    /**
     * Add a no wait to a known facade.
     */
    public void testAddDiscoveredService_FacadeInList_NoWaitNot()
    {
        // SETUP
        String cacheName = "testAddDiscoveredService_FacadeInList_NoWaitNot";

        ArrayList<String> cacheNames = new ArrayList<String>();
        cacheNames.add( cacheName );

        DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        // since the no waits are compared by object equality, I have to do this
        // TODO add an equals method to the noWait.  the problem if is figuring out what to compare.
        ITCPLateralCacheAttributes lca = new TCPLateralCacheAttributes();
        lca.setTransmissionType( LateralCacheAttributes.Type.TCP );
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        lca.setCacheName(cacheName);
        LateralCacheNoWait<String, String> noWait = factory.createCacheNoWait(lca, cacheEventLogger, elementSerializer);
        // this is the normal process, the discovery service expects it there
        cacheMgr.addAuxiliaryCache(factory.getName(), cacheName, noWait);

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );
        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        // DO WORK
        listener.addDiscoveredService( service );

        // VERIFY
        assertTrue( "Should have no wait.", listener.containsNoWait( cacheName, noWait ) );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveDiscoveredService_FacadeInList_NoWaitIs()
    {
        // SETUP
        String cacheName = "testRemoveDiscoveredService_FacadeInList_NoWaitIs";

        ArrayList<String> cacheNames = new ArrayList<String>();
        cacheNames.add( cacheName );

        DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        // since the no waits are compared by object equality, I have to do this
        // TODO add an equals method to the noWait.  the problem if is figuring out what to compare.
        ITCPLateralCacheAttributes lca = new TCPLateralCacheAttributes();
        lca.setTransmissionType( LateralCacheAttributes.Type.TCP );
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        lca.setCacheName(cacheName);
        LateralCacheNoWait<String, String> noWait = factory.createCacheNoWait(lca, cacheEventLogger, elementSerializer);
        // this is the normal process, the discovery service expects it there
        cacheMgr.addAuxiliaryCache(factory.getName(), cacheName, noWait);

        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );
        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );
        listener.addDiscoveredService( service );

        // DO WORK
        listener.removeDiscoveredService( service );

        // VERIFY
        assertFalse( "Should not have no wait.", listener.containsNoWait( cacheName, noWait ) );
    }
}
