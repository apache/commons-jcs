package org.apache.commons.jcs.auxiliary;

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
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.MockCacheEventLogger;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

import java.util.Properties;

/** Unit tests for the auxiliary cache configurator. */
public class AuxiliaryCacheConfiguratorUnitTest
    extends TestCase
{
    /**
     * Verify that we don't get an error.
     */
    public void testParseCacheEventLogger_Null()
    {
        // SETUP
        Properties props = new Properties();

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator.parseCacheEventLogger( props,
                                                                                                               "junk" );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }

    /**
     * Verify that we don't get an error.
     */
    public void testParseCacheEventLogger_NullName()
    {
        // SETUP
        Properties props = new Properties();

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator.parseCacheEventLogger( props,
                                                                                                               null );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }

    /**
     * Verify that we can parse the event logger.
     */
    public void testParseCacheEventLogger_Normal()
    {
        // SETUP
        String auxPrefix = "jcs.auxiliary." + "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockCacheEventLogger.class.getName();

        Properties props = new Properties();
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX
            + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator
            .parseCacheEventLogger( props, auxPrefix );

        // VERIFY
        assertNotNull( "Should have a logger.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /**
     * Verify that we can parse the ElementSerializer.
     */
    public void testParseElementSerializer_Normal()
    {
        // SETUP
        String auxPrefix = "jcs.auxiliary." + "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockElementSerializer.class.getName();

        Properties props = new Properties();
        props.put( auxPrefix + AuxiliaryCacheConfigurator.SERIALIZER_PREFIX, className );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.SERIALIZER_PREFIX
            + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        // DO WORK
        MockElementSerializer result = (MockElementSerializer) AuxiliaryCacheConfigurator
            .parseElementSerializer( props, auxPrefix );

        // VERIFY
        assertNotNull( "Should have a Serializer.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /**
     * Verify that we can parse the ElementSerializer.
     */
    public void testParseElementSerializer_Null()
    {
        // SETUP
        Properties props = new Properties();

        // DO WORK
        IElementSerializer result = AuxiliaryCacheConfigurator
            .parseElementSerializer( props, "junk" );

        // VERIFY
        assertTrue( "Should have the default Serializer.", result instanceof StandardSerializer );
    }
}
