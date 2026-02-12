package org.apache.commons.jcs4.auxiliary.disk.jdbc.mysql;

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

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.jcs4.auxiliary.disk.jdbc.JDBCDiskCache;
import org.apache.commons.jcs4.auxiliary.disk.jdbc.TableState;
import org.apache.commons.jcs4.auxiliary.disk.jdbc.TableState.TableStateType;
import org.apache.commons.jcs4.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.log.Log;

/**
 * The MySQLDiskCache extends the core JDBCDiskCache.
 * <p>
 * Although the generic JDBC Disk Cache can be used for MySQL, the MySQL JDBC Disk Cache has
 * additional features, such as table optimization that are particular to MySQL.
 * </p>
 */
public class MySQLDiskCache<K, V>
	extends JDBCDiskCache<K, V>
{
    /** Local logger */
    private static final Log log = Log.getLog( MySQLDiskCache.class );

    /** Config attributes */
    private final MySQLDiskCacheAttributes mySQLDiskCacheAttributes;

    /**
     * Delegates to the super and makes use of the MySQL specific parameters used for scheduled
     * optimization.
     *
     * @param attributes the configuration object for this cache
     * @param dsFactory the DataSourceFactory for this cache
     * @param tableState an object to track table operations
     * @throws SQLException if the pool access could not be set up
     */
    public MySQLDiskCache( final MySQLDiskCacheAttributes attributes, final DataSourceFactory dsFactory,
    		final TableState tableState) throws SQLException
    {
        super( attributes, dsFactory, tableState);

        mySQLDiskCacheAttributes = attributes;

        log.debug( "MySQLDiskCacheAttributes = {0}", attributes );
    }

    /**
     * Removed the expired. (now - create time) &gt; max life seconds * 1000
     * <p>
     * If we are currently optimizing, then this method will balk and do nothing.
     * <p>
     * TODO consider blocking and trying again.
     *
     * @return the number deleted
     */
    @Override
    protected int deleteExpired()
    {
        if (getTableState().getState() == TableStateType.OPTIMIZATION_RUNNING &&
            this.mySQLDiskCacheAttributes.isBalkDuringOptimization())
        {
            return -1;
        }
        return super.deleteExpired();
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently optimizing, then this
     * method will balk and return null.
     *
     * @param key Key to locate value for.
     * @return An object matching key, or null.
     */
    @Override
    protected ICacheElement<K, V> processGet( final K key )
    {
        if (getTableState().getState() == TableStateType.OPTIMIZATION_RUNNING &&
            this.mySQLDiskCacheAttributes.isBalkDuringOptimization())
        {
            return null;
        }
        return super.processGet( key );
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently optimizing, then this
     * method will balk and return null.
     *
     * @param pattern used for like query.
     * @return An object matching key, or null.
     */
    @Override
    protected Map<K, ICacheElement<K, V>> processGetMatching( final String pattern )
    {
        if (getTableState().getState() == TableStateType.OPTIMIZATION_RUNNING &&
            this.mySQLDiskCacheAttributes.isBalkDuringOptimization())
        {
            return null;
        }
        return super.processGetMatching( pattern );
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently optimizing, then this
     * method will balk and do nothing.
     *
     * @param element
     */
    @Override
    protected void processUpdate( final ICacheElement<K, V> element )
    {
        if (getTableState().getState() == TableStateType.OPTIMIZATION_RUNNING &&
            this.mySQLDiskCacheAttributes.isBalkDuringOptimization())
        {
            return;
        }
        super.processUpdate( element );
    }
}
