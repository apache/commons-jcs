package org.apache.commons.jcs.utils.date;

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

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Thread Safe version of SimpleDateFormat
 * <p>
 * This class simply synchronizes format and parse for SimpleDateFormat.
 */
public class ThreadSafeSimpleDateFormat
    extends SimpleDateFormat
{
    /**
     * Generated Serial Version ID
     */
    private static final long serialVersionUID = -6394173605134585999L;

    /**
     * Empty Constructor
     */
    public ThreadSafeSimpleDateFormat()
    {
        super();
    }

    /**
     * @param pattern the pattern describing the date and time format
     */
    public ThreadSafeSimpleDateFormat( String pattern )
    {
        super( pattern );
    }

    /**
     * @param pattern the pattern describing the date and time format
     * @param lenient leniency option - if false, strictly valid dates are enforced
     */
    public ThreadSafeSimpleDateFormat( String pattern, boolean lenient )
    {
        super( pattern );
        this.setLenient( lenient );
    }

    /**
     * @param pattern the pattern describing the date and time format
     * @param locale the locale whose date format symbols should be used.
     */
    public ThreadSafeSimpleDateFormat( String pattern, Locale locale )
    {
        super( pattern, locale );
    }

    /**
     * @param pattern the pattern describing the date and time format
     * @param formatSymbols the date format symbols to be used for formatting.
     */
    public ThreadSafeSimpleDateFormat( String pattern, DateFormatSymbols formatSymbols )
    {
        super( pattern, formatSymbols );
    }

    /**
     * @param date date
     * @param toAppendTo buffer to append to
     * @param fieldPosition field position
     * @return a string buffer with more data in it
     */
    @Override
    public synchronized StringBuffer format( Date date, StringBuffer toAppendTo, FieldPosition fieldPosition )
    {
        return super.format( date, toAppendTo, fieldPosition );
    }

    /**
     * @param source source
     * @param pos parse position
     * @return date
     */
    @Override
    public synchronized Date parse( String source, ParsePosition pos )
    {
        return super.parse( source, pos );
    }
}
