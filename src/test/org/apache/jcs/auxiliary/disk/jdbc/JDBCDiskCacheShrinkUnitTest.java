package org.apache.jcs.auxiliary.disk.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

/**
 * Runs basic tests for the JDBC disk cache.
 * 
 * @author Aaron Smuts
 * 
 */
public class JDBCDiskCacheShrinkUnitTest
    extends TestCase
{

    /**
     * Test setup
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    public void setUp()
        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
    {
        JCS.setConfigFilename( "/TestJDBCDiskCacheShrink.ccf" );

        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        Properties p = new Properties();
        String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
        String url = p.getProperty( "url", "jdbc:hsqldb:" );
        String database = p.getProperty( "database", rafroot + "/cache_hsql_db" );
        String user = p.getProperty( "user", "sa" );
        String password = p.getProperty( "password", "" );

        new org.hsqldb.jdbcDriver();
        Class.forName( driver ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        setupTABLE( cConn );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing. Verify
     * that items configured to expire after 1 second actually expire.
     * 
     * @throws Exception
     */
    public void testExpireInBackground()
        throws Exception
    {
        String regionExpire = "expire1Second";
        int items = 200;

        JCS jcsExpire = JCS.getInstance( regionExpire );

        System.out.println( "BEFORE PUT \n" + jcsExpire.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcsExpire.put( i + ":key", regionExpire + " data " + i );
        }

        System.out.println( jcsExpire.getStats() );

        // the shrinker is supposed to run every second
        Thread.sleep( 2000 );

        System.out.println( jcsExpire.getStats() );

        // Test that all items have been removed from the cache
        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcsExpire.get( i + ":key" ) );
        }
    }

    /**
     * Verify that those not scheduled to expire do not expire.
     * 
     * @throws CacheException
     * @throws InterruptedException
     */
    public void testDidNotExpire()
        throws CacheException, InterruptedException
    {
        String region = "expire100Second";
        int items = 200;

        JCS jcs = JCS.getInstance( region );

        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        System.out.println( jcs.getStats() );

        Thread.sleep( 1000 );

        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }

        // Remove all the items

        for ( int i = 0; i <= items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcs.get( i + ":key" ) );
        }
    }

    /**
     * Verify that eternal trumps max life.
     * 
     * @throws CacheException
     * @throws InterruptedException
     */
    public void testDidNotExpireEternal()
        throws CacheException, InterruptedException
    {
        String region = "eternal";
        int items = 200;

        JCS jcs = JCS.getInstance( region );

        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        System.out.println( jcs.getStats() );

        Thread.sleep( 1000 );

        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }

        // Remove all the items

        for ( int i = 0; i <= items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcs.get( i + ":key" ) );
        }
    }    
    
    /**
     * SETUP TABLE FOR CACHE
     * 
     * @param cConn
     */
    void setupTABLE( Connection cConn )
    {
        boolean newT = true;

        StringBuffer createSql = new StringBuffer();
        createSql.append( "CREATE CACHED TABLE JCS_STORE_SHRINK " );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           DATE, " );
        createSql.append( "CREATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
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
            e.printStackTrace();
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
                // TODO figure out if it exists prior to trying to create it.
                // log.error( "Problem creating table.", e );
                e.printStackTrace();
            }
        }

        String setupData[] = { "create index iKEY on JCS_STORE_SHRINK (CACHE_KEY, REGION)" };

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
                    System.out.println( "Exception: " + e );
                }
            }
        } // end ifnew
    }
}
