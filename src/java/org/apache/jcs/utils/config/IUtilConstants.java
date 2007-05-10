package org.apache.jcs.utils.config;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This can hold some constants used by various utilities. We should probably
 * just get rid of this.
 * <p>
 * It loads properties from jcsutil.properties on initialization.
 */
public interface IUtilConstants
{
    /** Description of the Field */
    public final static String ADMIN_USERID = Config.ADMIN_USERID;

    /** Description of the Field */
    public final static String ADMIN_PASSWORD = Config.ADMIN_PASSWORD;

    /**
     * Description of the Class
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
                        // swallow
                    }
                }
            }
            ADMIN_USERID = props.getProperty( "admin.userid", "admin" );
            ADMIN_PASSWORD = props.getProperty( "admin.password", "system" );
        }

        /** No instances please. */
        private Config()
        {
            super();
        }
    }
}
