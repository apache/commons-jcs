package org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory;

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
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.jcs3.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;
import org.apache.commons.jcs3.log.Log;

/**
 * A factory that looks up the DataSource from JNDI.  It is also able
 * to deploy the DataSource based on properties found in the
 * configuration.
 *
 * This factory tries to avoid excessive context lookups to improve speed.
 * The time between two lookups can be configured. The default is 0 (no cache).
 *
 * Borrowed and adapted from Apache DB Torque
 */
public class JndiDataSourceFactory implements DataSourceFactory
{
    /** The log. */
    private static final Log log = Log.getLog(JndiDataSourceFactory.class);

    /**
     *
     * @param ctx the context
     * @throws NamingException
     */
    private static void debugCtx(final Context ctx) throws NamingException
    {
        log.trace("InitialContext -------------------------------");
        final Map<?, ?> env = ctx.getEnvironment();
        log.trace("Environment properties: {0}", env.size());
        env.forEach((key, value) -> log.trace("    {0}: {1}", key, value));
        log.trace("----------------------------------------------");
    }

    /** The name of the factory. */
    private String name;

    /** The path to get the resource from. */
    private String path;

    /** The context to get the resource from. */
    private Context ctx;

    /** A locally cached copy of the DataSource */
    private DataSource ds;

    /** Time of last actual lookup action */
    private long lastLookup;

    /** Time between two lookups */
    private long ttl; // ms

    /**
     * Does nothing. We do not want to close a dataSource retrieved from Jndi,
     * because other applications might use it as well.
     */
    @Override
	public void close()
    {
        // do nothing
    }

    /**
     * @see org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.DataSourceFactory#getDataSource()
     */
    @Override
	public DataSource getDataSource() throws SQLException
    {
        final long time = System.currentTimeMillis();

        if (ds == null || time - lastLookup > ttl)
        {
            try
            {
                synchronized (ctx)
                {
                    ds = (DataSource) ctx.lookup(path);
                }
                lastLookup = time;
            }
            catch (final NamingException e)
            {
                throw new SQLException(e);
            }
        }

        return ds;
    }

    /**
     * @return the name of the factory.
     */
    @Override
	public String getName()
    {
    	return name;
    }

    /**
     * @see org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.DataSourceFactory#initialize(JDBCDiskCacheAttributes)
     */
    @Override
	public void initialize(final JDBCDiskCacheAttributes config) throws SQLException
    {
    	this.name = config.getConnectionPoolName();
        initJNDI(config);
    }

    /**
     * Initializes JNDI.
     *
     * @param config where to read the settings from
     * @throws SQLException if a property set fails
     */
    private void initJNDI(final JDBCDiskCacheAttributes config) throws SQLException
    {
        log.debug("Starting initJNDI");

        try
        {
            this.path = config.getJndiPath();
            log.debug("JNDI path: {0}", path);

            this.ttl = config.getJndiTTL();
            log.debug("Time between context lookups: {0}", ttl);

    		final Hashtable<String, Object> env = new Hashtable<>();
            ctx = new InitialContext(env);

            if (log.isTraceEnabled())
            {
            	log.trace("Created new InitialContext");
            	debugCtx(ctx);
            }
        }
        catch (final NamingException e)
        {
            throw new SQLException(e);
        }
    }
}
