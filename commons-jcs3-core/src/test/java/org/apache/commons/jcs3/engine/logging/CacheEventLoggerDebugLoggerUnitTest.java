package org.apache.commons.jcs3.engine.logging;

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

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.apache.commons.jcs3.TestLogConfigurationUtil;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.junit.Test;

/** Unit tests for the debug implementation */
public class CacheEventLoggerDebugLoggerUnitTest
{

    /** Verify that we can log */
    @Test
    public void testLogApplicationEvent_normal()
    {
        // SETUP
        final String logCategoryName = "testLogApplicationEvent_normal";

        final String source = "mySource";
        final String eventName = "MyEventName";
        final String optionalDetails = "SomeExtraData";

        final StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, logCategoryName );

        final CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        // DO WORK
        logger.logApplicationEvent( source, eventName, optionalDetails );

        // VERIFY
        final String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the optionalDetails should have been logged:" + result, result.indexOf( optionalDetails ) != -1 );
    }

    /** Verify that we can log */
    @Test
    public void testLogError_normal()
    {
        // SETUP
        final String logCategoryName = "testLogApplicationEvent_normal";

        final String source = "mySource";
        final String eventName = "MyEventName";
        final String errorMessage = "SomeExtraData";

        final StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, logCategoryName );

        final CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        // DO WORK
        logger.logError( source, eventName, errorMessage );

        // VERIFY
        final String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the errorMessage should have been logged:" + result, result.indexOf( errorMessage ) != -1 );
    }

    /** Verify that we can log */
    @Test
    public void testLogICacheEvent_normal()
    {
        // SETUP
        final String logCategoryName = "testLogEvent_normal";

        final String source = "mySource";
        final String region = "my region";
        final String eventName = "MyEventName";
        final String optionalDetails = "SomeExtraData";
        final String key = "my key";

        final StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, logCategoryName );

        final CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        final ICacheEvent<String> event = logger.createICacheEvent( source, region, eventName, optionalDetails, key );

        // DO WORK
        logger.logICacheEvent( event );

        // VERIFY
        final String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the region should have been logged:" + result, result.indexOf( region ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the optionalDetails should have been logged:" + result, result.indexOf( optionalDetails ) != -1 );
        assertTrue( "An event with the key should have been logged:" + result, result.indexOf( key ) != -1 );
    }
}
