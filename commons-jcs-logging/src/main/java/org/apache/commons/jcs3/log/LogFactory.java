package org.apache.commons.jcs3.log;

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
 * This is a SPI factory interface for specialized Log objects
 */
public interface LogFactory
{
    /**
     * The name of the root Log.
     */
    String ROOT_LOGGER_NAME = "";

    /**
     * Return the name of the Log subsystem managed by this factory
     *
     * @return the name of the log subsystem
     */
    String getName();

    /**
     * Shutdown the logging system if the logging system supports it.
     */
    void shutdown();

    /**
     * Returns a Log using the fully qualified name of the Class as the Log
     * name.
     *
     * @param clazz
     *            The Class whose name should be used as the Log name.
     * @return The Log.
     * @throws UnsupportedOperationException
     *             if {@code clazz} is {@code null}
     */
    Log getLog(final Class<?> clazz);

    /**
     * Returns a Log with the specified name.
     *
     * @param name
     *            The logger name.
     * @return The Log.
     * @throws UnsupportedOperationException
     *             if {@code name} is {@code null}
     */
    Log getLog(final String name);
}
