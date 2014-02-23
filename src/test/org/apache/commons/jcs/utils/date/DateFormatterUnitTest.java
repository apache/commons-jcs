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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

/** Simple tests for the date formatter utility. */
public class DateFormatterUnitTest
    extends TestCase
{
    /**
     * Output a date into a String like 3591010.
     */
    public void testGetDddHHmm()
    {
        // SETUP
        Calendar c = Calendar.getInstance();
        c.set( Calendar.DATE, 25 );
        c.set( Calendar.MONTH, Calendar.DECEMBER );
        c.set( Calendar.YEAR, 2005 );
        c.set( Calendar.HOUR_OF_DAY, 10 );
        c.set( Calendar.MINUTE, 10 );

        // DO WORK
        String formatted = DateFormatter.getDddHHmm( c.getTime() );

        // VERIFY
        assertNotNull( "Missing formatted date", formatted );
        assertEquals( "Incorrectly formatted date", "3591010", formatted.toUpperCase() );
    }

    /**
     * Verify that we can get a date from a string
     * @throws Exception on error
     */
    public void testParseFormattedStringDddHHmm()
        throws Exception
    {
        // SETUP
        String formatted = "3591017";

        // DO WORK
        Date date = DateFormatter.parseFormattedStringDddHHmm( formatted );

        // VERIFY
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        assertEquals( "Month is wrong", 12, calendar.get( Calendar.MONTH ) + 1 );
        assertEquals( "Date is wrong", 25, calendar.get( Calendar.DATE ) );
        assertEquals( "Hour is wrong", 10, calendar.get( Calendar.HOUR ) );
        assertEquals( "Minute is wrong", 17, calendar.get( Calendar.MINUTE ) );
    }

    /**
     * Output a date into a String like 359101001.
     */
    public void testGetDddHHmmss()
    {
        // SETUP
        Calendar c = Calendar.getInstance();
        c.set( Calendar.DATE, 25 );
        c.set( Calendar.MONTH, Calendar.DECEMBER );
        c.set( Calendar.YEAR, 2005 );
        c.set( Calendar.HOUR_OF_DAY, 10 );
        c.set( Calendar.MINUTE, 10 );
        c.set( Calendar.SECOND, 01 );

        // DO WORK
        String formatted = DateFormatter.getDddHHmmss( c.getTime() );

        // VERIFY
        assertNotNull( "Missing formatted date", formatted );
        assertEquals( "Incorrectly formatted date", "359101001", formatted.toUpperCase() );
    }

    /**
     * Verify that we can get a date from a string
     * @throws Exception on error
     */
    public void testParseFormattedStringDddHHmmss()
        throws Exception
    {
        String formatted = "359101701";

        // DO WORK
        Date date = DateFormatter.parseFormattedStringDddHHmmss( formatted );

        // VERIFY
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        assertEquals( "Month is wrong", 12, calendar.get( Calendar.MONTH ) + 1 );
        assertEquals( "Date is wrong", 25, calendar.get( Calendar.DATE ) );
        assertEquals( "Hour is wrong", 10, calendar.get( Calendar.HOUR ) );
        assertEquals( "Minute is wrong", 17, calendar.get( Calendar.MINUTE ) );
        assertEquals( "Second is wrong", 1, calendar.get( Calendar.SECOND ) );
    }
}
