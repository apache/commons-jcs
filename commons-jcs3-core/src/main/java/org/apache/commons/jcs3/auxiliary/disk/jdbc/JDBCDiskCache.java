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

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs3.engine.behavior.ICache;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;

/**
 * This is the jdbc disk cache plugin.
 * <p>
 * It expects a table created by the following script. The table name is configurable.
 * </p>
 * <pre>
 *                       drop TABLE JCS_STORE;
 *                       CREATE TABLE JCS_STORE
 *                       (
 *                       CACHE_KEY                  VARCHAR(250)          NOT NULL,
 *                       REGION                     VARCHAR(250)          NOT NULL,
 *                       ELEMENT                    BLOB,
 *                       CREATE_TIME                TIMESTAMP,
 *                       UPDATE_TIME_SECONDS        BIGINT,
 *                       MAX_LIFE_SECONDS           BIGINT,
 *                       SYSTEM_EXPIRE_TIME_SECONDS BIGINT,
 *                       IS_ETERNAL                 CHAR(1),
 *                       PRIMARY KEY (CACHE_KEY, REGION)
 *                       );
 * </pre>
 * <p>
 * The cleanup thread will delete non eternal items where (now - create time) &gt; max life seconds *
 * 1000
 * </p>
 * <p>
 * To speed up the deletion the SYSTEM_EXPIRE_TIME_SECONDS is used instead. It is recommended that
 * an index be created on this column is you will have over a million records.
 * </p>
 */
public class JDBCDiskCache<K, V>
    extends AbstractDiskCache<K, V>
{
    /** The local logger. */
    private static final Log log = Log.getLog( JDBCDiskCache.class );

    /** Configuration */
    private JDBCDiskCacheAttributes jdbcDiskCacheAttributes;

    /** # of times update was called */
    private final AtomicInteger updateCount = new AtomicInteger();

    /** # of times get was called */
    private final AtomicInteger getCount = new AtomicInteger();

    /** # of times getMatching was called */
    private final AtomicInteger getMatchingCount = new AtomicInteger();

    /** Db connection pool */
    private final DataSourceFactory dsFactory;

    /** Tracks optimization */
    private TableState tableState;

    /** SQL prepared statements */
    private static final String SQL_DELETE_EXPIRED =
        "delete from %s where IS_ETERNAL = ? and REGION = ? and ? > SYSTEM_EXPIRE_TIME_SECONDS";
    private static final String SQL_GET_SIZE = "select count(*) from %s where REGION = ?";
    private static final String SQL_DOES_ELEMENT_EXIST = "select CACHE_KEY from %s where REGION = ? and CACHE_KEY = ?";
    private static final String SQL_REMOVE_ALL = "delete from %s where REGION = ?";
    private static final String SQL_REMOVE_PARTIAL = "delete from %s where REGION = ? and CACHE_KEY like ?";
    private static final String SQL_REMOVE_SINGLE = "delete from %s where REGION = ? and CACHE_KEY = ?";
    private static final String SQL_GET_ELEMENT_MATCHING = "select ELEMENT from %s where REGION = ? and CACHE_KEY like ?";
    private static final String SQL_GET_ELEMENT = "select ELEMENT from %s where REGION = ? and CACHE_KEY = ?";
    private static final String SQL_INSERT_ROW = """
        insert into %s\
         (CACHE_KEY, REGION, ELEMENT, MAX_LIFE_SECONDS, IS_ETERNAL, CREATE_TIME, UPDATE_TIME_SECONDS,\
         SYSTEM_EXPIRE_TIME_SECONDS)\s\
         values (?, ?, ?, ?, ?, ?, ?, ?)""";

    private static final String SQL_UPDATE_ROW = """
            update %s\
             set ELEMENT  = ?, CREATE_TIME = ?, UPDATE_TIME_SECONDS = ?,\s\
             SYSTEM_EXPIRE_TIME_SECONDS = ?\s\
             where CACHE_KEY = ? and REGION = ?""";

    /**
     * Constructs a JDBC Disk Cache for the provided cache attributes. The table state object is
     * used to mark deletions.
     *
     * @param cattr the configuration object for this cache
     * @param dsFactory the DataSourceFactory for this cache
     * @param tableState an object to track table operations
     */
    public JDBCDiskCache(final JDBCDiskCacheAttributes cattr, final DataSourceFactory dsFactory, final TableState tableState)
    {
        super( cattr );

        setTableState( tableState );
        setJdbcDiskCacheAttributes( cattr );

        log.info( "jdbcDiskCacheAttributes = {0}", this::getJdbcDiskCacheAttributes);

        // This initializes the pool access.
        this.dsFactory = dsFactory;

        // Initialization finished successfully, so set alive to true.
        setAlive(true);
    }

    /**
     * @param pattern
     * @return String to use in the like query.
     */
    protected String constructLikeParameterFromPattern( final String pattern )
    {
        String likePattern = pattern.replace( ".+", "%" );
        likePattern = likePattern.replace( ".", "_" );

        log.debug( "pattern = [{0}]", likePattern );

        return likePattern;
    }

    /**
     * Get a connection from the data source and prepare it for use
     *
     * @return A database connection
     * @throws SQLException if getting the connection goes wrong
     */
    protected JDBCConnection getConnection() throws SQLException
    {
        return new JDBCConnection(getDataSource().getConnection());
    }

    /**
     * Removed the expired. (now - create time) &gt; max life seconds * 1000
     *
     * @return the number deleted
     */
    protected int deleteExpired()
    {
        int deleted = 0;

        try (JDBCConnection con = getConnection())
        {
            // The shrinker thread might kick in before the table is created
            // So check if the table exists first
            final DatabaseMetaData dmd = con.getMetaData();
            final ResultSet result = dmd.getTables(null, null, getTableName(), null);

            if (result.next())
            {
                getTableState().setState( TableState.DELETE_RUNNING );
                final long now = System.currentTimeMillis() / 1000;

                final String sql = String.format(SQL_DELETE_EXPIRED, getTableName());

                try (PreparedStatement psDelete = con.prepareStatement( sql ))
                {
                    psDelete.setString( 1, "F" );
                    psDelete.setString( 2, getCacheName() );
                    psDelete.setLong( 3, now );

                    setAlive(true);

                    deleted = psDelete.executeUpdate();
                }
                catch ( final SQLException e )
                {
                    log.error( "Problem creating statement.", e );
                    setAlive(false);
                }

                logApplicationEvent( getAuxiliaryCacheAttributes().getName(), "deleteExpired",
                                     "Deleted expired elements.  URL: " + getDiskLocation() );
            }
            else
            {
                log.warn( "Trying to shrink non-existing table [{0}]", getTableName() );
            }
        }
        catch ( final SQLException e )
        {
            logError( getAuxiliaryCacheAttributes().getName(), "deleteExpired",
                    e.getMessage() + " URL: " + getDiskLocation() );
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
     * Does an element exist for this key?
     *
     * @param ce the cache element
     * @param con a database connection
     * @return boolean
     */
    protected boolean doesElementExist( final ICacheElement<K, V> ce, final JDBCConnection con )
    {
        boolean exists = false;
        // don't select the element, since we want this to be fast.
        final String sqlS = String.format(SQL_DOES_ELEMENT_EXIST, getTableName());

        try (PreparedStatement psSelect = con.prepareStatement( sqlS ))
        {
            psSelect.setString( 1, getCacheName() );
            psSelect.setString( 2, (String) ce.getKey() );

            try (ResultSet rs = psSelect.executeQuery())
            {
                exists = rs.next();
            }

            log.debug( "[{0}] existing status is {1}", ce.getKey(), exists );
        }
        catch ( final SQLException e )
        {
            log.error( "Problem looking for item before insert.", e );
        }

        return exists;
    }

    /**
     * @return the AuxiliaryCacheAttributes.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.getJdbcDiskCacheAttributes();
    }

    /**
     * Public so managers can access it.
     * @return the dsFactory
     * @throws SQLException if getting a data source fails
     */
    public DataSource getDataSource() throws SQLException
    {
        return dsFactory.getDataSource();
    }

    /**
     * This is used by the event logging.
     *
     * @return the location of the disk, either path or ip.
     */
    @Override
    protected String getDiskLocation()
    {
        return this.jdbcDiskCacheAttributes.getUrl();
    }

    /**
     * @return the jdbcDiskCacheAttributes.
     */
    protected JDBCDiskCacheAttributes getJdbcDiskCacheAttributes()
    {
        return jdbcDiskCacheAttributes;
    }

    /**
     * @param jdbcDiskCacheAttributes The jdbcDiskCacheAttributes to set.
     */
    protected void setJdbcDiskCacheAttributes( final JDBCDiskCacheAttributes jdbcDiskCacheAttributes )
    {
        this.jdbcDiskCacheAttributes = jdbcDiskCacheAttributes;
    }

    /**
     * Return the keys in this cache.
     *
     * @see org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        throw new UnsupportedOperationException( "Groups not implemented." );
        // return null;
    }

    /**
     * Returns the current cache size. Just does a count(*) for the region.
     *
     * @return The size value
     */
    @Override
    public int getSize()
    {
        int size = 0;

        // region, key
        final String selectString = String.format(SQL_GET_SIZE, getTableName());

        try (JDBCConnection con = getConnection())
        {
            try (PreparedStatement psSelect = con.prepareStatement( selectString ))
            {
                psSelect.setString( 1, getCacheName() );

                try (ResultSet rs = psSelect.executeQuery())
                {
                    if ( rs.next() )
                    {
                        size = rs.getInt( 1 );
                    }
                }
            }
        }
        catch ( final SQLException e )
        {
            log.error( "Problem getting size.", e );
        }

        return size;
    }

    /**
     * Extends the parent stats.
     *
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();
        stats.setTypeName( "JDBC/Abstract Disk Cache" );

        final List<IStatElement<?>> elems = stats.getStatElements();

        elems.add(new StatElement<>( "Update Count", updateCount ) );
        elems.add(new StatElement<>( "Get Count", getCount ) );
        elems.add(new StatElement<>( "Get Matching Count", getMatchingCount ) );
        elems.add(new StatElement<>( "DB URL", getDiskLocation()) );

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * Returns the name of the table.
     *
     * @return the table name or UNDEFINED
     */
    protected String getTableName()
    {
        return this.jdbcDiskCacheAttributes != null ?
                this.jdbcDiskCacheAttributes.getTableName() : "UNDEFINED";
    }

    /**
     * @return the tableState.
     */
    public TableState getTableState()
    {
        return tableState;
    }

    /**
     * @param tableState The tableState to set.
     */
    public void setTableState( final TableState tableState )
    {
        this.tableState = tableState;
    }

    /**
     * If test before insert it true, we check to see if the element exists. If the element exists
     * we will update. Otherwise, we try inserting.  If this fails because the item exists, we will
     * update.
     *
     * @param ce
     * @param con
     * @param element
     */
    private void insertOrUpdate( final ICacheElement<K, V> ce, final JDBCConnection con, final byte[] element )
    {
        boolean exists = false;

        // First do a query to determine if the element already exists
        if ( this.getJdbcDiskCacheAttributes().isTestBeforeInsert() )
        {
            exists = doesElementExist( ce, con );
        }

        // If it doesn't exist, insert it, otherwise update
        if ( !exists )
        {
            exists = insertRow( ce, con, element );
        }

        // update if it exists.
        if ( exists )
        {
            updateRow( ce, con, element );
        }
    }

    /**
     * This inserts a new row in the database.
     *
     * @param ce
     * @param con
     * @param element
     * @return true if the insertion fails because the record exists.
     */
    private boolean insertRow( final ICacheElement<K, V> ce, final JDBCConnection con, final byte[] element )
    {
        boolean exists = false;
        final String sqlI = String.format(SQL_INSERT_ROW, getTableName());

        try (PreparedStatement psInsert = con.prepareStatement( sqlI ))
        {
            psInsert.setString( 1, ce.getKey().toString() );
            psInsert.setString( 2, getCacheName() );
            psInsert.setBytes( 3, element );
            psInsert.setLong( 4, ce.getElementAttributes().getMaxLife() );
            psInsert.setString( 5, ce.getElementAttributes().getIsEternal() ? "T" : "F" );

            final Timestamp createTime = new Timestamp( ce.getElementAttributes().getCreateTime() );
            psInsert.setTimestamp( 6, createTime );

            final long now = System.currentTimeMillis() / 1000;
            psInsert.setLong( 7, now );

            final long expireTime = now + ce.getElementAttributes().getMaxLife();
            psInsert.setLong( 8, expireTime );

            psInsert.execute();
        }
        catch ( final SQLException e )
        {
            if ("23000".equals(e.getSQLState()))
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
                exists = doesElementExist( ce, con );
            }
        }

        return exists;
    }

    /**
     * This updates a row in the database.
     *
     * @param ce
     * @param con
     * @param element
     */
    private void updateRow( final ICacheElement<K, V> ce, final JDBCConnection con, final byte[] element )
    {
        final String sqlU = String.format(SQL_UPDATE_ROW, getTableName());

        try (PreparedStatement psUpdate = con.prepareStatement( sqlU ))
        {
            psUpdate.setBytes( 1, element );

            final Timestamp createTime = new Timestamp( ce.getElementAttributes().getCreateTime() );
            psUpdate.setTimestamp( 2, createTime );

            final long now = System.currentTimeMillis() / 1000;
            psUpdate.setLong( 3, now );

            final long expireTime = now + ce.getElementAttributes().getMaxLife();
            psUpdate.setLong( 4, expireTime );

            psUpdate.setString( 5, (String) ce.getKey() );
            psUpdate.setString( 6, getCacheName() );
            psUpdate.execute();

            log.debug( "ran update {0}", sqlU );
        }
        catch ( final SQLException e )
        {
            log.error( "Error executing update sql [{0}]", sqlU, e );
        }
    }

    /** Shuts down the pool */
    @Override
    public void processDispose()
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( getCacheName(), null, ICacheEventLogger.DISPOSE_EVENT );

        try
        {
        	dsFactory.close();
        }
        catch ( final SQLException e )
        {
            log.error( "Problem shutting down.", e );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Queries the database for the value. If it gets a result, the value is deserialized.
     *
     * @param key
     * @return ICacheElement
     * @see org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache#get(Object)
     */
    @Override
    protected ICacheElement<K, V> processGet( final K key )
    {
    	getCount.incrementAndGet();

        log.debug( "Getting [{0}] from disk", key );

        if ( !isAlive() )
        {
            return null;
        }

        ICacheElement<K, V> obj = null;

        // region, key
        final String selectString = String.format(SQL_GET_ELEMENT, getTableName());

        try (JDBCConnection con = getConnection())
        {
            try (PreparedStatement psSelect = con.prepareStatement( selectString ))
            {
                psSelect.setString( 1, getCacheName() );
                psSelect.setString( 2, key.toString() );

                try (ResultSet rs = psSelect.executeQuery())
                {
                    byte[] data = null;

                    if ( rs.next() )
                    {
                        data = rs.getBytes( 1 );
                    }

                    if ( data != null )
                    {
                        try
                        {
                            // USE THE SERIALIZER
                            obj = getElementSerializer().deSerialize( data, null );
                        }
                        catch ( final IOException | ClassNotFoundException e )
                        {
                            log.error( "Problem getting item for key [{0}]", key, e );
                        }
                    }
                }
            }

            con.commit();
        }
        catch ( final SQLException sqle )
        {
            log.error( "Caught a SQL exception trying to get the item for key [{0}]",
                    key, sqle );
        }

        return obj;
    }

    /**
     * This will run a like query. It will try to construct a usable query but different
     * implementations will be needed to adjust the syntax.
     *
     * @param pattern
     * @return key,value map
     */
    @Override
    protected Map<K, ICacheElement<K, V>> processGetMatching( final String pattern )
    {
    	getMatchingCount.incrementAndGet();

        log.debug( "Getting [{0}] from disk", pattern);

        if ( !isAlive() )
        {
            return null;
        }

        final Map<K, ICacheElement<K, V>> results = new HashMap<>();

        // region, key
        final String selectString = String.format(SQL_GET_ELEMENT_MATCHING, getTableName());

        try (JDBCConnection con = getConnection())
        {
            try (PreparedStatement psSelect = con.prepareStatement( selectString ))
            {
                psSelect.setString( 1, getCacheName() );
                psSelect.setString( 2, constructLikeParameterFromPattern( pattern ) );

                try (ResultSet rs = psSelect.executeQuery())
                {
                    while ( rs.next() )
                    {
                        final byte[] data = rs.getBytes(1);
                        if ( data != null )
                        {
                            try
                            {
                                // USE THE SERIALIZER
                                final ICacheElement<K, V> value = getElementSerializer().deSerialize( data, null );
                                results.put( value.getKey(), value );
                            }
                            catch ( final IOException | ClassNotFoundException e )
                            {
                                log.error( "Problem getting items for pattern [{0}]", pattern, e );
                            }
                        }
                    }
                }
            }

            con.commit();
        }
        catch ( final SQLException sqle )
        {
            log.error( "Caught a SQL exception trying to get items for pattern [{0}]",
                    pattern, sqle );
        }

        return results;
    }

    /**
     * Returns true if the removal was successful; or false if there is nothing to remove. Current
     * implementation always results in a disk orphan.
     *
     * @param key
     * @return boolean
     */
    @Override
    protected boolean processRemove( final K key )
    {
        // remove single item.
        final String sqlSingle = String.format(SQL_REMOVE_SINGLE, getTableName());
        // remove all keys of the same name group.
        final String sqlPartial = String.format(SQL_REMOVE_PARTIAL, getTableName());

        try (JDBCConnection con = getConnection())
        {
            final boolean partial = key.toString().endsWith(ICache.NAME_COMPONENT_DELIMITER);
            final String sql = partial ? sqlPartial : sqlSingle;

            try (PreparedStatement psSelect = con.prepareStatement(sql))
            {
                psSelect.setString( 1, getCacheName() );
                if ( partial )
                {
                    psSelect.setString( 2, key.toString() + "%" );
                }
                else
                {
                    psSelect.setString( 2, key.toString() );
                }

                psSelect.executeUpdate();

                setAlive(true);
                con.commit();
            }
            catch ( final SQLException e )
            {
                log.error( "Problem creating statement. sql [{0}]", sql, e );
                setAlive(false);
            }
        }
        catch ( final SQLException e )
        {
            log.error( "Problem updating cache.", e );
            reset();
        }
        return false;
    }

    /**
     * This should remove all elements. The auxiliary can be configured to forbid this behavior. If
     * remove all is not allowed, the method balks.
     */
    @Override
    protected void processRemoveAll()
    {
        // it should never get here from the abstract disk cache.
        if ( this.jdbcDiskCacheAttributes.isAllowRemoveAll() )
        {
            final String sql = String.format(SQL_REMOVE_ALL, getTableName());

            try (JDBCConnection con = getConnection())
            {
                try (PreparedStatement psDelete = con.prepareStatement( sql ))
                {
                    psDelete.setString( 1, getCacheName() );
                    setAlive(true);
                    psDelete.executeUpdate();
                }
                catch ( final SQLException e )
                {
                    log.error( "Problem creating statement.", e );
                    setAlive(false);
                }

                con.commit();
            }
            catch ( final SQLException e )
            {
                log.error( "Problem removing all.", e );
                reset();
            }
        }
        else
        {
            log.info( "RemoveAll was requested but the request was not fulfilled: "
                    + "allowRemoveAll is set to false." );
        }
    }

    /**
     * Inserts or updates. By default it will try to insert. If the item exists we will get an
     * error. It will then update. This behavior is configurable. The cache can be configured to
     * check before inserting.
     *
     * @param ce
     */
    @Override
    protected void processUpdate( final ICacheElement<K, V> ce )
    {
    	updateCount.incrementAndGet();

        log.debug( "updating, ce = {0}", ce );

        try (JDBCConnection con = getConnection())
        {
            log.debug( "Putting [{0}] on disk.", ce::getKey);

            try
            {
                final byte[] element = getElementSerializer().serialize( ce );
                insertOrUpdate( ce, con, element );
            }
            catch ( final IOException e )
            {
                log.error( "Could not serialize element", e );
            }

            con.commit();
        }
        catch ( final SQLException e )
        {
            log.error( "Problem getting connection.", e );
        }
    }

    /**
     * Typically this is used to handle errors by last resort, force content update, or removeall
     */
    public void reset()
    {
        // nothing
    }

    /**
     * For debugging.
     *
     * @return this.getStats();
     */
    @Override
    public String toString()
    {
        return getStats();
    }
}
