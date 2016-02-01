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
import java.sql.SQLException;
import java.sql.Statement;

/** Can use this to setup a table. */
public class HsqlSetupTableUtil
{
    /**
     * SETUP a TABLE FOR CACHE testing
     * <p>
     * @param cConn
     * @param tableName
     *
     * @throws SQLException if database problems occur
     */
    public static void setupTABLE( Connection cConn, String tableName ) throws SQLException
    {
        boolean newT = true;

        StringBuilder createSql = new StringBuilder();
        createSql.append( "CREATE CACHED TABLE ").append( tableName );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           TIMESTAMP, " );
        createSql.append( "UPDATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
        createSql.append( "SYSTEM_EXPIRE_TIME_SECONDS      BIGINT, " );
        createSql.append( "IS_ETERNAL            CHAR(1), " );
        createSql.append( "PRIMARY KEY (CACHE_KEY, REGION) " );
        createSql.append( ");" );

        Statement sStatement = cConn.createStatement();

        try
        {
            sStatement.execute( createSql.toString() );
        }
        catch ( SQLException e )
        {
            if ("23000".equals(e.getSQLState()) || "S0001".equals(e.getSQLState()))
            {
                newT = false;
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            sStatement.close();
        }

        if ( newT )
        {
            String setupData[] = { "create index iKEY on " + tableName + " (CACHE_KEY, REGION)" };
            Statement iStatement = cConn.createStatement();

            try
            {
                for ( int i = 0; i < setupData.length; i++ )
                {
                    try
                    {
                        iStatement.execute( setupData[i] );
                    }
                    catch ( SQLException e )
                    {
                        System.out.println( "Exception: " + e );
                    }
                }
            }
            finally
            {
                iStatement.close();
            }
        }
    }
}
