package org.apache.commons.jcs.utils.key;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.jcs.utils.date.DateFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This can create and parse request ids.  You can use it to generate keys that contain a create time stamp.
 * <p>
 * You can set a system property called "KEY_LEAD_NUMBER" to override the lead number. The value
 * must be a simple int from 1 to 9.
 */
public final class KeyGeneratorUtil
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( KeyGeneratorUtil.class );

    /** a temporary counter for generating request ids. */
    private static int requestCounter = 0;

    /** last reset time. */
    private static long lastCounterResetTime = System.currentTimeMillis();

    /** defaults to 2 hours. */
    private static final long DEFAULT_COUNTER_RESET_INTERVAL_MILLIS = 2 * 60 * 60 * 1000;

    /** How often should we reset the counter. */
    protected static long counterResetIntervalMillis = DEFAULT_COUNTER_RESET_INTERVAL_MILLIS;

    /** The size of the data portion. */
    private static final int LENGTH_OF_DATE_STRING = 8;

    /**
     * The name of the system property that can be used to override the default. This allows us to
     * run multiple instance on a machine.
     */
    public static final String KEY_LEAD_NUMBER_PROPERTY_NAME = "KEY_LEAD_NUMBER";

    /** The default lead number. */
    public static final int DEFAULT_LEAD_NUMBER = 3;

    /** We lead with a number so it can be converted to a number. This is the prefix to all ids. */
    protected static int leadNumber = DEFAULT_LEAD_NUMBER;

    static
    {
        setLeadFromSystemProperty();
    }

    /** Sets the lead number from a system property */
    protected static void setLeadFromSystemProperty()
    {
        String leadString = System.getProperty( KEY_LEAD_NUMBER_PROPERTY_NAME, String.valueOf( DEFAULT_LEAD_NUMBER ) );
        if ( log.isInfoEnabled() )
        {
            log.info( "leadString = [" + leadString + "]" );
        }
        try
        {
            leadNumber = Integer.parseInt( leadString );
        }
        catch ( NumberFormatException e )
        {
            log.error( "Problem parsing lead number system property value. [" + leadString + "]", e );
        }
    }

    /**
     * Creates a query id in the format 1001010121712345 where the first 8 digits (10010101) is a 8
     * digit number where: 1 is a padding digit and 001 is the first day of the year. The next 3
     * digits (217) are the decimal representation of the last byte of data in the machine ip (for
     * example 192.168.1.2 will be 002). The remaining digits (ex. 12345) are some unique number.
     * These come from a counter that is reset every 2 hours.
     * <p>
     * @return long
     */
    public static String generateRequestId()
    {
        int counter = getNextRequestCounter();

        Date d = new Date();
        String dateString = DateFormatter.getDddHHmm( d );
        String finalOctetOfIp = org.apache.commons.jcs.utils.net.AddressUtil.obtainFinalThreeDigitsOfAddressAsString();
        String queryId = leadNumber + dateString + finalOctetOfIp + counter;
        return queryId;
    }

    /**
     * This DddHHmm.
     * <p>
     * This has to get the current year and set it, since the source data does not have the year.
     * <p>
     * @param queryId queryId
     * @return now if we can't parse, else the data from the query id.
     * @throws ParseException ParseException
     */
    public static Date getDateOfShopFromRequestId( String queryId )
        throws ParseException
    {
        Date date = null;
        if ( queryId != null )
        {
            if ( queryId.length() >= LENGTH_OF_DATE_STRING )
            {
                Calendar cal = Calendar.getInstance();
                int year = cal.get( Calendar.YEAR );
                int dayOfYear;
                int hour;
                int minute;

                try
                {
                    dayOfYear = Integer.parseInt( queryId.substring( 1, 4 ) );
                    hour = Integer.parseInt( queryId.substring( 4, 6 ) );
                    minute = Integer.parseInt( queryId.substring( 6, 8 ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new ParseException( "Error reading date/hour/minute from input string [" + queryId + "]", 0 );
                }

                cal.set( Calendar.YEAR, year );
                cal.set( Calendar.DAY_OF_YEAR, dayOfYear );
                cal.set( Calendar.HOUR_OF_DAY, hour );
                cal.set( Calendar.MINUTE, minute );
                date = cal.getTime();
            }
            else
            {
                throw new ParseException( "The input string is not long enough [" + queryId + "]", queryId.length() );
            }
        }
        else
        {
            throw new ParseException( "Can't parse a null string.", 0 );
        }
        return date;
    }

    /**
     * Automatically increment and return the request counter. If the last counter reset was more
     * than the interval, reset the counter.
     * <p>
     * @return The incremented count.
     */
    protected static synchronized int getNextRequestCounter()
    {
        long now = System.currentTimeMillis();
        if ( ( now - KeyGeneratorUtil.lastCounterResetTime ) > KeyGeneratorUtil.counterResetIntervalMillis )
        {
            resetCounter();
        }
        return ++requestCounter;
    }

    /** reset the counter and the reset time. */
    protected static synchronized void resetCounter()
    {
        KeyGeneratorUtil.lastCounterResetTime = System.currentTimeMillis();
        KeyGeneratorUtil.requestCounter = 0;
    }
}
