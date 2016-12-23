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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.jcs.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs.auxiliary.disk.jdbc.dsfactory.JndiDataSourceFactory;
import org.apache.commons.jcs.auxiliary.disk.jdbc.dsfactory.SharedPoolDataSourceFactory;

/** Unit tests for the data source factories */
public class JDBCDataSourceFactoryUnitTest
    extends TestCase
{
    /** Verify that we can configure the object based on the props.
     *  @throws SQLException
     */
    public void testConfigureDataSourceFactory_Simple() throws SQLException
    {
        // SETUP
        String poolName = "testConfigurePoolAccessAttributes_Simple";

        String url = "adfads";
        String userName = "zvzvz";
        String password = "qewrrewq";
        int maxActive = 10;
        String driverClassName = "org.hsqldb.jdbcDriver";

        Properties props = new Properties();
        String prefix = JDBCDiskCacheFactory.POOL_CONFIGURATION_PREFIX
    		+ poolName
            + JDBCDiskCacheFactory.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", url );
        props.put( prefix + ".userName", userName );
        props.put( prefix + ".password", password );
        props.put( prefix + ".maxActive", String.valueOf( maxActive ) );
        props.put( prefix + ".driverClassName", driverClassName );

        JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setConnectionPoolName( poolName );

        // DO WORK
        DataSourceFactory result = factory.getDataSourceFactory( cattr, props );
        assertTrue("Should be a shared pool data source factory", result instanceof SharedPoolDataSourceFactory);

        SharedPoolDataSource spds = (SharedPoolDataSource) result.getDataSource();
        assertNotNull( "Should have a data source class", spds );

        // VERIFY
        assertEquals( "Wrong pool name", poolName, spds.getDescription() );
        assertEquals( "Wrong maxActive value", maxActive, spds.getMaxActive() );
    }

    /** Verify that we can configure the object based on the attributes.
     *  @throws SQLException
     */
    public void testConfigureDataSourceFactory_Attributes() throws SQLException
    {
        // SETUP
        String url = "adfads";
        String userName = "zvzvz";
        String password = "qewrrewq";
        int maxActive = 10;
        String driverClassName = "org.hsqldb.jdbcDriver";

        JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setUrl(url);
        cattr.setUserName(userName);
        cattr.setPassword(password);
        cattr.setMaxActive(maxActive);
        cattr.setDriverClassName(driverClassName);

        // DO WORK
        DataSourceFactory result = factory.getDataSourceFactory( cattr, null );
        assertTrue("Should be a shared pool data source factory", result instanceof SharedPoolDataSourceFactory);

        SharedPoolDataSource spds = (SharedPoolDataSource) result.getDataSource();
        assertNotNull( "Should have a data source class", spds );

        // VERIFY
        assertEquals( "Wrong maxActive value", maxActive, spds.getMaxActive() );
    }

    /** Verify that we can configure the object based on JNDI.
     *  @throws SQLException
     */
    public void testConfigureDataSourceFactory_JNDI() throws SQLException
    {
        // SETUP
        String jndiPath = "java:comp/env/jdbc/MyDB";
        long ttl = 300000L;

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
        		  MockInitialContextFactory.class.getName());

        MockInitialContextFactory.bind(jndiPath, new BasicDataSource());

        JDBCDiskCacheFactory factory = new JDBCDiskCacheFactory();
        factory.initialize();

        JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setJndiPath(jndiPath);
        cattr.setJndiTTL(ttl);

        // DO WORK
        DataSourceFactory result = factory.getDataSourceFactory( cattr, null );
        assertTrue("Should be a JNDI data source factory", result instanceof JndiDataSourceFactory);
    }

    /* For JNDI mocking */
    public static class MockInitialContextFactory implements InitialContextFactory
    {
        private static Context context;

        static
        {
            try
            {
                context = new InitialContext(true)
                {
                    Map<String, Object> bindings = new HashMap<String, Object>();

                    @Override
                    public void bind(String name, Object obj) throws NamingException
                    {
                        bindings.put(name, obj);
                    }

                    @Override
                    public Object lookup(String name) throws NamingException
                    {
                        return bindings.get(name);
                    }
                };
            }
            catch (NamingException e)
            {
            	// can't happen.
                throw new RuntimeException(e);
            }
        }

        @Override
		public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
        {
            return context;
        }

        public static void bind(String name, Object obj)
        {
            try
            {
                context.bind(name, obj);
            }
            catch (NamingException e)
            {
            	// can't happen.
                throw new RuntimeException(e);
            }
        }
    }
}

