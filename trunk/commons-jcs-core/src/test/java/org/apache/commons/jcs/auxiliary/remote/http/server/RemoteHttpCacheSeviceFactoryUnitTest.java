package org.apache.commons.jcs.auxiliary.remote.http.server;

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
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.commons.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.logging.MockCacheEventLogger;

import java.util.Properties;

/** Unit tests for the factory */
public class RemoteHttpCacheSeviceFactoryUnitTest
    extends TestCase
{
    /** verify that we get the CacheEventLogger value */
    public void testCreateRemoteHttpCacheService_WithLogger()
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        String className = MockCacheEventLogger.class.getName();

        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );

        boolean allowClusterGet = false;
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".allowClusterGet", String
            .valueOf( allowClusterGet ) );

        manager.setConfigurationProperties( props );

        // DO WORK
        RemoteHttpCacheService<String, String> result = RemoteHttpCacheSeviceFactory
            .createRemoteHttpCacheService( manager );

        // VERIFY
        assertNotNull( "Should have a service.", result );
    }

    /** verify that we get the CacheEventLogger value */
    public void testConfigureCacheEventLogger_Present()
    {
        // SETUP
        String testPropertyValue = "This is the value";
        String className = MockCacheEventLogger.class.getName();

        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX
            + ".testProperty", testPropertyValue );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) RemoteHttpCacheSeviceFactory
            .configureCacheEventLogger( props );

        // VERIFY
        assertNotNull( "Should have a logger.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /** verify that we get the allowClusterGet value */
    public void testConfigureRemoteCacheServerAttributes_allowClusterGetPresent()
    {
        // SETUP
        boolean allowClusterGet = false;
        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".allowClusterGet", String
            .valueOf( allowClusterGet ) );

        // DO WORK
        RemoteHttpCacheServerAttributes result = RemoteHttpCacheSeviceFactory
            .configureRemoteHttpCacheServerAttributes( props );

        // VERIFY
        assertEquals( "Wrong allowClusterGet", allowClusterGet, result.isAllowClusterGet() );
    }

    /** verify that we get the startRegistry value */
    public void testConfigureRemoteCacheServerAttributes_localClusterConsistencyPresent()
    {
        // SETUP
        boolean localClusterConsistency = false;
        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".localClusterConsistency",
                   String.valueOf( localClusterConsistency ) );

        // DO WORK
        RemoteHttpCacheServerAttributes result = RemoteHttpCacheSeviceFactory
            .configureRemoteHttpCacheServerAttributes( props );

        // VERIFY
        assertEquals( "Wrong localClusterConsistency", localClusterConsistency, result.isLocalClusterConsistency() );
    }
}
