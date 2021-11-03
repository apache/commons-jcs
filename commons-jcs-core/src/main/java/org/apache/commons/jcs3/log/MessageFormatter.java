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

import java.text.MessageFormat;
import java.util.IllegalFormatException;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Handles messages that consist of a format string conforming to
 * java.text.MessageFormat. (Borrowed from log4j2)
 */
public class MessageFormatter
{
    private final String messagePattern;
    private final transient Object[] parameters;
    private transient String formattedMessage;
    private transient Throwable throwable;

    /**
     * Constructs a message formatter.
     *
     * @param messagePattern
     *            the pattern for this message format
     * @param parameters
     *            The objects to format
     */
    public MessageFormatter(final String messagePattern, final Object... parameters)
    {
        this.messagePattern = messagePattern;
        this.parameters = parameters;
        final int length = parameters == null ? 0 : parameters.length;
        if (length > 0 && parameters[length - 1] instanceof Throwable)
        {
            this.throwable = (Throwable) parameters[length - 1];
        }
    }

    /**
     * Constructs a message formatter.
     *
     * @param messagePattern
     *            the pattern for this message format
     * @param paramSuppliers
     *            An array of functions, which when called, produce the desired
     *            log message parameters.
     */
    public MessageFormatter(final String messagePattern, final Supplier<?>... paramSuppliers)
    {
        this.messagePattern = messagePattern;
        this.parameters = Stream.of(paramSuppliers)
                            .map(Supplier::get)
                            .toArray();

        final int length = parameters.length;
        if (length > 0 && parameters[length - 1] instanceof Throwable)
        {
            this.throwable = (Throwable) parameters[length - 1];
        }
    }

    /**
     * Returns the formatted message.
     *
     * @return the formatted message.
     */
    public String getFormattedMessage()
    {
        if (formattedMessage == null)
        {
            formattedMessage = formatMessage(messagePattern, parameters);
        }
        return formattedMessage;
    }

    protected String formatMessage(final String msgPattern, final Object... args)
    {
        try
        {
            final MessageFormat temp = new MessageFormat(msgPattern);
            return temp.format(args);
        }
        catch (final IllegalFormatException ife)
        {
            return msgPattern;
        }
    }

    @Override
    public String toString()
    {
        return getFormattedMessage();
    }

    /**
     * Return the throwable passed to the Message.
     *
     * @return the Throwable.
     */
    public Throwable getThrowable()
    {
        return throwable;
    }

    /**
     * Return true, if the parameters list contains a Throwable.
     *
     * @return true, if the parameters list contains a Throwable.
     */
    public boolean hasThrowable()
    {
        return throwable != null;
    }
}
