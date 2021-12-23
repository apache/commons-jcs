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

/**
 * Parses the very simple schedule format.
 * <p>
 * @author Aaron Smuts
 */
public class ScheduleParser
{
    /**
     * For each date time that is separated by a comma in the
     * OptimizationSchedule, create a date and add it to an array of dates.
     * <p>
     * @param schedule
     * @return Date[]
     * @throws ParseException
     */
    public static Date[] createDatesForSchedule( final String schedule )
        throws ParseException
    {
        if ( schedule == null )
        {
            throw new ParseException( "Cannot create schedules for a null String.", 0 );
        }

        final String timeStrings[] = schedule.split("\\s*,\\s*");
        final Date[] dates = new Date[timeStrings.length];
        int cnt = 0;
        for (String time : timeStrings)
        {
            dates[cnt++] = getDateForSchedule(time);
        }
        return dates;
    }

    /**
     * For a single string it creates a date that is the next time this hh:mm:ss
     * combo will be seen.
     * <p>
     * @param startTime
     * @return Date
     * @throws ParseException
     */
    public static Date getDateForSchedule( final String startTime )
        throws ParseException
    {
        if ( startTime == null )
        {
            throw new ParseException( "Cannot create date for a null String.", 0 );
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final Date date = sdf.parse(startTime);
        final Calendar cal = Calendar.getInstance();
        // This will result in a date of 1/1/1970
        cal.setTime(date);

        final Calendar now = Calendar.getInstance();
        cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        // if the date is less than now, add a day.
        if ( cal.before( now ) )
        {
            cal.add( Calendar.DAY_OF_MONTH, 1 );
        }

        return cal.getTime();
    }
}
