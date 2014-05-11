package org.apache.commons.jcs.access;

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
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * This test is for the system property usage in configuration values.
 *
 * @author Aaron Smuts
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SystemPropertyUnitTest
    extends TestCase
{

    /**
     * Verify that we use a system property for a ${FOO} string in a value.
     *
     * @throws Exception
     *
     */
    public void test1SystemPropertyInValueDelimiter()
        throws Exception
    {

        int maxMemory = 1234;
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_MAX_SIZE", String.valueOf( maxMemory ) );

        JCS.setConfigFilename( "/TestSystemProperties.ccf" );

        CacheAccess<String, String> cache = JCS.getInstance( "test1" );
        assertEquals( "We should have used the system property for the memory size", maxMemory, cache
            .getCacheAttributes().getMaxObjects() );

        System.clearProperty("MY_SYSTEM_PROPERTY_DISK_DIR");
        System.clearProperty("MY_SYSTEM_PROPERTY_MAX_SIZE");
    }

    /**
     * Verify that we use a system property for a ${FOO} string in a value. We
     * define a propety in the cache.ccf file, but we do not have it as a system
     * property. The default value should be used, if one exists.
     *
     * @throws Exception
     *
     */
    public void test2SystemPropertyMissingInValueDelimeter()
        throws Exception
    {
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );

        CompositeCacheManager mgr = CompositeCacheManager.getUnconfiguredInstance();
        mgr.configure( "/TestSystemProperties.ccf" );

        CacheAccess<String, String> cache = JCS.getInstance( "missing" );
        // TODO check against the actual default def
        assertEquals( "We should have used the default property for the memory size", 100, cache.getCacheAttributes()
            .getMaxObjects() );

        System.clearProperty("MY_SYSTEM_PROPERTY_DISK_DIR");

    }

}
