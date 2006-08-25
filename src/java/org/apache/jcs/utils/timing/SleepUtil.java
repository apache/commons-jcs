package org.apache.jcs.utils.timing;

/**
 * Utility methods to help deal with thread issues.
 */
public class SleepUtil
{
    /**
     * Sleep for a specified duration in milliseconds. This method is a
     * platform-specific workaround for Windows due to its inability to resolve
     * durations of time less than approximately 10 - 16 ms.
     * <p>
     * @param milliseconds the number of milliseconds to sleep
     */
    public static void sleepAtLeast( long milliseconds )
    {
        long endTime = System.currentTimeMillis() + milliseconds;

        while ( System.currentTimeMillis() <= endTime )
        {
            try
            {
                Thread.sleep( milliseconds );
            }
            catch ( InterruptedException e )
            {
                // TODO - Do something here?
            }
        }
    }
}
