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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * The configurator will set these values based on what is in the cache.ccf file.
 */
public class JDBCDiskCacheAttributes
    extends AbstractDiskCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -6535808344813320062L;

    /** Default */
    private static final String DEFAULT_TABLE_NAME = "JCS_STORE";

    /** This is the default limit on the maximum number of active connections. */
    public static final int DEFAULT_MAX_TOTAL = 10;

    /** This is the default setting for the cleanup routine. */
    public static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 300;

    /** The default Pool Name to which the connection pool will be keyed. */
    public static final String DEFAULT_POOL_NAME = "jcs";

    /** DB username */
    private String userName;

    /** DB password */
    private String password;

    /** URL for the db */
    private String url;

    /** The name of the database. */
    private String database = "";

    /** The driver */
    private String driverClassName;

    /** The JNDI path. */
    private String jndiPath;

    /** The time between two JNDI lookups */
    private long jndiTTL;

    /** The table name */
    private String tableName = DEFAULT_TABLE_NAME;

    /** If false we will insert and if it fails we will update. */
    private boolean testBeforeInsert = true;

    /** Max connections allowed */
    private int maxTotal = DEFAULT_MAX_TOTAL;

    /** How often should we remove expired. */
    private int shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;

    /** Should we remove expired in the background. */
    private boolean useDiskShrinker = true;

    /**
     * If a pool name is supplied, the manager will attempt to load it. It should be configured in a
     * separate section as follows. Assuming the name is "MyPool":
     *
     * <pre>
     * jcs.jdbcconnectionpool.MyPool.attributes.userName=MyUserName
     * jcs.jdbcconnectionpool.MyPool.attributes.password=MyPassword
     * jcs.jdbcconnectionpool.MyPool.attributes.url=MyUrl
     * jcs.jdbcconnectionpool.MyPool.attributes.maxActive=MyMaxActive
     * jcs.jdbcconnectionpool.MyPool.attributes.driverClassName=MyDriverClassName
     * </pre>
     */
    private String connectionPoolName;

    /**
     * @return the connectionPoolName
     */
    public String getConnectionPoolName()
    {
        return connectionPoolName;
    }

    /**
     * @return the database.
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @return the driverClassName.
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    /**
	 * @return the jndiPath
	 */
	public String getJndiPath()
	{
		return jndiPath;
	}

    /**
	 * @return the jndiTTL
	 */
	public long getJndiTTL()
	{
		return jndiTTL;
	}

    /**
     * @return the maxTotal.
     */
    public int getMaxTotal()
    {
        return maxTotal;
    }

    /**
     * @return the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @return the shrinkerIntervalSeconds.
     */
    public int getShrinkerIntervalSeconds()
    {
        return shrinkerIntervalSeconds;
    }

    /**
     * @return the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the url.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return the userName.
     */
    public String getUserName()
    {
        return userName;
    }

	/**
     * @return the testBeforeInsert.
     */
    public boolean isTestBeforeInsert()
    {
        return testBeforeInsert;
    }

	/**
     * @return the useDiskShrinker.
     */
    public boolean isUseDiskShrinker()
    {
        return useDiskShrinker;
    }

	/**
     * @param connectionPoolName the connectionPoolName to set
     */
    public void setConnectionPoolName( final String connectionPoolName )
    {
        this.connectionPoolName = connectionPoolName;
    }

	/**
     * This is appended to the url.
     * @param database The database to set.
     */
    public void setDatabase( final String database )
    {
        this.database = database;
    }

    /**
     * @param driverClassName The driverClassName to set.
     */
    public void setDriverClassName( final String driverClassName )
    {
        this.driverClassName = driverClassName;
    }

    /**
	 * @param jndiPath the jndiPath to set
	 */
	public void setJndiPath(final String jndiPath)
	{
		this.jndiPath = jndiPath;
	}

    /**
	 * @param jndiTTL the jndiTTL to set
	 */
	public void setJndiTTL(final long jndiTTL)
	{
		this.jndiTTL = jndiTTL;
	}

    /**
     * @param maxActive The maxTotal to set.
     */
    public void setMaxTotal( final int maxActive )
    {
        this.maxTotal = maxActive;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword( final String password )
    {
        this.password = password;
    }

    /**
     * @param shrinkerIntervalSecondsArg The shrinkerIntervalSeconds to set.
     */
    public void setShrinkerIntervalSeconds( final int shrinkerIntervalSecondsArg )
    {
        this.shrinkerIntervalSeconds = shrinkerIntervalSecondsArg;
    }

    /**
     * @param tableName The tableName to set.
     */
    public void setTableName( final String tableName )
    {
        this.tableName = tableName;
    }

    /**
     * If this is true then the disk cache will check to see if the item already exists in the
     * database. If it is false, it will try to insert. If the insert fails it will try to update.
     * <p>
     * @param testBeforeInsert The testBeforeInsert to set.
     */
    public void setTestBeforeInsert( final boolean testBeforeInsert )
    {
        this.testBeforeInsert = testBeforeInsert;
    }

    /**
     * @param url The url to set.
     */
    public void setUrl( final String url )
    {
        this.url = url;
    }

    /**
     * @param useDiskShrinker The useDiskShrinker to set.
     */
    public void setUseDiskShrinker( final boolean useDiskShrinker )
    {
        this.useDiskShrinker = useDiskShrinker;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName( final String userName )
    {
        this.userName = userName;
    }

    /**
     * For debugging.
     * <p>
     * @return debug string with most of the properties.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\nJDBCCacheAttributes" );
        buf.append( "\n UserName [" + getUserName() + "]" );
        buf.append( "\n Url [" + getUrl() + "]" );
        buf.append( "\n Database [" + getDatabase() + "]" );
        buf.append( "\n DriverClassName [" + getDriverClassName() + "]" );
        buf.append( "\n TableName [" + getTableName() + "]" );
        buf.append( "\n TestBeforeInsert [" + isTestBeforeInsert() + "]" );
        buf.append( "\n MaxActive [" + getMaxTotal() + "]" );
        buf.append( "\n AllowRemoveAll [" + isAllowRemoveAll() + "]" );
        buf.append( "\n ShrinkerIntervalSeconds [" + getShrinkerIntervalSeconds() + "]" );
        buf.append( "\n useDiskShrinker [" + isUseDiskShrinker() + "]" );
        return buf.toString();
    }
}
