package org.apache.jcs.auxiliary.disk.hsql;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;

import org.apache.jcs.auxiliary.disk.hsql.behavior.IHSQLCacheAttributes;
import org.apache.jcs.auxiliary.disk.PurgatoryElement;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.jcs.utils.data.PropertyGroups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Aaron Smuts
 * @created January 15, 2002
 * @version 1.0
 */
public class HSQLCache implements ICache, Serializable
{
    private final static Log log =
        LogFactory.getLog( HSQLCache.class );

    private static int numCreated = 0;
    private int numInstances = 0;

    private String cacheName;

    /** Description of the Field */
    public boolean isAlive = false;

    IHSQLCacheAttributes cattr;

    // disk cache buffer, need to remove key from buffer on update, if its there
    HSQLCacheNoWaitBuffer buffer;

    // for now use one statement per cache and keep it open
    // can move up to manager level or implement pooling if there are too many
    // caches
    Connection cConn;
    Statement sStatement;

    //PreparedStatement psInsert;

    //PreparedStatement psUpdate;
    //PreparedStatement psSelect;

    // should use this method
    /**
     * Constructor for the HSQLCache object
     *
     * @param buffer
     * @param cattr
     */
    public HSQLCache( HSQLCacheNoWaitBuffer buffer, IHSQLCacheAttributes cattr )
    {
        this( cattr.getCacheName(), cattr.getDiskPath() );
        this.cattr = cattr;
        this.buffer = buffer;
    }


    /**
     * Constructor for the HSQLCache object
     *
     * @param cacheName
     */
    protected HSQLCache( String cacheName )
    {
        this( cacheName, null );
    }


    /**
     * Constructor for the HSQLCache object
     *
     * @param cacheName
     * @param rafroot
     */
    protected HSQLCache( String cacheName, String rafroot )
    {
        numInstances++;

        this.cacheName = cacheName;

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

        try
        {

            Properties p = new Properties();
            String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
            String url = p.getProperty( "url", "jdbc:hsqldb:" );
            String database = p.getProperty( "database", "cache_hsql_db" );
            String user = p.getProperty( "user", "sa" );
            String password = p.getProperty( "password", "" );
            boolean test = p.getProperty( "test", "true" ).equalsIgnoreCase( "true" );
            // boolean log = p.getProperty( "log", "true" ).equalsIgnoreCase( "true" );

            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "driver  =" + driver
                         + ", url = " + url
                         + ", database = " + database
                         + ", user = " + user
                         + ", password = " + password
                         + ", test = " + test );
                }

                // As described in the JDBC FAQ:
                // http://java.sun.com/products/jdbc/jdbc-frequent.html;
                // Why doesn't calling class.forName() load my JDBC driver?
                // There is a bug in the JDK 1.1.x that can cause Class.forName() to fail.
                new org.hsqldb.jdbcDriver();
                Class.forName( driver ).newInstance();

                cConn = DriverManager.getConnection( url + database, user,
                    password );

                try
                {
                    sStatement = cConn.createStatement();
                    isAlive = true;
                }
                catch ( SQLException e )
                {
                    System.out.println( "Exception: " + e );
                    isAlive = false;
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
            log.error( e );
        }

    }
    // end constructor

    /** SETUP TABLE FOR CACHE */
    void setupTABLE()
    {

        boolean newT = true;

        String setup = "create table " + cacheName + " (KEY varchar(255) primary key, ELEMENT binary)";

        try
        {
            sStatement.executeQuery( setup );
        }
        catch ( SQLException e )
        {
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            log.error( e );
        }

        String setupData[] = {
            "create index iKEY on " + cacheName + " (KEY)"
            };

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
        }
        // end ifnew

    }


    /** Description of the Method */
    public void add( Serializable key, Serializable value )
        throws IOException
    {
        put( key, value );
    }


    // ignore the multicast field.
    /** Description of the Method */
    public void put( Serializable key, Serializable value, boolean multicast )
        throws IOException
    {
        put( key, value );
    }


    /** Description of the Method */
    public void put( Serializable key, Serializable value )
        throws IOException
    {
        put( key, value, null );
    }


    /** Description of the Method */
    public void put( Serializable key, Serializable value, IElementAttributes attr )
        throws IOException
    {
        CacheElement ce = null;
        ce = new CacheElement( cacheName, key, value );
        ce.setElementAttributes( attr );
        update( ce );
    }


    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        log.debug( "update" );

        if ( !isAlive )
        {
            return;
        }

        if ( ce instanceof PurgatoryElement )
        {
            PurgatoryElement pe = ( PurgatoryElement ) ce;
            ce = pe.getCacheElement();
            if ( ! pe.isSpoolable() )
            {
                log.debug( "pe is not spoolable" );

                // it has been plucked from purgatory
                return;
            }
        }
//    /*
//    if ( ce instanceof IDiskElement ) {
//      IDiskElement ide = (IDiskElement)ce;
//      if ( !ide.getIsSpoolable() ) {
//        // it has been plucked from purgatory
//        return;
//      }
//    }
//    */
        // remove item from purgatory since we are putting it on disk
        // assume that the disk cache will never break
        // disk breakage is akin to an out of memory exception
        buffer.purgatory.remove( ce.getKey() );

        if ( log.isDebugEnabled() )
        {
            log.debug( "putting " + ce.getKey() + " on disk, removing from purgatory" );
        }

        // remove single item.
        byte[] element = serialize( ce );
        //String sql = "insert into " + cacheName + " (KEY, ELEMENT) values ('" + ce.getKey() + "', '" + element + "' )";

        boolean exists = false;

        try
        {
            //String sqlS = "select ELEMENT from " + cacheName + " where KEY = ?";
            //PreparedStatement psSelect = cConn.prepareStatement(sqlS);
            //psSelect.setString(1,(String)ce.getKey());
            //ResultSet rs =  psSelect.executeQuery();

            String sqlS = "select ELEMENT from " + cacheName + " where KEY = '" + ( String ) ce.getKey() + "'";
            ResultSet rs = sStatement.executeQuery( sqlS );

            if ( rs.next() )
            {
                exists = true;
            }
            rs.close();
            //psSelect.close();
        }
        catch ( SQLException e )
        {
            log.error( e );
        }

        if ( !exists )
        {

            try
            {
                String sqlI = "insert into " + cacheName + " (KEY, ELEMENT) values (?, ? )";
                PreparedStatement psInsert = cConn.prepareStatement( sqlI );
                psInsert.setString( 1, ( String ) ce.getKey() );
                psInsert.setBytes( 2, element );
                psInsert.execute();
                psInsert.close();
                //sStatement.executeUpdate(sql);
            }
            catch ( SQLException e )
            {
                if ( e.toString().indexOf( "Violation of unique index" ) != -1 || e.getMessage().indexOf( "Violation of unique index" ) != -1 )
                {
                    exists = true;
                }
                else
                {
                    log.error( "Exception: " + e );
                }
            }

        }
        else
        {

            //sql = "update " + cacheName + " set ELEMENT = '" + element + "' where KEY = '" + ce.getKey() + "'";
            try
            {
                String sqlU = "update " + cacheName + " set ELEMENT  = ? ";
                PreparedStatement psUpdate = cConn.prepareStatement( sqlU );
                psUpdate.setBytes( 1, element );
                psUpdate.setString( 2, ( String ) ce.getKey() );
                psUpdate.execute();
                psUpdate.close();
                //sStatement.executeUpdate(sql);
                log.debug( "ran update" );
            }
            catch ( SQLException e2 )
            {
                log.error( "e2 Exception: " + e2 );
            }

        }

//    DiskElementDescriptor ded = null;
//    try {
//      ded = new DiskElementDescriptor();
//      byte[] data = Disk.serialize( ce );
//      ded.init( dataFile.length(), data );
//      // make sure this only locks for one particular cache region
//      locker.writeLock();
//      try {
//        if (!isAlive) {
//          return;
//        }
//        // Presume it's an append.
//        //DiskElement re = new DiskElement( cacheName, key, value );
//        //re.init( dataFile.length(), data );
//        DiskElementDescriptor old = (DiskElementDescriptor)keyHash.put(ce.getKey(), ded );
//        // Item with the same key already exists in file.
//        // Try to reuse the location if possible.
//        if (old != null && ded.len <= old.len) {
//          ded.pos = old.pos;
//        }
//        dataFile.write(data, ded.pos);
//        /*
//         // Only need to write if the item with the same key and value
//         // does not already exist in the file.
//         if (re.equals(old)) {
//         re.pos = old.pos;
//         }
//         else {
//         // Item with the same key but different value already exists in file.
//         // Try to reuse the location if possible.
//         if (old != null && re.len <= old.len) {
//         re.pos = old.pos;
//         }
//         dataFile.write(data, re.pos);
//         }
//         */
//      } finally {
//        locker.done();          // release write lock.
//      }
//      if ( log.isDebugEnabled() ) {
//        log.debug(fileName + " -- put " + ce.getKey() + " on disk at pos " + ded.pos + " size = " + ded.len );
//      }
//    } catch( ConcurrentModificationException cme ) {
//      // do nothing, this means it has gone back to memory mid serialization
//    } catch (Exception e) {
//      log.logEx(e, "cacheName = " + cacheName + ", ce.getKey() = " + ce.getKey());
//      //reset();
//    }
        return;
    }


    /** Description of the Method */
    public Serializable get( Serializable key )
    {
        return get( key, true, true );
    }


    /** Description of the Method */
    public Serializable get( Serializable key, boolean container )
    {
        return get( key, true, true );
    }


    /** Description of the Method */
    private Serializable get( Serializable key, boolean container, final boolean lock )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting " + key + " from disk" );
        }

        if ( !isAlive )
        {
            return null;
        }

        Serializable obj = null;

        byte[] data = null;
        try
        {
            String sqlS = "select ELEMENT from " + cacheName + " where KEY = ?";
            PreparedStatement psSelect = cConn.prepareStatement( sqlS );
            psSelect.setString( 1, ( String ) key );
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
                        obj = ( Serializable ) ois.readObject();
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

//    if (lock) {
//      locker.readLock();
//    }
//    try {
//      if (!isAlive) {
//        return  null;
//      }
//      DiskElementDescriptor ded = (DiskElementDescriptor)keyHash.get(key);
//      if (ded != null) {
//        if ( debugGet ) {
//          p( "found " + key + " on disk" );
//        }
//        obj = dataFile.readObject(ded.pos);
//      }
//      //System.out.println( "got element = " + (CacheElement)obj);
//    } catch (NullPointerException e) {
//      log.logEx(e, "cacheName = " + cacheName + ", key = " + key);
//    } catch (Exception e) {
//      log.logEx(e, "cacheName = " + cacheName + ", key = " + key);
//    } finally {
//      if (lock) {
//        locker.done();
//      }
//    }

        return obj;
    }


    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     */
    public boolean remove( Serializable key )
    {

        // remove single item.
        String sql = "delete from " + cacheName + " where KEY = '" + key + "'";

        try
        {

            if ( key instanceof String && key.toString().endsWith( NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.
                sql = "delete from " + cacheName + " where KEY = like '" + key + "%'";
            }

            try
            {
                sStatement.executeQuery( sql );
            }
            catch ( SQLException e )
            {
                log.error( e );
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
    public void removeAll()
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
    // end removeAll

    // handle error by last resort, force content update, or removeall
    /** Description of the Method */
    public void reset()
    {
//    log.logIt("Reseting cache");
//    locker.writeLock();
//    try {
//      try {
//        dataFile.close();
//        File file = new File(rafDir, fileName + ".data");
//        file.delete();
//        keyFile.close();
//        File file2 = new File(rafDir, fileName + ".key");
//        file2.delete();
//      } catch (Exception e) {
//        log.logEx(e);
//      }
//      try {
//        dataFile = new Disk(new File(rafDir, fileName + ".data"));
//        keyFile = new Disk(new File(rafDir, fileName + ".key"));
//        keyHash = new HashMap();
//      } catch (IOException e) {
//        log.logEx(e, " -- IN RESET");
//      } catch (Exception e) {
//        log.logEx(e, " -- IN RESET");
//      }
//    } finally {
//      locker.done();            // release write lock.
//    }
    }
    // end reset

    /**
     * Gets the stats attribute of the HSQLCache object
     *
     * @return The stats value
     */
    public String getStats()
    {
        return "numInstances = " + numInstances;
    }


    // shold be called by cachemanager, since it knows how
    // many are checked out
    /** Description of the Method */
    public void dispose()
    {
//    if (!isAlive) {
//      log.logIt("is not alive and close() was called -- " + fileName);
//      return;
//    }
//    locker.writeLock();
//    try {
//      if (!isAlive) {
//        log.logIt("is not alive and close() was called -- " + fileName);
//        return;
//      }
//      try {
//        optimizeFile();
//      } catch (Exception e) {
//        log.logEx(e, "-- " + fileName);
//      }
//      try {
//        numInstances--;
//        if (numInstances == 0) {
//          p( "dispose -- Closing files -- in close -- " + fileName );
//          log.warn("dispose -- Closing files -- in close -- " + fileName);
//          dataFile.close();
//          dataFile = null;
//          keyFile.close();
//          keyFile = null;
//        }
//      } catch (Exception e) {
//        log.logEx(e, "-- " + fileName);
//      }
//    } finally {
//      isAlive = false;
//      locker.done();            // release write lock;
//    }
    }
    // end dispose

    /**
     * Returns the cache status.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return isAlive ? STATUS_ALIVE : STATUS_DISPOSED;
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
     * Gets the cacheType attribute of the HSQLCache object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }


    /** For debugging. */
    public void dump()
    {
//    log.debug("keyHash.size()=" + keyHash.size());
//    for (Iterator itr = keyHash.entrySet().iterator(); itr.hasNext();) {
//      Map.Entry e = (Map.Entry)itr.next();
//      Serializable key = (Serializable)e.getKey();
//      DiskElementDescriptor ded = (DiskElementDescriptor)e.getValue();
//      Serializable val = get(key);
//      log.debug("disk dump> key=" + key + ", val=" + val + ", pos=" + ded.pos);
//    }
    }

    /**
     * Returns cache name, ha
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }


    /**
     * Returns the serialized form of the given object in a byte array.
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

}
// end class


