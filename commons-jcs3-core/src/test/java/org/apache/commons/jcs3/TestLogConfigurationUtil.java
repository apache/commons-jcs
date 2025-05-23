package org.apache.commons.jcs3;

import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

/** Utility for testing log messages. */
public class TestLogConfigurationUtil
{
    private static final class MockLogHandler extends Handler
    {
        private final StringWriter writer;

        public MockLogHandler(final StringWriter writer)
        {
            this.writer = writer;
        }

        @Override
        public void close() throws SecurityException
        {
        }

        @Override
        public void flush()
        {
            writer.flush();
        }

        @Override
        public void publish(final LogRecord record)
        {
            final StringBuilder sb = new StringBuilder();
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
    }

    /**
     * Configures a logger for the given name. This allows us to check the log output.
     *
     * @param stringWriter string writer
     * @param loggerName logger name
     */
    public static void configureLogger( final StringWriter stringWriter, final String loggerName )
    {
        java.util.logging.LogManager.getLogManager().reset();
        final Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");

        rootLogger.addHandler(new MockLogHandler(stringWriter));
        rootLogger.setLevel(Level.FINE);
    }
}
