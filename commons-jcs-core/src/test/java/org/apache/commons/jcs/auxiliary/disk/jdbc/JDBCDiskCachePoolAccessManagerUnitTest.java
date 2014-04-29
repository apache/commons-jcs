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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import junit.framework.TestCase;

/** Unit tests for the pool manager */
public class JDBCDiskCachePoolAccessManagerUnitTest
    extends TestCase
{
    /** Verify that we can configure the object based on the props. */
    public void testConfigurePoolAccessAttributes_Simple()
    {
        // SETUP
        String poolName = "testConfigurePoolAccessAttributes_Simple";

        String url = "adfads";
        String userName = "zvzvz";
        String password = "qewrrewq";
        int maxActive = 10;
        String driverClassName = "org.hsqldb.jdbcDriver";

        Properties props = new Properties();
        String prefix = JDBCDiskCachePoolAccessManager.POOL_CONFIGURATION_PREFIX + poolName
            + JDBCDiskCachePoolAccessManager.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", url );
        props.put( prefix + ".userName", userName );
        props.put( prefix + ".password", password );
        props.put( prefix + ".maxActive", String.valueOf( maxActive ) );
        props.put( prefix + ".driverClassName", driverClassName );

        JDBCDiskCachePoolAccessManager manager = JDBCDiskCachePoolAccessManager.getInstance( props );
        // in case another test has initilized this. See: JCS-114, JCS-115
        manager.setProps( props );

        // DO WORK
        JDBCDiskCachePoolAccessAttributes result = manager.configurePoolAccessAttributes( poolName );

        // VERIFY
        assertEquals( "Wrong url value", url, result.getUrl() );
        assertEquals( "Wrong userName value", userName, result.getUserName() );
        assertEquals( "Wrong password value", password, result.getPassword() );
        assertEquals( "Wrong maxActive value", maxActive, result.getMaxActive() );
        assertEquals( "Wrong driverClassName value", driverClassName, result.getDriverClassName() );
    }

    /**
     * Verify that we can get access.
     * <p>
     * @throws Exception
     */
    public void testGetJDBCDiskCachePoolAccess_Simple()
        throws Exception
    {
        // SETUP
        String poolName = "testGetJDBCDiskCachePoolAccess_Simple";

        String url = "jdbc:hsqldb:";
        String userName = "sa";
        String password = "";
        int maxActive = 10;
        String driverClassName = "org.hsqldb.jdbcDriver";

        Properties props = new Properties();
        String prefix = JDBCDiskCachePoolAccessManager.POOL_CONFIGURATION_PREFIX + poolName
            + JDBCDiskCachePoolAccessManager.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", url );
        props.put( prefix + ".userName", userName );
        props.put( prefix + ".password", password );
        props.put( prefix + ".maxActive", String.valueOf( maxActive ) );
        props.put( prefix + ".driverClassName", driverClassName );

        JDBCDiskCachePoolAccessManager manager = JDBCDiskCachePoolAccessManager.getInstance( props );
        // in case another test has initilized this. See: JCS-114, JCS-115
        manager.setProps( props );

        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        String database = rafroot + "/cache_hsql_db";

        new org.hsqldb.jdbcDriver();
        Class.forName( driverClassName ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, userName, password );
        HsqlSetupTableUtil.setupTABLE( cConn, "JCSTESTTABLE_ACCESS" );

        // DO WORK
        JDBCDiskCachePoolAccess result = manager.getJDBCDiskCachePoolAccess( poolName );

        // VERIFY
        assertNotNull( "Should have an access class", result );
    }
}
