package org.apache.commons.jcs3.log;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * This is a wrapper around the <code>java.util.logging.Logger</code> implementing our own
 * <code>Log</code> interface.
 * <p>
 * This is the mapping of the log levels
 * </p>
 * <pre>
 * Java Level   Log Level
 * SEVERE       FATAL
 * SEVERE       ERROR
 * WARNING      WARN
 * INFO         INFO
 * FINE         DEBUG
 * FINER        TRACE
 * </pre>
 */
public class JulLogAdapter implements Log
{
    private final Logger logger;

    /**
     * Construct a JUL Logger wrapper
     *
     * @param logger the JUL Logger
     */
    public JulLogAdapter(final Logger logger)
    {
        this.logger = logger;
    }

    private void log(final Level level, final String message)
    {
        if (logger.isLoggable(level))
        {
            logger.logp(level, logger.getName(), "", message);
        }
    }

    private void log(final Level level, final Object message)
    {
        if (logger.isLoggable(level))
        {
            if (message instanceof Throwable)
            {
                logger.logp(level, logger.getName(), "", "Exception:", (Throwable) message);
            }
            else
            {
                logger.logp(level, logger.getName(), "",
                        message == null ? null : message.toString());
            }
        }
    }

    private void log(final Level level, final String message, final Throwable t)
    {
        if (logger.isLoggable(level))
        {
            logger.logp(level, logger.getName(), "", message, t);
        }
    }

    private void log(final Level level, final String message, final Object... params)
    {
        if (logger.isLoggable(level))
        {
            final MessageFormatter formatter = new MessageFormatter(message, params);
            if (formatter.hasThrowable())
            {
                logger.logp(level, logger.getName(), "",
                        formatter.getFormattedMessage(), formatter.getThrowable());
            }
            else
            {
                logger.logp(level, logger.getName(), "",
                        formatter.getFormattedMessage());
            }
        }
    }

    private void log(final Level level, final String message, final Supplier<?>... paramSuppliers)
    {
        if (logger.isLoggable(level))
        {
            final MessageFormatter formatter = new MessageFormatter(message, paramSuppliers);
            if (formatter.hasThrowable())
            {
                logger.logp(level, logger.getName(), "",
                        formatter.getFormattedMessage(), formatter.getThrowable());
            }
            else
            {
                logger.logp(level, logger.getName(), "",
                        formatter.getFormattedMessage());
            }
        }
    }

    /**
     * Logs a message object with the DEBUG level.
     *
     * @param message the message string to log.
     */
    @Override
    public void debug(final String message)
    {
        log(Level.FINE, message);
    }

    /**
     * Logs a message object with the DEBUG level.
     *
     * @param message the message object to log.
     */
    @Override
    public void debug(final Object message)
    {
        log(Level.FINE, message);
    }

    /**
     * Logs a message with parameters at the DEBUG level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void debug(final String message, final Object... params)
    {
        log(Level.FINE, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the DEBUG level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called,
     *        produce the desired log message parameters.
     */
    @Override
    public void debug(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.FINE, message, paramSuppliers);
    }

    /**
     * Logs a message at the DEBUG level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    @Override
    public void debug(final String message, final Throwable t)
    {
        log(Level.FINE, message, t);
    }

    /**
     * Logs a message object with the ERROR level.
     *
     * @param message the message string to log.
     */
    @Override
    public void error(final String message)
    {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a message object with the ERROR level.
     *
     * @param message the message object to log.
     */
    @Override
    public void error(final Object message)
    {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a message with parameters at the ERROR level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void error(final String message, final Object... params)
    {
        log(Level.SEVERE, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the ERROR level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     * @since 2.4
     */
    @Override
    public void error(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.SEVERE, message, paramSuppliers);
    }

    /**
     * Logs a message at the ERROR level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    @Override
    public void error(final String message, final Throwable t)
    {
        log(Level.SEVERE, message, t);
    }

    /**
     * Logs a message object with the FATAL level.
     *
     * @param message the message string to log.
     */
    @Override
    public void fatal(final String message)
    {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a message object with the FATAL level.
     *
     * @param message the message object to log.
     */
    @Override
    public void fatal(final Object message)
    {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a message with parameters at the FATAL level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void fatal(final String message, final Object... params)
    {
        log(Level.SEVERE, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the FATAL level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce the
     *        desired log message parameters.
     */
    @Override
    public void fatal(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.SEVERE, message, paramSuppliers);
    }

    /**
     * Logs a message at the FATAL level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    @Override
    public void fatal(final String message, final Throwable t)
    {
        log(Level.SEVERE, message, t);
    }

    /**
     * Gets the logger name.
     *
     * @return the logger name.
     */
    @Override
    public String getName()
    {
        return logger.getName();
    }

    /**
     * Logs a message object with the INFO level.
     *
     * @param message the message string to log.
     */
    @Override
    public void info(final String message)
    {
        log(Level.INFO, message);
    }

    /**
     * Logs a message object with the INFO level.
     *
     * @param message the message object to log.
     */
    @Override
    public void info(final Object message)
    {
        log(Level.INFO, message);
    }

    /**
     * Logs a message with parameters at the INFO level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void info(final String message, final Object... params)
    {
        log(Level.INFO, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the INFO level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    @Override
    public void info(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.INFO, message, paramSuppliers);
    }

    /**
     * Logs a message at the INFO level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    @Override
    public void info(final String message, final Throwable t)
    {
        log(Level.INFO, message, t);
    }

    /**
     * Checks whether this Logger is enabled for the DEBUG Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level DEBUG, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isDebugEnabled()
    {
        return logger.isLoggable(Level.FINE);
    }

    /**
     * Checks whether this Logger is enabled for the ERROR Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level ERROR, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isErrorEnabled()
    {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * Checks whether this Logger is enabled for the FATAL Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level FATAL, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isFatalEnabled()
    {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * Checks whether this Logger is enabled for the INFO Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level INFO, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isInfoEnabled()
    {
        return logger.isLoggable(Level.INFO);
    }

    /**
     * Checks whether this Logger is enabled for the TRACE level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level TRACE, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isTraceEnabled()
    {
        return logger.isLoggable(Level.FINER);
    }

    /**
     * Checks whether this Logger is enabled for the WARN Level.
     *
     * @return boolean - {@code true} if this Logger is enabled for level WARN, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isWarnEnabled()
    {
        return logger.isLoggable(Level.WARNING);
    }

    /**
     * Logs a message object with the TRACE level.
     *
     * @param message the message string to log.
     */
    @Override
    public void trace(final String message)
    {
        log(Level.FINER, message);
    }

    /**
     * Logs a message object with the TRACE level.
     *
     * @param message the message object to log.
     */
    @Override
    public void trace(final Object message)
    {
        log(Level.FINER, message);
    }

    /**
     * Logs a message with parameters at the TRACE level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void trace(final String message, final Object... params)
    {
        log(Level.FINER, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the TRACE level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    @Override
    public void trace(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.FINER, message, paramSuppliers);
    }

    /**
     * Logs a message at the TRACE level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     * @see #debug(String)
     */
    @Override
    public void trace(final String message, final Throwable t)
    {
        log(Level.FINER, message, t);
    }

    /**
     * Logs a message object with the WARN level.
     *
     * @param message the message string to log.
     */
    @Override
    public void warn(final String message)
    {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message object with the WARN level.
     *
     * @param message the message object to log.
     */
    @Override
    public void warn(final Object message)
    {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message with parameters at the WARN level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     */
    @Override
    public void warn(final String message, final Object... params)
    {
        log(Level.WARNING, message, params);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the
     * logging level is the WARN level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce
     *        the desired log message parameters.
     */
    @Override
    public void warn(final String message, final Supplier<?>... paramSuppliers)
    {
        log(Level.WARNING, message, paramSuppliers);
    }

    /**
     * Logs a message at the WARN level including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
    @Override
    public void warn(final String message, final Throwable t)
    {
        log(Level.WARNING, message, t);
    }
}