package org.apache.jcs.auxiliary.disk.jdbc.hsql;

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
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCacheManager;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 * This factory should create mysql disk caches.
 * <p>
 * @author Aaron Smuts
 */
public class HSQLDiskCacheFactory
    implements AuxiliaryCacheFactory
{
    private final static Log log = LogFactory.getLog( HSQLDiskCacheFactory.class );

    private String name = "HSQLDiskCacheFactory";

    private Set databases = Collections.synchronizedSet( new HashSet() );

    /**
     * This factory method should create an instance of the mysqlcache.
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes rawAttr, ICompositeCacheManager arg1 )
    {
        JDBCDiskCacheManager mgr = JDBCDiskCacheManager.getInstance( (JDBCDiskCacheAttributes) rawAttr );
        try
        {
            setupDatabase( (JDBCDiskCacheAttributes) rawAttr );
        }
        catch ( Exception e )
        {
            // TODO we may not want to try and get the cache at this point.
            log.error( "Problem setting up database.", e );
        }
        return mgr.getCache( (JDBCDiskCacheAttributes) rawAttr );
    }

    /**
     * The name of the factory.
     */
    public void setName( String nameArg )
    {
        name = nameArg;
    }

    /**
     * Returns the display name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Creates the database if it doesn't exist, registers the driver class,
     * etc.
     * <p>
     * @param attributes
     * @throws Exception
     */
    protected void setupDatabase( JDBCDiskCacheAttributes attributes )
        throws Exception
    {
        if ( attributes == null )
        {
            throw new Exception( "The attributes are null." );
        }

        // url should start with "jdbc:hsqldb:"
        String database = attributes.getUrl() + attributes.getDatabase();

        if ( databases.contains( database ) )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "We already setup database [" + database + "]" );
            }
            return;
        }

        // TODO get this from the attributes.
        System.setProperty( "hsqldb.cache_scale", "8" );

        // "org.hsqldb.jdbcDriver"
        String driver = attributes.getDriverClassName();
        // "sa"
        String user = attributes.getUserName();
        // ""
        String password = attributes.getPassword();

        new org.hsqldb.jdbcDriver();
        try
        {
            Class.forName( driver ).newInstance();

            Connection cConn = DriverManager.getConnection( database, user, password );

            setupTABLE( cConn, attributes.getTableName() );

            if ( log.isInfoEnabled() )
            {
                log.info( "Finished setting up database [" + database + "]" );
            }

            databases.add( database );
        }
        catch ( Exception e )
        {
            log.error( "Fatal problem setting up the database.", e );
        }
    }

    /**
     * SETUP TABLE FOR CACHE
     * <p>
     * @param cConn
     * @param tableName
     */
    private void setupTABLE( Connection cConn, String tableName )
    {
        boolean newT = true;

        // TODO make the cached nature of the table configurable
        StringBuffer createSql = new StringBuffer();
        createSql.append( "CREATE CACHED TABLE " + tableName );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           DATE, " );
        createSql.append( "CREATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
        createSql.append( "SYSTEM_EXPIRE_TIME_SECONDS      BIGINT, " );
        createSql.append( "IS_ETERNAL            CHAR(1), " );
        createSql.append( "PRIMARY KEY (CACHE_KEY, REGION) " );
        createSql.append( ");" );

        Statement sStatement = null;
        try
        {
            sStatement = cConn.createStatement();
        }
        catch ( SQLException e )
        {
            log.error( "problem creating a statement.", e );
        }

        try
        {
            sStatement.executeQuery( createSql.toString() );
            sStatement.close();
        }
        catch ( SQLException e )
        {
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            else
            {
                log.error( "Problem creating table.", e );
            }
        }

        // TODO create an index on SYSTEM_EXPIRE_TIME_SECONDS
        String setupData[] = { "create index iKEY on " + tableName + " (CACHE_KEY, REGION)" };

        if ( newT )
        {
            for ( int i = 1; i < setupData.length; i++ )
            {
                try
                {
                    sStatement.executeQuery( setupData[i] );
                }
                catch ( SQLException e )
                {
                    log.error( "Exception caught when creating index." + e );
                }
            }
        }
    }
}
