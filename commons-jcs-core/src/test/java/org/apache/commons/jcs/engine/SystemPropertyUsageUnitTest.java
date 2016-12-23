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

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.utils.props.PropertyLoader;

import java.util.Properties;

/**
 * Verify that system properties can override.
 */
public class SystemPropertyUsageUnitTest
    extends TestCase
{
    private static final String JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS = "jcs.default.cacheattributes.MaxObjects";
    private static final int testValue = 6789;

    private CompositeCacheManager manager = null;
    
    @Override
    protected void setUp() throws Exception
    {
       super.setUp();
       //First shut down any previously running manager.
       manager = CompositeCacheManager.getInstance();
       manager.shutDown();
    }

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		if (manager != null)
		{
			manager.shutDown();
		}

        System.clearProperty(JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS);
		super.tearDown();
	}

	/**
     * Verify that the system properties are used.
     * @throws Exception
     *
     */
    public void testSystemPropertyUsage()
        throws Exception
    {
        System.setProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS, String.valueOf(testValue) );
        
        JCS.setConfigFilename( "/TestSystemPropertyUsage.ccf" );
        
        CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        manager = CompositeCacheManager.getInstance();

        assertEquals( "System property value is not reflected.", testValue, jcs.getCacheAttributes().getMaxObjects());
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
        System.setProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS, String.valueOf(testValue) );

        manager = CompositeCacheManager.getUnconfiguredInstance();

        Properties props = PropertyLoader.loadProperties( "TestSystemPropertyUsage.ccf" );

        manager.configure( props, false );

        CacheAccess<String, String> jcs = JCS.getInstance( "someCacheNotInFile" );

        assertEquals( "System property value should not be reflected",
                      Integer.parseInt( props.getProperty( JCS_DEFAULT_CACHEATTRIBUTES_MAX_OBJECTS ) ),
                      jcs.getCacheAttributes().getMaxObjects());
    }
}
