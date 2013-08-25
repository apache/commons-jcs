package org.apache.commons.jcs;

import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/** Utility for testing log messages. */
public class TestLogConfigurationUtil
{
    /**
     * Configures a logger for the given name. This allows us to check the log output.
     * <p>
     * @param stringWriter string writer
     * @param loggerName logger name
     */
    public static void configureLogger( StringWriter stringWriter, String loggerName )
    {
        Logger logger = Logger.getLogger( loggerName );
        WriterAppender appender = new WriterAppender( new PatternLayout(), stringWriter );

        logger.addAppender( appender );
        logger.setLevel( Level.DEBUG );
    }  
}
