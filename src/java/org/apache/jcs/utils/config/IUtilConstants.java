
package org.apache.jcs.utils.config;

import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IUtilConstants
{
    /** Description of the Field */
    public final static String ADMIN_USERID = Config.ADMIN_USERID;
    /** Description of the Field */
    public final static String ADMIN_PASSWORD = Config.ADMIN_PASSWORD;


    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
     */
    final static class Config
    {
        private final static Log log =
            LogFactory.getLog( Config.class );

        private final static String ADMIN_USERID;
        private final static String ADMIN_PASSWORD;

        static
        {
            Properties props = new Properties();
            InputStream is = null;
            try
            {
                props.load( is = IUtilConstants.class.getResourceAsStream( "/jcsutils.properties" ) );
            }
            catch ( IOException ex )
            {
                log.warn( ex.getMessage() );
            }
            finally
            {
                if ( is != null )
                {
                    try
                    {
                        is.close();
                    }
                    catch ( IOException ignore )
                    {
                    }
                }
            }
            ADMIN_USERID = props.getProperty( "admin.userid", "admin" );
            ADMIN_PASSWORD = props.getProperty( "admin.password", "system" );
        }


        /** Constructor for the Config object */
        private Config() { }
    }
}
