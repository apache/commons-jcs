package org.apache.commons.jcs.utils.key;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.jcs.utils.timing.SleepUtil;

/**
 * Tests for the Key Generator Util.
 */
public class KeyGeneratorUtilUnitTest
    extends TestCase
{
    private String getDddHHmm( Date d )
    {
        SimpleDateFormat sdf = new SimpleDateFormat(KeyGeneratorUtil.dddHHmmFormat);
        return sdf.format( d );
    }

    /**
     * Creates a query id. Verify that we lead with the system lead.
     */
    public void testGetQueryId_SytemLeadNumber_Good()
    {
        // SETUP
        String lead = "9";
        System.setProperty( KeyGeneratorUtil.KEY_LEAD_NUMBER_PROPERTY_NAME, lead );
        // have to force this since it might have already been loaded.
        KeyGeneratorUtil.setLeadFromSystemProperty();
        String expectedDate = getDddHHmm( new Date() );

        // DO WORK
        String result = KeyGeneratorUtil.generateRequestId();

        // VERIFY
        assertNotNull( "We should have a query id.", result );
        assertTrue( "Should have the input.", result.indexOf( String.valueOf( expectedDate ) ) != -1 );
        assertEquals( "Wrong lead", lead, result.substring( 0, 1 ) );
    }

    /**
     * Creates a query id. Verify that we lead with the default if the system lead is junk.
     */
    public void testGetQueryId_SytemLeadNumber_Nan()
    {
        // SETUP
        KeyGeneratorUtil.leadNumber = KeyGeneratorUtil.DEFAULT_LEAD_NUMBER;
        String lead = "afdsafsadf";
        System.setProperty( KeyGeneratorUtil.KEY_LEAD_NUMBER_PROPERTY_NAME, lead );
        // have to force this since it might have already been loaded.
        KeyGeneratorUtil.setLeadFromSystemProperty();
        String expectedDate = getDddHHmm( new Date() );

        // DO WORK
        String result = KeyGeneratorUtil.generateRequestId();

        // VERIFY
        assertNotNull( "We should have a query id.", result );
        assertTrue( "Should have the input.", result.indexOf( String.valueOf( expectedDate ) ) != -1 );
        assertEquals( "Wrong lead", String.valueOf( KeyGeneratorUtil.DEFAULT_LEAD_NUMBER ), result.substring( 0, 1 ) );
    }

    /**
     * Creates a query id.
     */
    public void testGetQueryId()
    {
        // SETUP
        String expectedDate = getDddHHmm( new Date() );

        // DO WORK
        String result = KeyGeneratorUtil.generateRequestId();

        // VERIFY
        assertNotNull( "We should have a query id.", result );
        assertTrue( "Should have the input.", result.indexOf( String.valueOf( expectedDate ) ) != -1 );
    }

    /**
     * Verify that we get the right date out. This has just the leading 9 and the date.
     */
    public void testGetDateFromQueryId_exact()
    {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, 13 );
        cal.set( Calendar.MINUTE, 59 );

        String inputDate = getDddHHmm( cal.getTime() );

        // DO WORK
        try
        {
            Date result = KeyGeneratorUtil.getDateOfShopFromRequestId( "9" + inputDate );

            // VERIFY
            Calendar resultCal = Calendar.getInstance();
            resultCal.setTime( result );

            assertEquals( "Wrong day of year.", cal.get( Calendar.DAY_OF_YEAR ), resultCal.get( Calendar.DAY_OF_YEAR ) );
            assertEquals( "Wrong hour.", cal.get( Calendar.HOUR_OF_DAY ), resultCal.get( Calendar.HOUR_OF_DAY ) );
            assertEquals( "Wrong minute.", cal.get( Calendar.MINUTE ), resultCal.get( Calendar.MINUTE ) );
        }
        catch ( ParseException e )
        {
            fail( e.getMessage() );
        }
    }

    /**
     * Verify that we get the right date out. This has the leading 9, the date, and more
     */
    public void testGetDateFromQueryId_over()
    {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, 13 );
        cal.set( Calendar.MINUTE, 59 );

        String inputDate = getDddHHmm( cal.getTime() );

        // DO WORK
        try
        {
            Date result = KeyGeneratorUtil.getDateOfShopFromRequestId( "9" + inputDate + "542143211242134" );

            // VERIFY
            Calendar resultCal = Calendar.getInstance();
            resultCal.setTime( result );

            assertEquals( "Wrong day of year.", cal.get( Calendar.DAY_OF_YEAR ), resultCal.get( Calendar.DAY_OF_YEAR ) );
            assertEquals( "Wrong hour.", cal.get( Calendar.HOUR_OF_DAY ), resultCal.get( Calendar.HOUR_OF_DAY ) );
            assertEquals( "Wrong minute.", cal.get( Calendar.MINUTE ), resultCal.get( Calendar.MINUTE ) );
        }
        catch ( ParseException e )
        {
            fail( e.getMessage() );
        }
    }

    /**
     * Verify that we get an error if it is too small
     */
    public void testGetDateFromQueryId_tooSmall()
    {
        // DO WORK
        try
        {
            KeyGeneratorUtil.getDateOfShopFromRequestId( "9876" );

            fail( "We should have an error." );
        }
        catch ( ParseException e )
        {
            // expected
            assertTrue( "Missing string from error message.", e.getMessage().indexOf( "9876" ) != -1 );
        }
    }

    /**
     * Verify that we get an error if it is null
     */
    public void testGetDateFromQueryId_null()
    {
        // DO WORK
        try
        {
            KeyGeneratorUtil.getDateOfShopFromRequestId( null );

            fail( "We should have an error." );
        }
        catch ( ParseException e )
        {
            // expected
            assertTrue( "Missing string from error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Reset and verify that we get 1;
     */
    public void testGetNextRequestCounter_simple()
    {
        // SETUP
        KeyGeneratorUtil.resetCounter();

        // DO WORK
        int result = KeyGeneratorUtil.getNextRequestCounter();

        // VERIFY
        assertEquals( "Wrong counter value.", 1, result );
    }

    /**
     * Reset, call twice, and verify that we get 2;
     */
    public void testGetNextRequestCounter_twice()
    {
        // SETUP
        KeyGeneratorUtil.resetCounter();

        // DO WORK
        KeyGeneratorUtil.getNextRequestCounter();
        int result = KeyGeneratorUtil.getNextRequestCounter();

        // VERIFY
        assertEquals( "Wrong counter value.", 2, result );
    }

    /**
     * Verify that the counter is reset if we set the interval vey low.
     */
    public void testGetNextRequestCounter_delay()
    {
        // SETUP
        long interval = 10;
        KeyGeneratorUtil.counterResetIntervalMillis = interval;
        KeyGeneratorUtil.resetCounter();

        // DO WORK
        KeyGeneratorUtil.getNextRequestCounter();

        SleepUtil.sleepAtLeast( interval * 2 );

        int result = KeyGeneratorUtil.getNextRequestCounter();

        // VERIFY
        assertEquals( "Wrong counter value.", 1, result );
    }
}
