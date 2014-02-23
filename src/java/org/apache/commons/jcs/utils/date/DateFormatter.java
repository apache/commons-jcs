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

import java.text.ParseException;
import java.util.Date;

/**
 * This makes standard formatted dates.
 * <p>
 * This is used by the KeyGenerationUtil.
 */
public final class DateFormatter
{
    /** static methods */
    private DateFormatter()
    {
        // no instances
    }

    /** DDDHHmm */
    private static final String dddHHmmFormat = "DDDHHmm";

    /** DDDHHmmss */
    private static final String dddHHmmssFormat = "DDDHHmmss";

    /** dddHHmmFormatter */
    private static final ThreadSafeSimpleDateFormat dddHHmmFormatter = new ThreadSafeSimpleDateFormat( dddHHmmFormat );

    /** dddHHmmssFormatter */
    private static final ThreadSafeSimpleDateFormat dddHHmmssFormatter = new ThreadSafeSimpleDateFormat( dddHHmmssFormat );


    /**
     * Takes string that look like 20051017
     * <p>
     * @param in in
     * @return Date for the string, if the input is null, null is returned.
     * @throws ParseException ParseException
     */
    public static Date parseFormattedStringDddHHmm( String in )
        throws ParseException
    {
        Date retval = null;
        if ( in != null )
        {
            retval = dddHHmmFormatter.parse( in );
        }
        return retval;
    }

    /**
     * gets a date formatted in dddHHmm
     * <p>
     * @param in date
     * @return date as string
     */
    public static String getDddHHmm( Date in )
    {
        String retval = null;
        if ( in != null )
        {
            retval = dddHHmmFormatter.format( in );
        }
        return retval;
    }

    /**
     * Takes string that look like 209123934
     * <p>
     * @param in in
     * @return Date for the string, if the input is null, null is returned.
     * @throws ParseException ParseException
     */
    public static Date parseFormattedStringDddHHmmss( String in )
        throws ParseException
    {
        Date retval = null;
        if ( in != null )
        {
            retval = dddHHmmssFormatter.parse( in );
        }
        return retval;
    }

    /**
     * gets a date formatted in yyyymmddss
     * <p>
     * @param in date
     * @return date as string
     */
    public static String getDddHHmmss( Date in )
    {
        String retval = null;
        if ( in != null )
        {
            retval = dddHHmmssFormatter.format( in );
        }
        return retval;
    }
}
