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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the removal functionality. */
class JDBCDiskCacheRemovalUnitTest
{
    /** Db name -- set in system props */
    private final String databaseName = "JCS_STORE_REMOVAL";

    /**
     * Test setup
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        System.setProperty( "DATABASE_NAME", databaseName );
        JCS.setConfigFilename( "/TestJDBCDiskCacheRemoval.ccf" );
        try (Connection con = HsqlSetupUtil.getTestDatabaseConnection(new Properties(), getClass().getSimpleName()))
        {
            HsqlSetupUtil.setupTable(con, databaseName);
        }
    }

    /**
     * Verify the fix for BUG JCS-20
     * <p>
     * Setup an hsql db. Add an item. Remove using partial key.
     * @throws Exception
     */
    @Test
    void testPartialKeyRemoval_Good()
        throws Exception
    {
        // SETUP
        final String keyPart1 = "part1";
        final String keyPart2 = "part2";
        final String region = "testCache1";
        final String data = "adfadsfasfddsafasasd";

        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // DO WORK
        jcs.put( keyPart1 + ":" + keyPart2, data );
        Thread.sleep( 1000 );

        // VERIFY
        final String resultBeforeRemove = jcs.get( keyPart1 + ":" + keyPart2 );
        assertEquals( data, resultBeforeRemove, "Wrong result" );

        jcs.remove( keyPart1 + ":" );
        final String resultAfterRemove = jcs.get( keyPart1 + ":" + keyPart2 );
        assertNull( resultAfterRemove, "Should not have a result after removal." );

//        System.out.println( jcs.getStats() );
    }
}
