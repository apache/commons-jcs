package org.apache.commons.jcs.engine.control;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.commons.jcs.auxiliary.MockAuxiliaryCache;
import org.apache.commons.jcs.auxiliary.MockAuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.MockAuxiliaryCacheFactory;
import org.apache.commons.jcs.engine.logging.MockCacheEventLogger;

/** Unit tests for the configurator. */
public class CompositeCacheConfiguratorUnitTest
    extends TestCase
{
    /**
     * Verify that we can parse the event logger correctly
     */
    public void testParseAuxiliary_CacheEventLogger_Normal()
    {
        // SETUP
        String regionName = "MyRegion";

        String auxName = "MockAux";
        String auxPrefix = CompositeCacheConfigurator.AUXILIARY_PREFIX + auxName;
        String auxiliaryClassName = MockAuxiliaryCacheFactory.class.getName();
        String eventLoggerClassName = MockCacheEventLogger.class.getName();
        String auxiliaryAttributeClassName = MockAuxiliaryCacheAttributes.class.getName();

        Properties props = new Properties();
        props.put( auxPrefix, auxiliaryClassName );
        props.put( auxPrefix + CompositeCacheConfigurator.ATTRIBUTE_PREFIX, auxiliaryAttributeClassName );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, eventLoggerClassName );

//        System.out.print( props );

        CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();
        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator();

        // DO WORK
        AuxiliaryCache<String, String> aux = configurator.parseAuxiliary( props, manager, auxName, regionName );
        MockAuxiliaryCache<String, String> result = (MockAuxiliaryCache<String, String>)aux;

        // VERIFY
        assertNotNull( "Should have an auxcache.", result );
        assertNotNull( "Should have an event logger.", result.getCacheEventLogger() );
    }

    /**
     * Verify that we can parse the spool chunk size
     */
    public void testParseSpoolChunkSize_Normal()
    {
        // SETUP
        String regionName = "MyRegion";
        int chunkSize = 5;

        Properties props = new Properties();
        props.put( "jcs.default", "" );
        props.put( "jcs.default.cacheattributes.SpoolChunkSize", String.valueOf( chunkSize ) );

        CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();

        // DO WORK
        manager.configure( props );

        // VERIFY
        CompositeCache<String, String> cache = manager.getCache( regionName );
        assertEquals( "Wrong chunkSize", cache.getCacheAttributes().getSpoolChunkSize(), chunkSize );
    }
}
