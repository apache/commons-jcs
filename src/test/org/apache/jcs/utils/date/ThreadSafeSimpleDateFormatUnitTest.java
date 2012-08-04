package org.apache.jcs.utils.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * Multi-threaded tests for SimpleDateFormat.
 */
public class ThreadSafeSimpleDateFormatUnitTest
    extends TestCase
{
    /** log instance */
    private static final Log log = LogFactory.getLog( ThreadSafeSimpleDateFormatUnitTest.class );

    /** date format string */
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd kk:mm:ss:SSS";

    /** test run length */
    private static final int TEST_RUN_LENGTH = 100;

    /** number of threads */
    private static final int NUM_THREADS = 50;

    /** random number generator */
    protected static final Random random = new Random();

    /** number wrong */
    protected int numWrong = 0;

    /** number of loops? */
    protected int numLoops = 0;

    /** run? */
    protected boolean run = false;

    /** a simpledateformat instance */
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_FORMAT_STRING );

    /** a threadsafe simpledateformat instance */
    private ThreadSafeSimpleDateFormat threadSafeSimpleDateFormat = new ThreadSafeSimpleDateFormat( DATE_FORMAT_STRING );

    /** date format */
    protected DateFormat dateFormat;

    /**
     * Tests to make sure that format produces the same string on the thread-safe implementation as
     * it does on the regular one.
     */
    public void testFormat()
    {
        Date now = new Date();
        String regularFormatted = simpleDateFormat.format( now );
        String threadSafeFormatted = threadSafeSimpleDateFormat.format( now );

        assertEquals( "Two formatted strings should be equal", regularFormatted, threadSafeFormatted );
    }

    /**
     * Tests to make sure that parse produces the same date on the thread-safe implementation as it
     * does on the regular one.
     * <p>
     * @throws ParseException if there was a problem parsing the line.
     */
    public void testParse()
        throws ParseException
    {
        String dateString = "2006-01-11 21:03:37:719";
        Date regularDate = simpleDateFormat.parse( dateString );
        Date threadSafeDate = threadSafeSimpleDateFormat.parse( dateString );

        assertEquals( "Two dates should be equal", regularDate, threadSafeDate );
    }

    /**
     * Tests to make sure that the pooled implementation works.
     */
    public void testThreadSafeDateFormatWorks()
    {
        dateFormat = threadSafeSimpleDateFormat;

        // Reset the counter.
        numWrong = 0;
        numLoops = 0;
        run = true;

        for ( int i = 0; i < NUM_THREADS; i++ )
        {
            Thread thread = createSimpleDateFormatTestThread();
            thread.start();
        }

        // Let the threads run a bit...
        SleepUtil.sleepAtLeast( TEST_RUN_LENGTH );

        // Stop the threads.
        run = false;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Thread Safe Test - NumLoops: " + numLoops + " NumWrong: " + numWrong );
        }
        assertEquals( "Thread, we should get no wrongly formatted dates", 0, numWrong );
    }

    /**
     * Creates a test thread that records how many times a date is formatted wrong by creating and
     * formatting random dates.
     * <p>
     * @return an unstarted thread.
     */
    private Thread createSimpleDateFormatTestThread()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Date date = new Date( System.currentTimeMillis() + random.nextInt( 60 * 60 * 1000 ) );

                String properDate = dateFormat.format( date );

                while ( run )
                {
                    numLoops++;
                    // Get an SimpleDateFormat object from the pool, and creates the formatted date.
                    String formattedDate = dateFormat.format( date );
                    if ( !properDate.equals( formattedDate ) )
                    {
                        numWrong++;
                    }
                    SleepUtil.sleepAtLeast( 1 );
                }
            }
        };
        return thread;
    }
}
