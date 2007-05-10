package org.apache.jcs.auxiliary.disk.jdbc.mysql;

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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCache;
import org.apache.jcs.auxiliary.disk.jdbc.TableState;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * The MySQLDiskCache extends the core JDBCDiskCache.
 * <p>
 * Although the generic JDBC Disk Cache can be used for MySQL, the MySQL JDBC Disk Cache has
 * additional features, such as table optimization that are particular to MySQL.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCache
    extends JDBCDiskCache
{
    private static final long serialVersionUID = -7169488308515823491L;

    private final static Log log = LogFactory.getLog( MySQLDiskCache.class );

    MySQLDiskCacheAttributes mySQLDiskCacheAttributes;

    /**
     * Delegates to the super and makes use of the MySQL specific parameters used for scheduled
     * optimization.
     * <p>
     * @param attributes
     * @param tableState
     */
    public MySQLDiskCache( MySQLDiskCacheAttributes attributes, TableState tableState )
    {
        super( attributes, tableState );

        mySQLDiskCacheAttributes = attributes;

        if ( log.isDebugEnabled() )
        {
            log.debug( "MySQLDiskCacheAttributes = " + attributes );
        }
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently optimizing, then this
     * method will balk and return null.
     * <p>
     * @param key Key to locate value for.
     * @return An object matching key, or null.
     */
    public ICacheElement doGet( Serializable key )
    {
        if ( this.getTableState().getState() == TableState.OPTIMIZATION_RUNNING )
        {
            if ( this.mySQLDiskCacheAttributes.isBalkDuringOptimization() )
            {
                return null;
            }
        }
        return super.doGet( key );
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently optimizing, then this
     * method will balk and do nothing.
     * <p>
     * @param element
     */
    public void doUpdate( ICacheElement element )
    {
        if ( this.getTableState().getState() == TableState.OPTIMIZATION_RUNNING )
        {
            if ( this.mySQLDiskCacheAttributes.isBalkDuringOptimization() )
            {
                return;
            }
        }
        super.doUpdate( element );
    }

    /**
     * Removed the expired. (now - create time) > max life seconds * 1000
     * <p>
     * If we are currently optimizing, then this method will balk and do nothing.
     * <p>
     * TODO consider blocking and trying again.
     * <p>
     * @return the number deleted
     */
    protected int deleteExpired()
    {
        if ( this.getTableState().getState() == TableState.OPTIMIZATION_RUNNING )
        {
            if ( this.mySQLDiskCacheAttributes.isBalkDuringOptimization() )
            {
                return -1;
            }
        }
        return super.deleteExpired();
    }
}
