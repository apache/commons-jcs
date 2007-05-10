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

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * This is the jdbc disk cache plugin.
 * <p>
 * It expects a table created by the following script. The table name is
 * configurable.
 * <p>
 *
 * <pre>
 *                       drop TABLE JCS_STORE;
 *
 *                       CREATE TABLE JCS_STORE
 *                       (
 *                       CACHE_KEY                  VARCHAR(250)          NOT NULL,
 *                       REGION                     VARCHAR(250)          NOT NULL,
 *                       ELEMENT                    BLOB,
 *                       CREATE_TIME                DATE,
 *                       CREATE_TIME_SECONDS        BIGINT,
 *                       MAX_LIFE_SECONDS           BIGINT,
 *                       SYSTEM_EXPIRE_TIME_SECONDS BIGINT,
 *                       IS_ETERNAL                 CHAR(1),
 *                       PRIMARY KEY (CACHE_KEY, REGION)
 *                       );
 * </pre>
 *
 * <p>
 * The cleanup thread will delete non eternal items where (now - create time) >
 * max life seconds * 1000
 * <p>
 * To speed up the deletion the SYSTEM_EXPIRE_TIME_SECONDS is used instead. It
 * is recommended that an index be created on this column is you will have over
 * a million records.
 * <p>
 * @author Aaron Smuts
 */
public class JDBCDiskCache
    extends AbstractDiskCache
{
    private final static Log log = LogFactory.getLog( JDBCDiskCache.class );

    private static final long serialVersionUID = -7169488308515823492L;

    private IElementSerializer elementSerializer = new StandardSerializer();

    private JDBCDiskCacheAttributes jdbcDiskCacheAttributes;

    private int updateCount = 0;

    private int getCount = 0;

    // if count % interval == 0 then log
    private static final int LOG_INTERVAL = 100;

    private JDBCDiskCachePoolAccess poolAccess = null;

    private TableState tableState;

    /**
     * Constructs a JDBC Disk Cache for the provided cache attributes. The table
     * state object is used to mark deletions.
     * <p>
     * @param cattr
     * @param tableState
     */
    public JDBCDiskCache( JDBCDiskCacheAttributes cattr, TableState tableState )
    {
        super( cattr );

        this.setTableState( tableState );

        setJdbcDiskCacheAttributes( cattr );

        if ( log.isInfoEnabled() )
        {
            log.info( "jdbcDiskCacheAttributes = " + getJdbcDiskCacheAttributes() );
        }

        /**
         * This initializes the pool access.
         */
        initializePoolAccess( cattr );

        // Initialization finished successfully, so set alive to true.
        alive = true;
    }

    /**
     * Registers the driver and creates a poolAccess class.
     * @param cattr
     */
    protected void initializePoolAccess( JDBCDiskCacheAttributes cattr )
    {
        try
        {
            try
            {
                // org.gjt.mm.mysql.Driver
                Class.forName( cattr.getDriverClassName() );
            }
            catch ( ClassNotFoundException e )
            {
                log.error( "Couldn't find class for driver [" + cattr.getDriverClassName() + "]", e );
            }

            poolAccess = new JDBCDiskCachePoolAccess( cattr.getName() );

            poolAccess.setupDriver( cattr.getUrl() + cattr.getDatabase(), cattr.getUserName(), cattr.getPassword(),
                                    cattr.getMaxActive() );

            poolAccess.logDriverStats();
        }
        catch ( Exception e )
        {
            log.error( "Problem getting connection.", e );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doUpdate(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void doUpdate( ICacheElement ce )
    {
        incrementUpdateCount();

        if ( log.isDebugEnabled() )
        {
            log.debug( "updating, ce = " + ce );
        }

        Connection con;
        try
        {
            con = poolAccess.getConnection();
        }
        catch ( SQLException e )
        {
            log.error( "Problem getting conenction.", e );
            return;
        }

        try
        {
            // TEST
            Statement sStatement = null;
            try
            {
                sStatement = con.createStatement();
                alive = true;
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
                    sStatement.close();
                }
                catch ( SQLException e )
                {
                    log.error( "Problem closing statement.", e );
                }
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
                log.debug( "Putting [" + ce.getKey() + "] on disk." );
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
            if ( this.getJdbcDiskCacheAttributes().isTestBeforeInsert() )
            {
                exists = doesElementExist( ce );
            }

            // If it doesn't exist, insert it, otherwise update
            if ( !exists )
            {
                try
                {
                    String sqlI = "insert into "
                        + getJdbcDiskCacheAttributes().getTableName()
                        + " (CACHE_KEY, REGION, ELEMENT, MAX_LIFE_SECONDS, IS_ETERNAL, CREATE_TIME, CREATE_TIME_SECONDS, SYSTEM_EXPIRE_TIME_SECONDS) "
                        + " values (?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement psInsert = con.prepareStatement( sqlI );
                    psInsert.setString( 1, (String) ce.getKey() );
                    psInsert.setString( 2, this.getCacheName() );
                    psInsert.setBytes( 3, element );
                    psInsert.setLong( 4, ce.getElementAttributes().getMaxLifeSeconds() );
                    if ( ce.getElementAttributes().getIsEternal() )
                    {
                        psInsert.setString( 5, "T" );
                    }
                    else
                    {
                        psInsert.setString( 5, "F" );
                    }
                    Date createTime = new Date( ce.getElementAttributes().getCreateTime() );
                    psInsert.setDate( 6, createTime );

                    long now = System.currentTimeMillis() / 1000;
                    psInsert.setLong( 7, now );

                    long expireTime = now + ce.getElementAttributes().getMaxLifeSeconds();
                    psInsert.setLong( 8, expireTime );

                    psInsert.execute();
                    psInsert.close();
                }
                catch ( SQLException e )
                {
                    if ( e.toString().indexOf( "Violation of unique index" ) != -1
                        || e.getMessage().indexOf( "Violation of unique index" ) != -1
                        || e.getMessage().indexOf( "Duplicate entry" ) != -1 )
                    {
                        exists = true;
                    }
                    else
                    {
                        log.error( "Could not insert element", e );
                    }

                    // see if it exists, if we didn't already
                    if ( !exists && !this.getJdbcDiskCacheAttributes().isTestBeforeInsert() )
                    {
                        exists = doesElementExist( ce );
                    }
                }
            }

            // update if it exists.
            if ( exists )
            {
                String sqlU = null;
                try
                {
                    sqlU = "update " + getJdbcDiskCacheAttributes().getTableName()
                        + " set ELEMENT  = ?, CREATE_TIME = ?, CREATE_TIME_SECONDS = ?, "
                        + " SYSTEM_EXPIRE_TIME_SECONDS = ? " + " where CACHE_KEY = ? and REGION = ?";
                    PreparedStatement psUpdate = con.prepareStatement( sqlU );
                    psUpdate.setBytes( 1, element );

                    Date createTime = new Date( ce.getElementAttributes().getCreateTime() );
                    psUpdate.setDate( 2, createTime );

                    long now = System.currentTimeMillis() / 1000;
                    psUpdate.setLong( 3, now );

                    long expireTime = now + ce.getElementAttributes().getMaxLifeSeconds();
                    psUpdate.setLong( 4, expireTime );

                    psUpdate.setString( 5, (String) ce.getKey() );
                    psUpdate.setString( 6, this.getCacheName() );
                    psUpdate.execute();
                    psUpdate.close();

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "ran update " + sqlU );
                    }
                }
                catch ( SQLException e2 )
                {
                    log.error( "e2 sql [" + sqlU + "] Exception: ", e2 );
                }
            }
        }
        finally
        {
            try
            {
                con.close();
            }
            catch ( SQLException e )
            {
                log.error( "Problem closing connection.", e );
            }
        }

        if ( log.isInfoEnabled() )
        {
            if ( updateCount % LOG_INTERVAL == 0 )
            {
                // TODO make a log stats method
                log.info( "Update Count [" + updateCount + "]" );
            }
        }
    }

    /**
     * Does an element exist for this key?
     * <p>
     * @param ce
     * @return boolean
     */
    protected boolean doesElementExist( ICacheElement ce )
    {
        boolean exists = false;

        Connection con;
        try
        {
            con = poolAccess.getConnection();
        }
        catch ( SQLException e )
        {
            log.error( "Problem getting conenction.", e );
            return exists;
        }

        PreparedStatement psSelect = null;
        try
        {
            // don't select the element, since we want this to be fast.
            String sqlS = "select CACHE_KEY from " + getJdbcDiskCacheAttributes().getTableName()
                + " where REGION = ? and CACHE_KEY = ?";

            psSelect = con.prepareStatement( sqlS );
            psSelect.setString( 1, this.getCacheName() );
            psSelect.setString( 2, (String) ce.getKey() );

            ResultSet rs = psSelect.executeQuery();

            if ( rs.next() )
            {
                exists = true;
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "[" + ce.getKey() + "] existing status is " + exists );
            }

            rs.close();
        }
        catch ( SQLException e )
        {
            log.error( "Problem looking for item before insert.", e );
        }
        finally
        {
            try
            {
                if ( psSelect != null )
                {
                    psSelect.close();
                }
                psSelect.close();
            }
            catch ( SQLException e1 )
            {
                log.error( "Problem closing statement.", e1 );
            }

            try
            {
                con.close();
            }
            catch ( SQLException e )
            {
                log.error( "Problem closing connection.", e );
            }
        }

        return exists;
    }

    /**
     * Queries the database for the value. If it gets a result, the value is
     * deserialized.
     * <p>
     * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doGet(java.io.Serializable)
     */
    public ICacheElement doGet( Serializable key )
    {
        incrementGetCount();

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
            // region, key
            String selectString = "select ELEMENT from " + getJdbcDiskCacheAttributes().getTableName()
                + " where REGION = ? and CACHE_KEY = ?";

            Connection con = poolAccess.getConnection();
            try
            {
                PreparedStatement psSelect = null;
                try
                {
                    psSelect = con.prepareStatement( selectString );
                    psSelect.setString( 1, this.getCacheName() );
                    psSelect.setString( 2, key.toString() );

                    ResultSet rs = psSelect.executeQuery();
                    try
                    {
                        if ( rs.next() )
                        {
                            data = rs.getBytes( 1 );
                        }
                        if ( data != null )
                        {
                            try
                            {
                                // USE THE SERIALIZER
                                obj = (ICacheElement) getElementSerializer().deSerialize( data );
                            }
                            catch ( IOException ioe )
                            {
                                log.error( ioe );
                            }
                            catch ( Exception e )
                            {
                                log.error( "Problem getting item.", e );
                            }
                        }
                    }
                    finally
                    {
                        if ( rs != null )
                        {
                            rs.close();
                        }
                        rs.close();
                    }
                }
                finally
                {
                    if ( psSelect != null )
                    {
                        psSelect.close();
                    }
                    psSelect.close();
                }
            }
            finally
            {
                if ( con != null )
                {
                    con.close();
                }
            }
        }
        catch ( SQLException sqle )
        {
            log.error( sqle );
        }

        if ( log.isInfoEnabled() )
        {
            if ( getCount % LOG_INTERVAL == 0 )
            {
                // TODO make a log stats method
                log.info( "Get Count [" + getCount + "]" );
            }
        }

        return obj;
    }

    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     * <p>
     * @param key
     * @return boolean
     */
    public boolean doRemove( Serializable key )
    {
        // remove single item.
        String sql = "delete from " + getJdbcDiskCacheAttributes().getTableName()
            + " where REGION = ? and CACHE_KEY = ?";

        try
        {
            boolean partial = false;
            if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.
                sql = "delete from " + getJdbcDiskCacheAttributes().getTableName()
                    + " where REGION = ? and CACHE_KEY like ?";
                partial = true;
            }
            Connection con = poolAccess.getConnection();
            PreparedStatement psSelect = null;
            try
            {
                psSelect = con.prepareStatement( sql );
                psSelect.setString( 1, this.getCacheName() );
                if ( partial )
                {
                    psSelect.setString( 2, key.toString() + "%" );
                }
                else
                {
                    psSelect.setString( 2, key.toString() );
                }

                psSelect.executeUpdate( );

                alive = true;
            }
            catch ( SQLException e )
            {
                log.error( "Problem creating statement. sql [" + sql + "]", e );
                alive = false;
            }
            finally
            {
                try
                {
                    if ( psSelect != null )
                    {
                        psSelect.close();
                    }
                    con.close();
                }
                catch ( SQLException e1 )
                {
                    log.error( "Problem closing statement.", e1 );
                }
            }

        }
        catch ( Exception e )
        {
            log.error( "Problem updating cache.", e );
            reset();
        }
        return false;
    }

    /** This should remove all elements. */
    public void doRemoveAll()
    {
        // it should never get here formt he abstract dis cache.
        if ( this.jdbcDiskCacheAttributes.isAllowRemoveAll() )
        {
            try
            {
                String sql = "delete from " + getJdbcDiskCacheAttributes().getTableName() + " where REGION = ?";
                Connection con = poolAccess.getConnection();
                PreparedStatement psDelete = null;
                try
                {
                    psDelete = con.prepareStatement( sql );
                    psDelete.setString( 1, this.getCacheName() );
                    alive = true;
                    psDelete.executeUpdate( );
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
                        if ( psDelete != null )
                        {
                            psDelete.close();
                        }
                        con.close();
                    }
                    catch ( SQLException e1 )
                    {
                        log.error( "Problem closing statement.", e1 );
                    }
                }
            }
            catch ( Exception e )
            {
                log.error( "Problem removing all.", e );
                reset();
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "RemoveAll was requested but the request was not fulfilled: allowRemoveAll is set to false." );
            }
        }
    }

    /**
     * Removed the expired. (now - create time) > max life seconds * 1000
     * <p>
     * @return the number deleted
     */
    protected int deleteExpired()
    {
        int deleted = 0;

        try
        {
            getTableState().setState( TableState.DELETE_RUNNING );

            long now = System.currentTimeMillis() / 1000;

            // This is to slow when we push over a million records
            // String sql = "delete from " +
            // getJdbcDiskCacheAttributes().getTableName() + " where REGION = '"
            // + this.getCacheName() + "' and IS_ETERNAL = 'F' and (" + now
            // + " - CREATE_TIME_SECONDS) > MAX_LIFE_SECONDS";

            String sql = "delete from " + getJdbcDiskCacheAttributes().getTableName()
                + " where IS_ETERNAL = ? and REGION = ? and ? > SYSTEM_EXPIRE_TIME_SECONDS";

            Connection con = poolAccess.getConnection();
            PreparedStatement psDelete = null;
            try
            {
                psDelete = con.prepareStatement( sql );
                psDelete.setString( 1, "F" );
                psDelete.setString( 2, this.getCacheName() );
                psDelete.setLong( 3, now );

                alive = true;

                deleted = psDelete.executeUpdate( );
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
                    if ( psDelete != null )
                    {
                        psDelete.close();
                    }
                    con.close();
                }
                catch ( SQLException e1 )
                {
                    log.error( "Problem closing statement.", e1 );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem removing expired elements from the table.", e );
            reset();
        }
        finally
        {
            getTableState().setState( TableState.FREE );
        }

        return deleted;
    }

    /**
     * Typically this is used to handle errors by last resort, force content
     * update, or removeall
     */
    public void reset()
    {
        // nothing
    }

    /** Shuts down the pool */
    public void doDispose()
    {
        try
        {
            poolAccess.shutdownDriver();
        }
        catch ( Exception e )
        {
            log.error( "Problem shutting down.", e );
        }
    }

    /**
     * Returns the current cache size. Just does a count(*) for the region.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        int size = 0;

        // region, key
        String selectString = "select count(*) from " + getJdbcDiskCacheAttributes().getTableName()
            + " where REGION = ?";

        Connection con;
        try
        {
            con = poolAccess.getConnection();
        }
        catch ( SQLException e1 )
        {
            log.error( "Problem getting conenction.", e1 );
            return size;
        }
        try
        {
            PreparedStatement psSelect = null;
            try
            {
                psSelect = con.prepareStatement( selectString );
                psSelect.setString( 1, this.getCacheName() );
                ResultSet rs = null;

                rs = psSelect.executeQuery();
                try
                {
                    if ( rs.next() )
                    {
                        size = rs.getInt( 1 );
                    }
                }
                finally
                {
                    if ( rs != null )
                    {
                        rs.close();
                    }
                    rs.close();
                }
            }
            finally
            {
                if ( psSelect != null )
                {
                    psSelect.close();
                }
                psSelect.close();
            }
        }
        catch ( SQLException e )
        {
            log.error( "Problem getting size.", e );
        }
        finally
        {
            try
            {
                con.close();
            }
            catch ( SQLException e )
            {
                log.error( "Problem closing connection.", e );
            }
        }
        return size;
    }

    /**
     * Returns the serialized form of the given object in a byte array.
     * <p>
     * @param obj
     * @return byte[]
     * @throws IOException
     */
    protected byte[] serialize( Serializable obj )
        throws IOException
    {
        return getElementSerializer().serialize( obj );
    }

    /**
     * @param groupName
     * @return
     */
    public Set getGroupKeys( String groupName )
    {
        if ( true )
        {
            throw new UnsupportedOperationException( "Groups not implemented." );
        }
        return null;
    }

    /**
     * @param elementSerializer
     *            The elementSerializer to set.
     */
    public void setElementSerializer( IElementSerializer elementSerializer )
    {
        this.elementSerializer = elementSerializer;
    }

    /**
     * @return Returns the elementSerializer.
     */
    public IElementSerializer getElementSerializer()
    {
        return elementSerializer;
    }

    /** safely increment */
    private synchronized void incrementUpdateCount()
    {
        updateCount++;
    }

    /** safely increment */
    private synchronized void incrementGetCount()
    {
        getCount++;
    }

    /**
     * @param jdbcDiskCacheAttributes
     *            The jdbcDiskCacheAttributes to set.
     */
    protected void setJdbcDiskCacheAttributes( JDBCDiskCacheAttributes jdbcDiskCacheAttributes )
    {
        this.jdbcDiskCacheAttributes = jdbcDiskCacheAttributes;
    }

    /**
     * @return Returns the jdbcDiskCacheAttributes.
     */
    protected JDBCDiskCacheAttributes getJdbcDiskCacheAttributes()
    {
        return jdbcDiskCacheAttributes;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.getJdbcDiskCacheAttributes();
    }

    /**
     * Extends the parent stats.
     */
    public IStats getStatistics()
    {
        IStats stats = super.getStatistics();
        stats.setTypeName( "JDBC/Abstract Disk Cache" );
        stats.getStatElements();

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Update Count" );
        se.setData( "" + updateCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Get Count" );
        se.setData( "" + getCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Size" );
        se.setData( "" + getSize() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Active DB Connections" );
        se.setData( "" + poolAccess.getNumActiveInPool() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Idle DB Connections" );
        se.setData( "" + poolAccess.getNumIdleInPool() );
        elems.add( se );

        se = new StatElement();
        se.setName( "DB URL" );
        se.setData( this.jdbcDiskCacheAttributes.getUrl() );
        elems.add( se );

        // get the stats from the event queue too
        // get as array, convert to list, add list to our outer list
        IStatElement[] eqSEs = stats.getStatElements();
        List eqL = Arrays.asList( eqSEs );
        elems.addAll( eqL );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

    /**
     * Returns the name of the table.
     * <p>
     * @return the table name or UNDEFINED
     */
    protected String getTableName()
    {
        String name = "UNDEFINED";
        if ( this.getJdbcDiskCacheAttributes() != null )
        {
            name = this.getJdbcDiskCacheAttributes().getTableName();
        }
        return name;
    }

    /**
     * @param tableState The tableState to set.
     */
    public void setTableState( TableState tableState )
    {
        this.tableState = tableState;
    }

    /**
     * @return Returns the tableState.
     */
    public TableState getTableState()
    {
        return tableState;
    }

    /**
     * For debugging.
     */
    public String toString()
    {
        return this.getStats();
    }
}
