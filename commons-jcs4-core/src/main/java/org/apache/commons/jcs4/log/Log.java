package org.apache.commons.jcs4.log;

import java.lang.System.Logger;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

import java.util.function.Supplier;

/**
 * This is a borrowed and stripped-down version of the log4j2 Logger interface.
 * All logging operations, except configuration, are done through this interface.
 *
 * <p>
 * The canonical way to obtain a Logger for a class is through {@link Log#getLog(String)}}.
 * Typically, each class should get its own Log named after its fully qualified class name
 * </p>
 *
 * <pre>
 * public class MyClass {
 *     private static final Log log = Log.getLog(MyClass.class);
 *     // ...
 * }
 * </pre>
 */
public interface Log
{
    /**
     * The name of the root Log.
     */
    String ROOT_LOGGER_NAME = "";

    /**
     * Logs a message object with the DEBUG level.
     *
     * @param message the message object to log.
     */
    void debug(Object message);

    /**
     * Logs a message object with the DEBUG level.
     *
     * @param message the message string to log.
     */
    void debug(String message);

    /**
     * Logs a message with parameters at the DEBUG level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void debug(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the DEBUG level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void debug(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the DEBUG level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    void debug(String message, Throwable t);

    /**
     * Logs a message object with the ERROR level.
     *
     * @param message the message object to log.
     */
    void error(Object message);

    /**
     * Logs a message object with the ERROR level.
     *
     * @param message the message string to log.
     */
    void error(String message);

    /**
     * Logs a message with parameters at the ERROR level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void error(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the ERROR level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void error(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the ERROR level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    void error(String message, Throwable t);

    /**
     * Logs a message object with the FATAL level.
     *
     * @param message the message object to log.
     */
    void fatal(Object message);

    /**
     * Logs a message object with the FATAL level.
     *
     * @param message the message string to log.
     */
    void fatal(String message);

    /**
     * Logs a message with parameters at the FATAL level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void fatal(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the FATAL level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void fatal(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the FATAL level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    void fatal(String message, Throwable t);

    /**
     * Gets the logger name.
     *
     * @return the logger name.
     */
    String getName();

    /**
     * Logs a message object with the INFO level.
     *
     * @param message the message object to log.
     */
    void info(Object message);

    /**
     * Logs a message object with the INFO level.
     *
     * @param message the message string to log.
     */
    void info(String message);

    /**
     * Logs a message with parameters at the INFO level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void info(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the INFO level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void info(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the INFO level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    void info(String message, Throwable t);

    /**
     * Checks whether this Logger is enabled for the DEBUG Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false}
     *         otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Checks whether this Logger is enabled for the ERROR Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level ERROR, {@code false}
     *         otherwise.
     */
    boolean isErrorEnabled();

    /**
     * Checks whether this Logger is enabled for the FATAL Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level FATAL, {@code false}
     *         otherwise.
     */
    boolean isFatalEnabled();

    /**
     * Checks whether this Logger is enabled for the INFO Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level INFO, {@code false}
     *         otherwise.
     */
    boolean isInfoEnabled();

    /**
     * Checks whether this Logger is enabled for the TRACE level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level TRACE, {@code false}
     *         otherwise.
     */
    boolean isTraceEnabled();

    /**
     * Checks whether this Logger is enabled for the WARN Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level WARN, {@code false}
     *         otherwise.
     */
    boolean isWarnEnabled();

    /**
     * Logs a message object with the TRACE level.
     *
     * @param message the message object to log.
     */
    void trace(Object message);

    /**
     * Logs a message object with the TRACE level.
     *
     * @param message the message string to log.
     */
    void trace(String message);

    /**
     * Logs a message with parameters at the TRACE level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void trace(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the TRACE level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void trace(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the TRACE level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     * @see #debug(String)
     */
    void trace(String message, Throwable t);

    /**
     * Logs a message object with the WARN level.
     *
     * @param message the message object to log.
     */
    void warn(Object message);

    /**
     * Logs a message object with the WARN level.
     *
     * @param message the message string to log.
     */
    void warn(String message);

    /**
     * Logs a message with parameters at the WARN level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    void warn(String message, Object... params);

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the WARN level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    void warn(String message, Supplier<?>... paramSuppliers);

    /**
     * Logs a message at the WARN level including the stack trace of the {@link Throwable}
     * {@code t} passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    void warn(String message, Throwable t);

    /**
     * Returns the root logger.
     *
     * @return the root logger, named {@link ROOT_LOGGER_NAME}.
     */
    static Log getRootLogger()
    {
        return getLog(ROOT_LOGGER_NAME);
    }

    /**
     * Returns a Log with the specified name.
     *
     * @param name
     *            The logger name.
     * @return The Log.
     * @throws UnsupportedOperationException
     *             if {@code name} is {@code null}
     */
    static Log getLog(final String name)
    {
        final Logger logger = System.getLogger(name);
        return new SystemLogAdapter(logger);
    }

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
    static Log getLog(final Class<?> clazz)
    {
        return Log.getLog(clazz.getName());
    }
}