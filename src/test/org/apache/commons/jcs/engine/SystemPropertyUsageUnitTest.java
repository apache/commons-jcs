package org.apache.commons.jcs.engine;

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

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.utils.props.PropertyLoader;

/**
 * Verify that system properties can override.
 */
public class SystemPropertyUsageUnitTest
    extends TestCase
{

    /**
     * Verify that the system properties are used.
     * @throws Exception
     *
     */
    public void testSystemPropertyUsage()
        throws Exception
    {
        final int testValue = 6789;

        System.getProperties().setProperty( "jcs.default.cacheattributes.MaxObjects", String.valueOf(testValue) );

        JCS.setConfigFilename( "/TestSystemPropertyUsage.ccf" );

        CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        assertEquals( "System property value is not reflected", testValue, jcs.getCacheAttributes().getMaxObjects());

    }

    /**
     * Verify that the system properties are not used is specified.
     *
     * @throws Exception
     *
     */
    public void testSystemPropertyUsage_inactive()
        throws Exception
    {
        System.getProperties().setProperty( "jcs.default.cacheattributes.MaxObjects", "6789" );

        CompositeCacheManager mgr = CompositeCacheManager.getUnconfiguredInstance();

        Properties props = PropertyLoader.loadProperties( "TestSystemPropertyUsage.ccf" );

        mgr.configure( props, false );

        CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        assertFalse( "System property value should not be reflected",
                     jcs.getCacheAttributes().getMaxObjects() == Integer.parseInt( props
                         .getProperty( "jcs.default.cacheattributes.MaxObjects" ) ) );

    }
}
