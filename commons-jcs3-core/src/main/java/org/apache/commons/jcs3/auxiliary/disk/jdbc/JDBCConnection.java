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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Encapsulates connection status tracking and allows AutoClosing within JCS.
 */
public class JDBCConnection implements AutoCloseable
{
    /** Has this transaction already been committed? */
    private boolean committed;

    /** Save state of auto-commit */
    private boolean autoCommit;

    /** The wrapped connection instance */
    private Connection connection;

    /**
     * Constructor
     * @param con Connection object
     * @throws SQLException if a database error occurs
     */
    public JDBCConnection(Connection con) throws SQLException
    {
        this.committed = false;
        this.autoCommit = con.getAutoCommit();
        this.connection = con;
        this.connection.setAutoCommit(false);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return connection.prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#commit()
     */
    public void commit() throws SQLException
    {
        connection.commit();
        this.committed = true;
    }

    /**
     * @see java.sql.Connection#rollback()
     */
    public void rollback() throws SQLException
    {
        connection.rollback();
        this.committed = false;
    }

    /**
     * @see java.sql.Connection#close()
     */
    @Override
    public void close() throws SQLException
    {
        if (!this.committed)
        {
            connection.rollback();
        }

        connection.setAutoCommit(this.autoCommit);

        // Justin Case
        if (!connection.isClosed())
        {
            connection.close();
        }
    }

    /**
     * @see java.sql.Connection#getMetaData()
     */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return connection.getMetaData();
    }
}
