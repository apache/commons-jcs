package org.apache.commons.jcs.auxiliary.disk.jdbc;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** This is just a helper util. */
public class JDBCDiskCachePoolAccessFactory
{
    /** The local logger. */
    private static final Log log = LogFactory.getLog( JDBCDiskCachePoolAccessFactory.class );

    /**
     * Creates a JDBCDiskCachePoolAccess object from the JDBCDiskCachePoolAccessAttributes. This is
     * used by the connection pool manager.
     * <p>
     * @param poolAttributes
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( JDBCDiskCachePoolAccessAttributes poolAttributes )
        throws Exception
    {
        return createPoolAccess( poolAttributes.getDriverClassName(), poolAttributes.getPoolName(), poolAttributes
            .getUrl()
            + poolAttributes.getDatabase(), poolAttributes.getUserName(), poolAttributes.getPassword(), poolAttributes
            .getMaxActive() );
    }

    /**
     * Creates a JDBCDiskCachePoolAccess object from the JDBCDiskCacheAttributes. Use this when not
     * using the connection pool manager.
     * <p>
     * @param cattr
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( JDBCDiskCacheAttributes cattr )
        throws Exception
    {
        return createPoolAccess( cattr.getDriverClassName(), cattr.getName(), cattr.getUrl() + cattr.getDatabase(),
                                 cattr.getUserName(), cattr.getPassword(), cattr.getMaxActive() );
    }

    /**
     * Creates a pool access object and registers the driver.
     * <p>
     * @param driverClassName
     * @param poolName
     * @param fullURL = (url + database)
     * @param userName
     * @param password
     * @param maxActive
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( String driverClassName, String poolName, String fullURL,
                                                            String userName, String password, int maxActive )
        throws Exception
    {
        JDBCDiskCachePoolAccess poolAccess = null;

        try
        {
            // com.mysql.jdbc.Driver
            Class.forName( driverClassName );
        }
        catch ( ClassNotFoundException e )
        {
            log.error( "Couldn't find class for driver [" + driverClassName + "]", e );
        }

        poolAccess = new JDBCDiskCachePoolAccess( poolName );

        poolAccess.setupDriver( fullURL, userName, password, maxActive );

        poolAccess.logDriverStats();

        if ( log.isInfoEnabled() )
        {
            log.info( "Created: " + poolAccess );
        }

        return poolAccess;
    }
}
