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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.JndiDataSourceFactory;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.SharedPoolDataSourceFactory;
import org.junit.jupiter.api.Test;

/** Tests for the data source factories */
class JDBCDataSourceFactoryUnitTest
{
    /* For JNDI mocking */
    public static class MockInitialContextFactory implements InitialContextFactory
    {
        private static final Context context;

        static
        {
            try
            {
                context = new InitialContext(true)
                {
                    final Map<String, Object> bindings = new HashMap<>();

                    @Override
                    public void bind(final String name, final Object obj) throws NamingException
                    {
                        bindings.put(name, obj);
                    }

                    @Override
                    public Hashtable<?, ?> getEnvironment() throws NamingException
                    {
                        return new Hashtable<>();
                    }

                    @Override
                    public Object lookup(final String name) throws NamingException
                    {
                        return bindings.get(name);
                    }
                };
            }
            catch (final NamingException e)
            {
            	// can't happen.
                throw new IllegalStateException(e);
            }
        }

        public static void bind(final String name, final Object obj)
        {
            try
            {
                context.bind(name, obj);
            }
            catch (final NamingException e)
            {
            	// can't happen.
                throw new IllegalArgumentException(e);
            }
        }

        @Override
		public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException
        {
            return context;
        }
    }

    /**
     * Verify that we can configure the object based on the attributes.
     * @throws SQLException
     */
    @Test
    void testConfigureDataSourceFactory_Attributes()
        throws SQLException
    {
        // SETUP
        final String url = "adfads";
        final String userName = "zvzvz";
        final String password = "qewrrewq";
        final int maxActive = 10;
        final String driverClassName = "org.hsqldb.jdbcDriver";

        final JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        final JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setUrl(url);
        cattr.setUserName(userName);
        cattr.setPassword(password);
        cattr.setMaxTotal(maxActive);
        cattr.setDriverClassName(driverClassName);

        // DO WORK
        final DataSourceFactory result = factory.getDataSourceFactory( cattr, null );
        assertInstanceOf( SharedPoolDataSourceFactory.class, result, "Should be a shared pool data source factory" );

        final SharedPoolDataSource spds = (SharedPoolDataSource) result.getDataSource();
        assertNotNull( spds, "Should have a data source class" );

        // VERIFY
        assertEquals( maxActive, spds.getMaxTotal(), "Wrong maxActive value" );
    }

    /**
     * Verify that we can configure the object based on JNDI.
     * @throws SQLException
     */
    @Test
    void testConfigureDataSourceFactory_JNDI()
        throws SQLException
    {
        // SETUP
        final String jndiPath = "java:comp/env/jdbc/MyDB";
        final long ttl = 300000L;

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                MockInitialContextFactory.class.getName());

        MockInitialContextFactory.bind(jndiPath, new BasicDataSource());

        final JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        final JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setJndiPath(jndiPath);
        cattr.setJndiTTL(ttl);

        // DO WORK
        final DataSourceFactory result = factory.getDataSourceFactory( cattr, null );
        assertInstanceOf( JndiDataSourceFactory.class, result, "Should be a JNDI data source factory" );
    }

    /**
     * Verify that we can configure the object based on the props.
     * @throws SQLException
     */
    @Test
    void testConfigureDataSourceFactory_Simple()
        throws SQLException
    {
        // SETUP
        final String poolName = "testConfigurePoolAccessAttributes_Simple";

        final String url = "adfads";
        final String userName = "zvzvz";
        final String password = "qewrrewq";
        final int maxActive = 10;
        final String driverClassName = "org.hsqldb.jdbcDriver";

        final Properties props = new Properties();
        final String prefix = JDBCDiskCacheFactory.POOL_CONFIGURATION_PREFIX
    		+ poolName
            + JDBCDiskCacheFactory.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", url );
        props.put( prefix + ".userName", userName );
        props.put( prefix + ".password", password );
        props.put( prefix + ".maxActive", String.valueOf( maxActive ) );
        props.put( prefix + ".driverClassName", driverClassName );

        final JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        final JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setConnectionPoolName( poolName );

        // DO WORK
        final DataSourceFactory result = factory.getDataSourceFactory( cattr, props );
        assertInstanceOf( SharedPoolDataSourceFactory.class, result, "Should be a shared pool data source factory" );

        final SharedPoolDataSource spds = (SharedPoolDataSource) result.getDataSource();
        assertNotNull( spds, "Should have a data source class" );

        // VERIFY
        assertEquals( poolName, spds.getDescription(), "Wrong pool name" );
        assertEquals( maxActive, spds.getMaxTotal(), "Wrong maxActive value" );
    }
}

