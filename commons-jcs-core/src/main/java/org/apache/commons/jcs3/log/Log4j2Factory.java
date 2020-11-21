package org.apache.commons.jcs3.log;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

/**
 * This is a SPI factory implementation for log4j2
 */
public class Log4j2Factory implements LogFactory
{
    /** Use java.text.MessageFormat for log messages */
    private final MessageFactory messageFactory = new MessageFormatMessageFactory();

    /**
     * Return the name of the Log subsystem managed by this factory
     *
     * @return the name of the log subsystem
     */
    @Override
    public String getName()
    {
        return "log4j2";
    }

    /**
     * Shutdown the logging system if the logging system supports it.
     */
    @Override
    public void shutdown()
    {
        org.apache.logging.log4j.LogManager.shutdown();
    }

    /**
     * Returns a Log using the fully qualified name of the Class as the Log
     * name.
     *
     * @param clazz
     *            The Class whose name should be used as the Log name. If null
     *            it will default to the calling class.
     * @return The Log.
     * @throws UnsupportedOperationException
     *             if {@code clazz} is {@code null} and the calling class cannot
     *             be determined.
     */
    @Override
    public Log getLog(final Class<?> clazz)
    {
        final Logger logger = org.apache.logging.log4j.LogManager.getLogger(clazz, messageFactory);
        return new Log4j2LogAdapter(logger);
    }

    /**
     * Returns a Log with the specified name.
     *
     * @param name
     *            The logger name. If null the name of the calling class will be
     *            used.
     * @return The Log.
     * @throws UnsupportedOperationException
     *             if {@code name} is {@code null} and the calling class cannot
     *             be determined.
     */
    @Override
    public Log getLog(final String name)
    {
        final Logger logger = org.apache.logging.log4j.LogManager.getLogger(name, messageFactory);
        return new Log4j2LogAdapter(logger);
    }
}
