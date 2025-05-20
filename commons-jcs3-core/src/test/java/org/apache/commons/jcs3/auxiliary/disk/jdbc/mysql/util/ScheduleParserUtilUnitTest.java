package org.apache.commons.jcs3.auxiliary.disk.jdbc.mysql.util;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Tests for the schedule parser.
 */
class ScheduleParserUtilUnitTest
{

    /**
     * Verify that we get an exception for a schedule that has a non numeric item.
     */
    @Test
    void testGetDatesMalformedNan()
    {
        try
        {
            final String schedule = "12:34:56,03:51:00,aa:12:12";
            ScheduleParser.createDatesForSchedule( schedule );

            fail( "Should have thrown an exception for a malformed date" );
        }
        catch ( final ParseException e )
        {
            // expected
        }
    }

    /**
     * Verify that we get an exception for a single bad date in a list.
     */
    @Test
    void testGetDatesMalformedNoColon()
    {
        try
        {
            final String schedule = "12:34:56,03:51:00,123234";
            ScheduleParser.createDatesForSchedule( schedule );

            fail( "Should have thrown an exception for a malformed date" );
        }
        catch ( final ParseException e )
        {
            // expected
        }
    }

    /**
     * Verify that we get an exception and not a null pointer for null input.
     */
    @Test
    void testGetDatesWithNullInput()
    {
        try
        {
            ScheduleParser.createDatesForSchedule( null );

            fail( "Should have thrown an exception" );
        }
        catch ( final ParseException e )
        {
            // expected
        }
    }

    /**
     * Verify that we get an exception and not a null pointer for null input.
     */
    @Test
    void testGetDateWithNullInput()
    {
        try
        {
            ScheduleParser.getDateForSchedule( null );

            fail( "Should have thrown an exception" );
        }
        catch ( final ParseException e )
        {
            // expected
        }
    }

    /**
     * Verify that we get one date for one date.
     * @throws ParseException
     */
    @Test
    void testGetsDatesMultiple()
        throws ParseException
    {
        final String schedule = "12:34:56,03:51:00,12:34:12";
        final Date[] dates = ScheduleParser.createDatesForSchedule( schedule );
        //System.out.println( dates );
        assertEquals( 3, dates.length, "Wrong number of dates returned." );
    }

    /**
     * Verify that we get one date for one date.
     * @throws ParseException
     */
    @Test
    void testGetsDatesSingle()
        throws ParseException
    {
        final String schedule = "12:34:56";
        final Date[] dates = ScheduleParser.createDatesForSchedule( schedule );

        assertEquals( 1, dates.length, "Wrong number of dates returned." );
    }
}
