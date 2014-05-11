package org.apache.commons.jcs;

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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import java.io.StringWriter;

/** Utility for testing log messages. */
public class TestLogConfigurationUtil
{
    /**
     * Configures a logger for the given name. This allows us to check the log output.
     * <p>
     * @param stringWriter string writer
     * @param loggerName logger name
     */
    public static void configureLogger( StringWriter stringWriter, String loggerName )
    {
        Logger logger = Logger.getLogger( loggerName );
        WriterAppender appender = new WriterAppender( new PatternLayout(), stringWriter );

        logger.addAppender( appender );
        logger.setLevel( Level.DEBUG );
    }
}
