package org.apache.commons.jcs3.auxiliary.disk.jdbc;

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.jcs3.auxiliary.disk.jdbc.hsql.HSQLDiskCacheFactory;

/** Can use this to setup a table. */
public final class HsqlSetupUtil
{
    private static final class HSQLDiskCacheFactoryHelper extends HSQLDiskCacheFactory
    {
        @Override
        protected synchronized void setupTable(final Connection cConn, final String tableName) throws SQLException
        {
            super.setupTable(cConn, tableName);
        }
    }

    private static HSQLDiskCacheFactoryHelper factory = new HSQLDiskCacheFactoryHelper();

    /**
     * Connect to the database and return a connection.
     *
     * @param testProperties test database properties
     * @param testDBName name of the test database
     *
     * @return a connection to the database
     *
     * @throws Exception
     */
    public static Connection getTestDatabaseConnection(final Properties testProperties, final String testDBName)
        throws Exception
    {
        System.setProperty( "hsqldb.cache_scale", "8" );

        final String rafroot = "target/";
        final String driver = testProperties.getProperty("driver", "org.hsqldb.jdbcDriver");
        final String url = testProperties.getProperty("url", "jdbc:hsqldb:");
        final String database = testProperties.getProperty("database", rafroot + testDBName);
        final String user = testProperties.getProperty("user", "sa");
        final String password = testProperties.getProperty("password", "");

        Class.forName(driver).getDeclaredConstructor().newInstance();
        return DriverManager.getConnection(url + database, user, password);
    }

    /**
     * Sets up a table for cache testing
     * <p>
     * @param cConn
     * @param tableName
     *
     * @throws SQLException if database problems occur
     */
    public static void setupTable( final Connection cConn, final String tableName ) throws SQLException
    {
        factory.setupTable(cConn, tableName);
    }
}
