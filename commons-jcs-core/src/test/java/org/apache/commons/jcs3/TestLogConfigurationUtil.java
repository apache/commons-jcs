package org.apache.commons.jcs3;

import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.jcs3.log.LogManager;

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
        LogManager.setLogSystem("jul");
        java.util.logging.LogManager.getLogManager().reset();
        Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");

        rootLogger.addHandler(new MockLogHandler(stringWriter));
        rootLogger.setLevel(Level.FINE);
    }

    private static class MockLogHandler extends Handler
    {
        private final StringWriter writer;

        public MockLogHandler(StringWriter writer)
        {
            super();
            this.writer = writer;
        }

        @Override
        public void publish(LogRecord record)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getMillis())
              .append(" - ")
              .append(record.getSourceClassName())
              .append("#")
              .append(record.getSourceMethodName())
              .append(" - ")
              .append(record.getMessage())
              .append('\n');
            writer.append(sb.toString());
        }

        @Override
        public void flush()
        {
            writer.flush();
        }

        @Override
        public void close() throws SecurityException
        {
        }
    }
}
