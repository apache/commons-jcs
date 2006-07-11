package org.apache.jcs.auxiliary.disk.jdbc.mysql.util;

/**
 * This is thrown internally by the schedule parser.
 * <p>
 * @author Aaron Smuts
 */
public class ScheduleFormatException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public ScheduleFormatException( String message )
    {
        super( message );
    }
}
