package org.apache.commons.jcs4.engine.control;

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

import java.util.Properties;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.commons.jcs4.auxiliary.MockAuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.MockAuxiliaryCacheAttributes;
import org.apache.commons.jcs4.auxiliary.MockAuxiliaryCacheFactory;
import org.apache.commons.jcs4.engine.logging.MockCacheEventLogger;
import org.junit.jupiter.api.Test;

/** Tests for the configurator. */
class CompositeCacheConfiguratorUnitTest
{
    /**
     * Verify that we can parse the event logger correctly
     */
    @Test
    void testParseAuxiliary_CacheEventLogger_Normal()
    {
        // SETUP
        final String regionName = "MyRegion";

        final String auxName = "MockAux";
        final String auxPrefix = CompositeCacheConfigurator.AUXILIARY_PREFIX + auxName;
        final String auxiliaryClassName = MockAuxiliaryCacheFactory.class.getName();
        final String eventLoggerClassName = MockCacheEventLogger.class.getName();
        final String auxiliaryAttributeClassName = MockAuxiliaryCacheAttributes.class.getName();

        final Properties props = new Properties();
        props.put( auxPrefix, auxiliaryClassName );
        props.put( auxPrefix + CompositeCacheConfigurator.ATTRIBUTE_PREFIX, auxiliaryAttributeClassName );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, eventLoggerClassName );

//        System.out.print( props );

        final CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();
        final CompositeCacheConfigurator configurator = new CompositeCacheConfigurator();

        // DO WORK
        final AuxiliaryCache<String, String> aux = configurator.parseAuxiliary( props, manager, auxName, regionName );
        final MockAuxiliaryCache<String, String> result = (MockAuxiliaryCache<String, String>)aux;

        // VERIFY
        assertNotNull( result, "Should have an auxcache." );
        assertNotNull( result.getCacheEventLogger(), "Should have an event logger." );
    }

    /**
     * Verify that we can parse the spool chunk size
     */
    @Test
    void testParseSpoolChunkSize_Normal()
    {
        // SETUP
        final String regionName = "MyRegion";
        final int chunkSize = 5;

        final Properties props = new Properties();
        props.put( "jcs.default", "" );
        props.put( "jcs.default.cacheattributes.SpoolChunkSize", String.valueOf( chunkSize ) );

        final CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();

        // DO WORK
        manager.configure( props );

        // VERIFY
        final CompositeCache<String, String> cache = manager.getCache( regionName );
        assertEquals( cache.getCacheAttributes().spoolChunkSize(), chunkSize, "Wrong chunkSize" );
    }
}
