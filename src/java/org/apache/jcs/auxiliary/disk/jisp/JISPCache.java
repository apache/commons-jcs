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

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * JISP disk cache implementation. Slow as hell with this type of key.
 *
 * <b>VERY EXPERIMENTAL, and only partially implemented</b>
 * Does not work with groups.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id: ILateralCacheTCPListener.java,v 1.2 2002/01/18 22:08:26
 */
public class JISPCache extends AbstractDiskCache
{
    private final static Log log =
        LogFactory.getLog( JISPCache.class );

    private int numInstances = 0;

    /** Description of the Field */
    public boolean isAlive = false;

    JISPCacheAttributes cattr;

    //JISP ACCESS
    IndexedObjectDatabase database;
    BTreeIndex index1;
    private static int s_order = 101;

    String jispDataFileName = "default_this_is_BAD";
    String jispIndexFileName = "default_this_is_BAD";

    /**
     * Constructor for the JISPCache object
     *
     * @param cattr
     */
    public JISPCache( JISPCacheAttributes cattr )
    {
        super( cattr.getCacheName() );

        numInstances++;

        String rafroot = cattr.getDiskPath();

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
    protected void doUpdate( ICacheElement ce )
    {
        if ( !isAlive )
        {
            log.warn( "not alive" );
            return;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Putting " + ce.getKey() + " on disk" );
        }

        try
        {

            KeyObject[] keyArray = new JISPKey[ 1 ];
            keyArray[ 0 ] = new JISPKey( ce.getKey() );

            // akin to an update, should insert as well
            database.write( keyArray, ( Serializable ) ce );

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        return;
    }

    /** Description of the Method */
    protected ICacheElement doGet( Serializable key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "getting " + key + " from disk" );
        }

        if ( !isAlive )
        {
            return null;
        }

        ICacheElement element = null;

        try
        {
            element = ( CacheElement )
                database.read( new JISPKey( key ), index1 );
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( element == null )
        {
            return null;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( " " + key + ", val = " + element.getVal() );
        }

        return element;
    }

    public Set getGroupKeys(String groupName)
    {
        if (true) 
        {
            throw new UnsupportedOperationException("Groups not implemented.");
        }        
        return null;
    }

    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     */
    protected boolean doRemove( Serializable key )
    {
        KeyObject[] keyArray = new JISPKey[ 1 ];
        keyArray[ 0 ] = new JISPKey( key );

        try
        {
            // FIXME: Partial removal not yet implemented for JISP.

            database.remove( keyArray );
        }
        catch ( Exception e )
        {
            log.error( e );
            reset();
        }
        return false;
    }

    /** Description of the Method */
    protected void doRemoveAll()
    {
        try
        {
            reset();
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    // handle error by last resort, force content update, or removeall
    /** Description of the Method */
    private void reset()
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

    /** Description of the Method */
    public void doDispose()
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
}

