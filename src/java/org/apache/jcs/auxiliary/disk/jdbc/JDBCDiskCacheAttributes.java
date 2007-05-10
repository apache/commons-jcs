package org.apache.jcs.auxiliary.disk.jdbc;

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

import org.apache.jcs.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * The configurator will set these values based on what is in the cache.ccf
 * file.
 * <p>
 * @author Aaron Smuts
 */
public class JDBCDiskCacheAttributes
    extends AbstractDiskCacheAttributes
{
    private static final long serialVersionUID = -6535808344813320062L;

    private static final String DEFAULT_TABLE_NAME = "JCS_STORE";

    private String userName;

    private String password;

    private String url;

    private String database = "";

    private String driverClassName;

    private String tableName = DEFAULT_TABLE_NAME;

    private boolean testBeforeInsert = true;

    /** This is the default limit on the maximum number of active connections. */
    public static final int DEFAULT_MAX_ACTIVE = 10;

    private int maxActive = DEFAULT_MAX_ACTIVE;

    /** This is the default setting for the cleanup routine. */
    public static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 300;

    private int shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;

    private boolean UseDiskShrinker = true;

    /**
     * @param userName
     *            The userName to set.
     */
    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword( String password )
    {
        this.password = password;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param url
     *            The url to set.
     */
    public void setUrl( String url )
    {
        this.url = url;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * This is appended to the url.
     * @param database
     *            The database to set.
     */
    public void setDatabase( String database )
    {
        this.database = database;
    }

    /**
     * @return Returns the database.
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @param driverClassName
     *            The driverClassName to set.
     */
    public void setDriverClassName( String driverClassName )
    {
        this.driverClassName = driverClassName;
    }

    /**
     * @return Returns the driverClassName.
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    /**
     * @param tableName
     *            The tableName to set.
     */
    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }

    /**
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * If this is true then the disk cache will check to see if the item already
     * exists in the database. If it is false, it will try to insert. If the
     * isnert fails it will try to update.
     * @param testBeforeInsert
     *            The testBeforeInsert to set.
     */
    public void setTestBeforeInsert( boolean testBeforeInsert )
    {
        this.testBeforeInsert = testBeforeInsert;
    }

    /**
     * @return Returns the testBeforeInsert.
     */
    public boolean isTestBeforeInsert()
    {
        return testBeforeInsert;
    }

    /**
     * @param maxActive
     *            The maxActive to set.
     */
    public void setMaxActive( int maxActive )
    {
        this.maxActive = maxActive;
    }

    /**
     * @return Returns the maxActive.
     */
    public int getMaxActive()
    {
        return maxActive;
    }

    /**
     * @param shrinkerIntervalSecondsArg
     *            The shrinkerIntervalSeconds to set.
     */
    public void setShrinkerIntervalSeconds( int shrinkerIntervalSecondsArg )
    {
        this.shrinkerIntervalSeconds = shrinkerIntervalSecondsArg;
    }

    /**
     * @return Returns the shrinkerIntervalSeconds.
     */
    public int getShrinkerIntervalSeconds()
    {
        return shrinkerIntervalSeconds;
    }

    /**
     * @param useDiskShrinker
     *            The useDiskShrinker to set.
     */
    public void setUseDiskShrinker( boolean useDiskShrinker )
    {
        UseDiskShrinker = useDiskShrinker;
    }

    /**
     * @return Returns the useDiskShrinker.
     */
    public boolean isUseDiskShrinker()
    {
        return UseDiskShrinker;
    }

    /**
     * For debugging.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nJDBCCacheAttributes" );
        buf.append( "\n UserName [" + getUserName() + "]" );
        buf.append( "\n Url [" + getUrl() + "]" );
        buf.append( "\n Database [" + getDatabase() + "]" );
        buf.append( "\n DriverClassName [" + getDriverClassName() + "]" );
        buf.append( "\n TableName [" + getTableName() + "]" );
        buf.append( "\n TestBeforeInsert [" + isTestBeforeInsert() + "]" );
        buf.append( "\n MaxActive [" + getMaxActive() + "]" );
        buf.append( "\n AllowRemoveAll [" + isAllowRemoveAll() + "]" );
        buf.append( "\n ShrinkerIntervalSeconds [" + getShrinkerIntervalSeconds() + "]" );
        buf.append( "\n UseDiskShrinker [" + isUseDiskShrinker() + "]" );
        return buf.toString();
    }
}
