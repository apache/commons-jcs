package org.apache.jcs.auxiliary.disk.hsql;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.data.PropertyGroups;

/**
 * HSQLDB Based Local Persistence.
 * 
 * <b>VERY EXPERIMENTAL, and only partially implemented </b> Requires String
 * keys and does not work with groups.
 * 
 * @version 1.0
 */
public class HSQLCache
    extends AbstractDiskCache
{

    private static final long serialVersionUID = 1L;

    private final static Log log = LogFactory.getLog( HSQLCache.class );

    private int numInstances = 0;

    private HSQLCacheAttributes cattr;

    // for now use one statement per cache and keep it open
    // can move up to manager level or implement pooling if there are too many
    // caches
    private Connection cConn;

    /**
     * Constructor for the HSQLCache object
     * 
     * @param cattr
     */
    public HSQLCache( HSQLCacheAttributes cattr )
    {
        super( cattr );

        this.cattr = cattr;

        String rafroot = this.cattr.getDiskPath();

        numInstances++;

        //String rafroot = cattr.getDiskPath();
        if ( rafroot == null )
        {
            try
            {
                PropertyGroups pg = new PropertyGroups( "/cache.properties" );
                rafroot = pg.getProperty( "diskPath" );
            }
            catch ( Exception e )
            {
                log.error( e );
            }
        }
        else
        {
            try
            {
                File dir = new File( rafroot );
                dir.mkdir();
            }
            catch ( Exception e )
            {
                log.error( "Problem creating directory.", e );
            }
        }

        try
        {
            Properties p = new Properties();
            String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
            String url = p.getProperty( "url", "jdbc:hsqldb:" );
            // TODO trim trailling / from root
            String database = p.getProperty( "database", rafroot + "/cache_hsql_db" );
            String user = p.getProperty( "user", "sa" );
            String password = p.getProperty( "password", "" );
            boolean test = p.getProperty( "test", "true" ).equalsIgnoreCase( "true" );
            // boolean log = p.getProperty( "log", "true" ).equalsIgnoreCase(
            // "true" );

            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "driver  =" + driver + ", url = " + url + ", database = " + database + ", user = "
                        + user + ", password = " + password + ", test = " + test );
                }

                // As described in the JDBC FAQ:
                // http://java.sun.com/products/jdbc/jdbc-frequent.html;
                // Why doesn't calling class.forName() load my JDBC driver?
                // There is a bug in the JDK 1.1.x that can cause
                // Class.forName() to fail.
                new org.hsqldb.jdbcDriver();
                Class.forName( driver ).newInstance();

                cConn = DriverManager.getConnection( url + database, user, password );
                Statement sStatement = null;
                try
                {
                    sStatement = cConn.createStatement();
                    alive = true;
                }
                catch ( SQLException e )
                {
                    log.error( "Problem creating statement.", e );
                    System.out.println( "Exception: " + e );
                    alive = false;
                }
                finally
                {
                    try
                    {
                        if ( sStatement != null )
                        {
                            sStatement.close();
                        }
                    }
                    catch ( SQLException e1 )
                    {
                        log.error( "Problem closing statement.", e1 );
                    }
                }
                setupTABLE();
            }
            catch ( Exception e )
            {
                log.error( "QueryTool.init", e );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem creating HSQL", e );
        }
    } // end constructor

    /** SETUP TABLE FOR CACHE */
    void setupTABLE()
    {
        boolean newT = true;

        String setup = "create table " + cacheName + " (KEY varchar(255) primary key, ELEMENT binary)";

        Statement sStatement = null;
        try
        {
            sStatement = cConn.createStatement();
            alive = true;
        }
        catch ( SQLException e )
        {
            log.error( "Problem creating statement.", e );
            //System.out.println( "Exception: " + e );
            alive = false;
            return;
        }

        try
        {
            sStatement.executeQuery( setup );
            sStatement.close();
        }
        catch ( SQLException e )
        {
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            // TODO figure out if it exists prior to trying to create it.
            log.error( "Problem creating table.", e );
        }

        String setupData[] = { "create index iKEY on " + cacheName + " (KEY)" };

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

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doUpdate(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public synchronized void doUpdate( ICacheElement ce )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "updating, ce = " + ce );
        }

        Statement sStatement = null;
        try
        {
            sStatement = cConn.createStatement();
            alive = true;
        }
        catch ( SQLException e )
        {
            log.error( "Problem creating statement.", e );
            alive = false;
        }

        if ( !alive )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Disk is not alive, aborting put." );
            }
            return;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Putting " + ce.getKey() + " on disk." );
        }

        byte[] element;

        try
        {
            element = serialize( ce );
        }
        catch ( IOException e )
        {
            log.error( "Could not serialize element", e );
            return;
        }

        boolean exists = false;

        // First do a query to determine if the element already exists

        try
        {
            String sqlS = "SELECT element FROM " + cacheName + " WHERE key = '" + (String) ce.getKey() + "'";

            ResultSet rs = sStatement.executeQuery( sqlS );

            if ( rs.next() )
            {
                exists = true;
            }

            rs.close();
        }
        catch ( SQLException e )
        {
            log.error( e );
        }
        finally
        {
            try
            {
                sStatement.close();
            }
            catch ( SQLException e1 )
            {
                log.error( "Problem closing statement.", e1 );
            }
        }

        // If it doesn't exist, insert it, otherwise update

        if ( !exists )
        {
            try
            {
                String sqlI = "insert into " + cacheName + " (KEY, ELEMENT) values (?, ? )";

                PreparedStatement psInsert = cConn.prepareStatement( sqlI );
                psInsert.setString( 1, (String) ce.getKey() );
                psInsert.setBytes( 2, element );
                psInsert.execute();
                psInsert.close();
                //sStatement.executeUpdate(sql);
            }
            catch ( SQLException e )
            {
                if ( e.toString().indexOf( "Violation of unique index" ) != -1
                    || e.getMessage().indexOf( "Violation of unique index" ) != -1 )
                {
                    exists = true;
                }
                else
                {
                    log.error( "Could not insert element", e );
                }
            }
        }
        else
        {
            try
            {
                String sqlU = "update " + cacheName + " set ELEMENT  = ? ";
                PreparedStatement psUpdate = cConn.prepareStatement( sqlU );
                psUpdate.setBytes( 1, element );
                psUpdate.setString( 2, (String) ce.getKey() );
                psUpdate.execute();
                psUpdate.close();

                log.debug( "ran update" );
            }
            catch ( SQLException e2 )
            {
                log.error( "e2 Exception: ", e2 );
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doGet(java.io.Serializable)
     */
    public synchronized ICacheElement doGet( Serializable key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Getting " + key + " from disk" );
        }

        if ( !alive )
        {
            return null;
        }

        ICacheElement obj = null;

        byte[] data = null;
        try
        {
            String sqlS = "select ELEMENT from " + cacheName + " where KEY = ?";
            PreparedStatement psSelect = cConn.prepareStatement( sqlS );
            psSelect.setString( 1, (String) key );
            ResultSet rs = psSelect.executeQuery();
            if ( rs.next() )
            {
                data = rs.getBytes( 1 );
            }
            if ( data != null )
            {
                try
                {
                    ByteArrayInputStream bais = new ByteArrayInputStream( data );
                    BufferedInputStream bis = new BufferedInputStream( bais );
                    ObjectInputStream ois = new ObjectInputStream( bis );
                    try
                    {
                        obj = (ICacheElement) ois.readObject();
                    }
                    finally
                    {
                        ois.close();
                    }
                    // move to finally
                    rs.close();
                    psSelect.close();
                }
                catch ( IOException ioe )
                {
                    log.error( ioe );
                }
                catch ( Exception e )
                {
                    log.error( e );
                }
            }
            //else {
            //return null;
            //}
        }
        catch ( SQLException sqle )
        {
            log.error( sqle );
        }

        return obj;
    }

    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     * 
     * @param key
     * @return boolean
     */
    public synchronized boolean doRemove( Serializable key )
    {
        // remove single item.
        String sql = "delete from " + cacheName + " where KEY = '" + key + "'";

        try
        {
            if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.
                sql = "delete from " + cacheName + " where KEY = like '" + key + "%'";
            }

            Statement sStatement = null;
            try
            {
                sStatement = cConn.createStatement();
                alive = true;

                sStatement.executeQuery( sql );
            }
            catch ( SQLException e )
            {
                log.error( "Problem creating statement.", e );
                alive = false;
            }
            finally
            {
                try
                {
                    if ( sStatement != null )
                    {
                        sStatement.close();
                    }
                }
                catch ( SQLException e1 )
                {
                    log.error( "Problem closing statement.", e1 );
                }
            }

        }
        catch ( Exception e )
        {
            log.error( e );
            reset();
        }
        return false;
    }

    /** Description of the Method */
    public synchronized void doRemoveAll()
    {
        try
        {
            //reset();
        }
        catch ( RuntimeException e )
        {
            log.error( e );
            //reset();
        }
    }

    // handle error by last resort, force content update, or removeall
    /** Description of the Method */
    public void reset()
    {
        // nothing
    }

    /** Description of the Method */
    public void doDispose()
    {
        // nothing
    }

    /**
     * Returns the current cache size.
     * 
     * @return The size value
     */
    public int getSize()
    {
        return 0;
        // need to get count
    }

    /**
     * Returns the serialized form of the given object in a byte array.
     * 
     * @param obj
     * @return byte[]
     * @throws IOException
     */
    static byte[] serialize( Serializable obj )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }

    /**
     * @param groupName
     * @return
     *  
     */
    public Set getGroupKeys( String groupName )
    {
        if ( true )
        {
            throw new UnsupportedOperationException( "Groups not implemented." );
        }
        return null;
    }
}
