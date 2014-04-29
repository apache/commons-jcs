package org.apache.commons.jcs.auxiliary.disk.jdbc.mysql;

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

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.disk.jdbc.JDBCDiskCachePoolAccessFactory;
import org.apache.commons.jcs.auxiliary.disk.jdbc.TableState;

/**
 * Hand run tests for the MySQL table optimizer.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLTableOptimizerManualTester
    extends TestCase
{
    /**
     * Run the optimization against live a table.
     * <p>
     * @throws Exception
     */
    public void testBasicOptimization()
        throws Exception
    {
        // SETUP
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        attributes.setUserName( "java" );
        attributes.setPassword( "letmein" );
        attributes.setUrl( "jdbc:mysql://10.19.98.43:3306/flight_option_cache" );
        attributes.setDriverClassName( "com.mysql.jdbc.Driver" );
        String tableName = "JCS_STORE_FLIGHT_OPTION_ITINERARY";
        attributes.setTableName( tableName );
        TableState tableState = new TableState( tableName );

        MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState, JDBCDiskCachePoolAccessFactory
            .createPoolAccess( attributes ) );

        // DO WORK
        optimizer.optimizeTable();
    }

    /**
     * Run the optimization against live a table.
     * <p>
     * @throws Exception
     */
    public void testBasicOptimizationUnknownTable()
        throws Exception
    {
        // SETUP
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        attributes.setUserName( "java" );
        attributes.setPassword( "letmein" );
        attributes.setUrl( "jdbc:mysql://10.19.98.43:3306/flight_option_cache" );
        attributes.setDriverClassName( "com.mysql.jdbc.Driver" );
        String tableName = "DOESNTEXIST";
        attributes.setTableName( tableName );
        TableState tableState = new TableState( tableName );

        MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState, JDBCDiskCachePoolAccessFactory
            .createPoolAccess( attributes ) );

        // DO WORK
        optimizer.optimizeTable();
    }
}
