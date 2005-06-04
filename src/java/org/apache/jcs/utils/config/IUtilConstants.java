package org.apache.jcs.utils.config;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Interface
 *  
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
     */
    final static class Config
    {
        private final static Log log = LogFactory.getLog( Config.class );

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
        private Config()
        {
        }
    }
}
