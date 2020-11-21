package org.apache.commons.jcs3.auxiliary.disk.jdbc.hsql;

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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.JDBCDiskCache;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.JDBCDiskCacheFactory;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This factory should create hsql disk caches.
 * <p>
 * @author Aaron Smuts
 */
public class HSQLDiskCacheFactory
    extends JDBCDiskCacheFactory
{
    /** The logger */
    private static final Log log = LogManager.getLog( HSQLDiskCacheFactory.class );

    /**
     * This factory method should create an instance of the hsqlcache.
     * <p>
     * @param rawAttr
     * @param compositeCacheManager
     * @param cacheEventLogger
     * @param elementSerializer
     * @return JDBCDiskCache
     * @throws SQLException if the creation of the cache instance fails
     */
    @Override
    public <K, V> JDBCDiskCache<K, V> createCache( final AuxiliaryCacheAttributes rawAttr,
			final ICompositeCacheManager compositeCacheManager,
			final ICacheEventLogger cacheEventLogger,
			final IElementSerializer elementSerializer )
			throws SQLException
    {
        // TODO get this from the attributes.
        System.setProperty( "hsqldb.cache_scale", "8" );

        final JDBCDiskCache<K, V> cache = super.createCache(rawAttr, compositeCacheManager,
                cacheEventLogger, elementSerializer);
        setupDatabase( cache.getDataSource(), (JDBCDiskCacheAttributes) rawAttr );

        return cache;
    }

    /**
     * Creates the table if it doesn't exist
     * <p>
     * @param ds Data Source
     * @param attributes Cache region configuration
     * @throws SQLException
     */
    protected void setupDatabase( final DataSource ds, final JDBCDiskCacheAttributes attributes )
        throws SQLException
    {
        try (Connection cConn = ds.getConnection())
        {
            setupTable( cConn, attributes.getTableName() );
            log.info( "Finished setting up table [{0}]", attributes.getTableName());
        }
    }

    /**
     * SETUP TABLE FOR CACHE
     * <p>
     * @param cConn
     * @param tableName
     */
    protected synchronized void setupTable( final Connection cConn, final String tableName ) throws SQLException
    {
        final DatabaseMetaData dmd = cConn.getMetaData();
        final ResultSet result = dmd.getTables(null, null, tableName, null);

        if (!result.next())
        {
            // TODO make the cached nature of the table configurable
            final StringBuilder createSql = new StringBuilder();
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

            try (Statement sStatement = cConn.createStatement())
            {
                sStatement.execute( createSql.toString() );
            }
        }
    }
}
