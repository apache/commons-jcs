package org.apache.jcs.auxiliary.disk.jisp;

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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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
import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.jcs.auxiliary.disk.jisp.behavior.IJISPCacheAttributes;
import org.apache.jcs.auxiliary.disk.PurgatoryElement;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheConstants;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JISP disk cache implementation. Slow as hell with this type of key.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id: ILateralCacheTCPListener.java,v 1.2 2002/01/18 22:08:26
 */
public class JISPCache implements ICache, Serializable
{
    private final static Log log =
        LogFactory.getLog( JISPCache.class );

    private static int numCreated = 0;
    private int numInstances = 0;

    private String cacheName;

    /** Description of the Field */
    public boolean isAlive = false;

    IJISPCacheAttributes cattr;

    // disk cache buffer, need to remove key from buffer on update, if its there
    JISPCacheNoWaitBuffer buffer;

    //JISP ACCESS
    IndexedObjectDatabase database;
    BTreeIndex index1;
    private static int s_order = 101;

    String jispDataFileName = "default_this_is_BAD";
    String jispIndexFileName = "default_this_is_BAD";

    // should use this method
    /**
     * Constructor for the JISPCache object
     *
     * @param buffer
     * @param cattr
     */
    public JISPCache( JISPCacheNoWaitBuffer buffer, IJISPCacheAttributes cattr )
    {
        //this( cattr.getCacheName(), cattr.getDiskPath() );
        this( cattr );
        this.cattr = cattr;
        this.buffer = buffer;
    }

    /**
     * Constructor for the JISPCache object
     *
     * @param cattr
     */
    protected JISPCache( IJISPCacheAttributes cattr )
    {
        numInstances++;

        String rafroot = cattr.getDiskPath();

        this.cacheName = cattr.getCacheName();

        if ( rafroot == null )
        {
            log.warn( "The JISP directory was not defined in the cache.ccf " );
            rafroot = "";
        }

        jispDataFileName = rafroot + cacheName + "DATA.jisp";
        jispIndexFileName = rafroot + cacheName + "INDEX.jisp";

        log.debug( "jispDataFileName = " + jispDataFileName );

        // See if the JISP Cache has already been created.
        // build b-tree database if it doesn't already exist
        try
        {

            File finddb = new File( jispDataFileName );

            if ( !finddb.exists() || cattr.getClearOnStart() )
            {
                setupTABLE();
            }

            try
            {
                createDB( cattr.getClearOnStart() );
            }
            catch ( Exception e )
            {
                log.error( e );
                reset();
            }

            isAlive = true;

        }
        catch ( Exception e )
        {
            log.error( "QueryTool.init", e );

            reset();
        }

    }
    // end constructor

    /** SETUP TABLE FOR CACHE */
    void setupTABLE()
    {

        try
        {

            // delete old files
            File killit = new File( jispDataFileName );

            if ( killit.exists() )
            {
                killit.delete();
            }

            killit = new File( jispIndexFileName );

            if ( killit.exists() )
            {
                killit.delete();
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Exception: " + e );
            log.error( e );
        }

    }
    // end setupTable

    /** Description of the Method */
    public void createDB( boolean clear )
        throws Exception
    {
        try
        {

            // create database
            // it seems to alwasy lose the data on retstart
            database = new IndexedObjectDatabase( jispDataFileName, clear );

            index1 = new BTreeIndex( jispIndexFileName, s_order, new JISPKey(), false );
            database.attachIndex( index1 );
        }
        catch ( Exception e )
        {
            System.out.println( "Exception: " + e );
            log.error( e );
            throw e;
        }
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
            log.warn( "not alive" );
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

        // remove item from purgatory since we are putting it on disk
        // assume that the disk cache will never break
        // disk breakage is akin to an out of memory exception
        buffer.purgatory.remove( ce.getKey() );
        if ( log.isDebugEnabled() )
        {
            log.debug( "\n putting " + ce.getKey() + " on disk, removing from purgatory" );
        }

        // make sure this only locks for one particular cache region
        //locker.writeLock();

        try
        {

            KeyObject[] keyArray = new JISPKey[1];
            keyArray[0] = new JISPKey( ce.getKey() );

            // akin to an update, should insert as well
            database.write( keyArray, ( Serializable ) ce );
            //p( "put " + ce.getKey() );

        }
        catch ( Exception e )
        {
            log.error( e );
        }
        finally
        {
            //locker.done();          // release write lock.
        }

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

        ICacheElement obj = null;

        try
        {

            obj = ( ICacheElement ) database.read( new JISPKey( key ), index1 );

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( obj == null )
        {
            return null;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( " " + key + ", val = " + obj.getVal() );
        }

        if ( container )
        {
            return ( Serializable ) obj;
        }
        return obj.getVal();
    }


    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     */
    public boolean remove( Serializable key )
    {

        KeyObject[] keyArray = new JISPKey[1];
        keyArray[0] = new JISPKey( key );

        try
        {

            if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.
                //sql = "delete from " + cacheName + " where KEY = like '" + key + "%'";
                // need to figure how to do this in JISP
            }
            else
            {
                database.remove( keyArray );
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
            reset();
        }
        catch ( Exception e )
        {
            log.error( e );
            //reset();
        }
        finally
        {
        }
    }
    // end removeAll

    // handle error by last resort, force content update, or removeall
    /** Description of the Method */
    public void reset()
    {

        try
        {
            setupTABLE();
            createDB( true );
        }
        catch ( Exception e )
        {
            //log.error( e );
            log.warn( "Trying to create error files, two programs must be using the same JISP directory" );
            // Move to error directory, someone else must be using the file.
            try
            {
                this.jispDataFileName += ".error";
                this.jispIndexFileName += ".error";
                setupTABLE();
                createDB( true );
            }
            catch ( Exception e2 )
            {
                log.error( "Could not create error files!", e2 );
            }
        }
    }
    // end reset

    /**
     * Gets the stats attribute of the JISPCache object
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

        if ( !isAlive )
        {
            log.error( "is not alive and close() was called -- " + this.jispDataFileName );
            return;
        }

        try
        {
            database.close();
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        finally
        {
            isAlive = false;
        }

        // TODO: can we defragment here?

    }
    // end dispose

    /**
     * Returns the cache status.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return isAlive ? CacheConstants.STATUS_ALIVE : CacheConstants.STATUS_DISPOSED;
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
     * Gets the cacheType attribute of the JISPCache object
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
        // TODO: not sure this is possible with JISP
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
}

