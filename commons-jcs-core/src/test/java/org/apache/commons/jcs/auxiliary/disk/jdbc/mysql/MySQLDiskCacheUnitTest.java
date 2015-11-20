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

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.disk.jdbc.TableState;
import org.apache.commons.jcs.auxiliary.disk.jdbc.dsfactory.SharedPoolDataSourceFactory;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

/**
 * Simple tests for the MySQLDisk Cache.
 * <p>
 * We will probably need to setup an hsql behind this, to test some of the pass through methods.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCacheUnitTest
    extends TestCase
{
    /**
     * Verify that we simply return null on get if an optimization is in
     * progress and the cache is configured to balk on optimization.
     * <p>
     * This is a bit tricky since we don't want to have to have a mysql instance
     * running. Right now this doesn't really test much
     * @throws SQLException
     */
    public void testBalkOnGet() throws SQLException
    {
        // SETUP
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        String tableName = "JCS_TEST";
        // Just use something that exists
        attributes.setDriverClassName( "org.hsqldb.jdbcDriver" );
        attributes.setTableName( tableName );
        attributes.setBalkDuringOptimization( true );
        SharedPoolDataSourceFactory dsFactory = new SharedPoolDataSourceFactory();
        dsFactory.initialize(attributes);

        TableState tableState = new TableState( tableName );
        tableState.setState( TableState.OPTIMIZATION_RUNNING );

        MySQLDiskCache<String, String> cache = new MySQLDiskCache<String, String>( attributes, dsFactory, tableState,
        		CompositeCacheManager.getUnconfiguredInstance() );

        // DO WORK
        Object result = cache.processGet( "myKey" );

        // VERIFY
        assertNull( "The result should be null", result );
    }
}
