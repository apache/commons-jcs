package org.apache.commons.jcs3.auxiliary.disk.jdbc.mysql;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;

import org.apache.commons.jcs3.auxiliary.disk.jdbc.TableState;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.SharedPoolDataSourceFactory;
import org.junit.jupiter.api.Test;

/**
 * Simple tests for the MySQLDisk Cache.
 * <p>
 * We will probably need to setup an hsql behind this, to test some of the pass through methods.
 * </p>
 */
class MySQLDiskCacheUnitTest
{
    /**
     * Verify that we simply return null on get if an optimization is in
     * progress and the cache is configured to balk on optimization.
     * <p>
     * This is a bit tricky since we don't want to have to have a mysql instance
     * running. Right now this doesn't really test much
     * </p>
     * @throws SQLException
     */
    @Test
    void testBalkOnGet()
        throws SQLException
    {
        // SETUP
        final MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        final String tableName = "JCS_TEST";
        // Just use something that exists
        attributes.setDriverClassName( "org.hsqldb.jdbcDriver" );
        attributes.setTableName( tableName );
        attributes.setBalkDuringOptimization( true );
        final SharedPoolDataSourceFactory dsFactory = new SharedPoolDataSourceFactory();
        dsFactory.initialize(attributes);

        final TableState tableState = new TableState( tableName );
        tableState.setState( TableState.OPTIMIZATION_RUNNING );

        final MySQLDiskCache<String, String> cache = new MySQLDiskCache<>(attributes, dsFactory, tableState);

        // DO WORK
        final Object result = cache.processGet( "myKey" );

        // VERIFY
        assertNull( result, "The result should be null" );
    }
}
