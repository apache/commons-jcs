package org.apache.commons.jcs3.engine;

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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.utils.props.PropertyLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Verify that system properties can override.
 */
public class SystemPropertyUsageUnitTest
{
    private static final String JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS = "jcs.default.cacheattributes.MaxObjects";
    private static final int testValue = 6789;

    private CompositeCacheManager manager;

    @Before
    public void setUp() throws Exception
    {
       //First shut down any previously running manager.
       manager = CompositeCacheManager.getInstance();
       manager.shutDown();
    }

    @After
    public void tearDown() throws Exception
	{
		if (manager != null)
		{
			manager.shutDown();
		}

        System.clearProperty(JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS);
	}

	/**
     * Verify that the system properties are used.
     * @throws Exception
     *
     */
    @Test
    public void testSystemPropertyUsage()
        throws Exception
    {
        System.setProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS, String.valueOf(testValue) );

        JCS.setConfigFilename( "/TestSystemPropertyUsage.ccf" );

        final CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        manager = CompositeCacheManager.getInstance();

        assertEquals( "System property value is not reflected.", testValue, jcs.getCacheAttributes().getMaxObjects());
    }

    /**
     * Verify that the system properties are not used is specified.
     *
     * @throws Exception
     *
     */
    @Test
    public void testSystemPropertyUsage_inactive()
        throws Exception
    {
        System.setProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS, String.valueOf(testValue) );

        manager = CompositeCacheManager.getUnconfiguredInstance();

        final Properties props = PropertyLoader.loadProperties( "TestSystemPropertyUsage.ccf" );

        manager.configure( props, false );

        final CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        assertEquals( "System property value should not be reflected",
                      Integer.parseInt( props.getProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS ) ),
                      jcs.getCacheAttributes().getMaxObjects());
    }
}
