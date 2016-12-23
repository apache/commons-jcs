package org.apache.commons.jcs.engine.logging;

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
import org.apache.commons.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import java.io.StringWriter;

/** Unit tests for the debug implementation */
public class CacheEventLoggerDebugLoggerUnitTest
    extends TestCase
{

    /** verify that we can log */
    public void testLogICacheEvent_normal()
    {
        // SETUP
        String logCategoryName = "testLogEvent_normal";

        String source = "mySource";
        String region = "my region";
        String eventName = "MyEventName";
        String optionalDetails = "SomeExtraData";
        String key = "my key";

        StringWriter stringWriter = new StringWriter();
        configureLogger( stringWriter, logCategoryName );

        CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        ICacheEvent<String> event = logger.createICacheEvent( source, region, eventName, optionalDetails, key );

        // DO WORK
        logger.logICacheEvent( event );

        // VERIFY
        String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the region should have been logged:" + result, result.indexOf( region ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the optionalDetails should have been logged:" + result, result.indexOf( optionalDetails ) != -1 );
        assertTrue( "An event with the key should have been logged:" + result, result.indexOf( key ) != -1 );
    }

    /** verify that we can log */
    public void testLogApplicationEvent_normal()
    {
        // SETUP
        String logCategoryName = "testLogApplicationEvent_normal";

        String source = "mySource";
        String eventName = "MyEventName";
        String optionalDetails = "SomeExtraData";

        StringWriter stringWriter = new StringWriter();
        configureLogger( stringWriter, logCategoryName );

        CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        // DO WORK
        logger.logApplicationEvent( source, eventName, optionalDetails );

        // VERIFY
        String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the optionalDetails should have been logged:" + result, result.indexOf( optionalDetails ) != -1 );
    }

    /** verify that we can log */
    public void testLogError_normal()
    {
        // SETUP
        String logCategoryName = "testLogApplicationEvent_normal";

        String source = "mySource";
        String eventName = "MyEventName";
        String errorMessage = "SomeExtraData";

        StringWriter stringWriter = new StringWriter();
        configureLogger( stringWriter, logCategoryName );

        CacheEventLoggerDebugLogger logger = new CacheEventLoggerDebugLogger();
        logger.setLogCategoryName( logCategoryName );

        // DO WORK
        logger.logError( source, eventName, errorMessage );

        // VERIFY
        String result = stringWriter.toString();
        assertTrue( "An event with the source should have been logged:" + result, result.indexOf( source ) != -1 );
        assertTrue( "An event with the event name should have been logged:" + result, result.indexOf( eventName ) != -1 );
        assertTrue( "An event with the errorMessage should have been logged:" + result, result.indexOf( errorMessage ) != -1 );
    }

    /**
     * Configures a logger for the given name. This allows us to check the log output.
     * <p>
     * @param stringWriter
     * @param loggerName
     */
    private void configureLogger( StringWriter stringWriter, String loggerName )
    {
        Logger logger = Logger.getLogger( loggerName );
        WriterAppender appender = null;

        try
        {
            appender = new WriterAppender( new PatternLayout(), stringWriter );
        }
        catch ( Exception e )
        {
            // NOOP
        }

        logger.addAppender( appender );
        logger.setLevel( Level.DEBUG );
    }
}
