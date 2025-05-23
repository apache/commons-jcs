package org.apache.commons.jcs3.auxiliary.remote.server;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.rmi.server.RMISocketFactory;
import java.util.Properties;

import org.apache.commons.jcs3.auxiliary.remote.behavior.ICommonRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.junit.jupiter.api.Test;

/** Tests for the factory */
class RemoteCacheServerFactoryUnitTest
{
    /** Verify that we get the timeout value */
    @Test
    void testConfigureObjectSpecificCustomFactory_withProperty()
    {
        // SETUP
        final String testValue = "123245";
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX, MockRMISocketFactory.class.getName() );
        props.put( IRemoteCacheConstants.CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX + ".testStringProperty", testValue );

        // DO WORK
        final RMISocketFactory result = RemoteCacheServerFactory.configureObjectSpecificCustomFactory( props );

        // VERIFY
        assertNotNull( result, "Should have a custom socket factory." );
        assertEquals( testValue, ( (MockRMISocketFactory) result ).getTestStringProperty(), "Wrong testValue" );
    }

    /** Verify that we get the timeout value */
    @Test
    void testConfigureObjectSpecificCustomFactory_withProperty_TimeoutConfigurableRMIScoketFactory()
    {
        // SETUP
        final int readTimeout = 1234;
        final int openTimeout = 1234;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX, TimeoutConfigurableRMISocketFactory.class.getName() );
        props.put( IRemoteCacheConstants.CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX + ".readTimeout", String.valueOf( readTimeout ) );
        props.put( IRemoteCacheConstants.CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX + ".openTimeout", String.valueOf( openTimeout ) );

        // DO WORK
        final RMISocketFactory result = RemoteCacheServerFactory.configureObjectSpecificCustomFactory( props );

        // VERIFY
        assertNotNull( result, "Should have a custom socket factory." );
        assertEquals( readTimeout, ( (TimeoutConfigurableRMISocketFactory) result ).getReadTimeout(),
                      "Wrong readTimeout" );
        assertEquals( openTimeout, ( (TimeoutConfigurableRMISocketFactory) result ).getOpenTimeout(),
                      "Wrong readTimeout" );
    }

    /** Verify that we get the startRegistry value */
    @Test
    void testConfigureRemoteCacheServerAttributes_allowClusterGetPresent()
    {
        // SETUP
        final boolean allowClusterGet = false;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".allowClusterGet", String.valueOf( allowClusterGet ) );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( allowClusterGet, result.isAllowClusterGet(), "Wrong allowClusterGet" );
    }

    /** Verify that we get the timeout value */
    @Test
    void testConfigureRemoteCacheServerAttributes_eventQueuePoolName()
    {
        // SETUP
        final String eventQueuePoolName = "specialName";
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".EventQueuePoolName", eventQueuePoolName );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( eventQueuePoolName, result.getEventQueuePoolName(), "Wrong eventQueuePoolName" );
    }

    /** Verify that we get the startRegistry value */
    @Test
    void testConfigureRemoteCacheServerAttributes_localClusterConsistencyPresent()
    {
        // SETUP
        final boolean localClusterConsistency = false;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".localClusterConsistency", String.valueOf( localClusterConsistency ) );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( localClusterConsistency, result.isLocalClusterConsistency(), "Wrong localClusterConsistency" );
    }

    /** Verify that we get the registryKeepAliveDelayMillis value */
    @Test
    void testConfigureRemoteCacheServerAttributes_registryKeepAliveDelayMillisPresent()
    {
        // SETUP
        final int registryKeepAliveDelayMillis = 123245;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".registryKeepAliveDelayMillis", String.valueOf( registryKeepAliveDelayMillis ) );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( registryKeepAliveDelayMillis, result.getRegistryKeepAliveDelayMillis(),
                      "Wrong registryKeepAliveDelayMillis" );
    }

    /** Verify that we get the registryKeepAliveDelayMillis value */
    @Test
    void testConfigureRemoteCacheServerAttributes_rmiSocketFactoryTimeoutMillisPresent()
    {
        // SETUP
        final int rmiSocketFactoryTimeoutMillis = 123245;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".rmiSocketFactoryTimeoutMillis", String.valueOf( rmiSocketFactoryTimeoutMillis ) );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( rmiSocketFactoryTimeoutMillis, result.getRmiSocketFactoryTimeoutMillis(),
                      "Wrong rmiSocketFactoryTimeoutMillis" );
    }

    /** Verify that we get the timeout value */
    @Test
    void testConfigureRemoteCacheServerAttributes_timeoutNotPresent()
    {
        // SETUP
        final Properties props = new Properties();

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( ICommonRemoteCacheAttributes.DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MILLIS,
                      result.getRmiSocketFactoryTimeoutMillis(), "Wrong timeout" );
    }

    /** Verify that we get the useRegistryKeepAlive value */
    @Test
    void testConfigureRemoteCacheServerAttributes_useRegistryKeepAlivePresent()
    {
        // SETUP
        final boolean useRegistryKeepAlive = false;
        final Properties props = new Properties();
        props.put( IRemoteCacheConstants.CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".useRegistryKeepAlive", String.valueOf( useRegistryKeepAlive ) );

        // DO WORK
        final RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );

        // VERIFY
        assertEquals( useRegistryKeepAlive, result.isUseRegistryKeepAlive(), "Wrong useRegistryKeepAlive" );
    }
}
