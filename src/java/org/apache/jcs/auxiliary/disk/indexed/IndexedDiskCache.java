package org.apache.jcs.auxiliary.disk.indexed;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.auxiliary.disk.indexed.behavior.IIndexedDiskCacheAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Disk cache that uses a RandomAccessFile with keys stored in memory
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class IndexedDiskCache extends AbstractDiskCache
{
    private final static Log log =
        LogFactory.getLog( IndexedDiskCache.class );

    private String fileName;
    private String cacheName;
    private IndexedDisk dataFile;
    private IndexedDisk keyFile;
    private HashMap keyHash;

    private File rafDir;

    IIndexedDiskCacheAttributes cattr;

    /**
     * Constructor for the DiskCache object
     *
     * @param buffer
     * @param cattr
     */
    public IndexedDiskCache( IIndexedDiskCacheAttributes cattr )
    {
        this( cattr.getCacheName(), cattr.getDiskPath() );

        this.cattr = cattr;
    }

    /**
     * Constructor for the DiskCache object
     *
     * @param cacheName
     */
    protected IndexedDiskCache( String cacheName )
    {
        this( cacheName, null );
    }

    /**
     * Constructor for the DiskCache object
     *
     * @param cacheName
     * @param rafroot
     */
    protected IndexedDiskCache( String cacheName, String rootDirName )
    {
        super( cacheName );

        this.fileName = cacheName;
        this.cacheName = cacheName;

        rafDir = new File( rootDirName );
        rafDir.mkdirs();

        log.info( "Cache file root directory: " + rootDirName );

        try
        {
            dataFile = new IndexedDisk(
                new File( rafDir, fileName + ".data" ) );

            keyFile = new IndexedDisk(
                new File( rafDir, fileName + ".key" ) );

            // If the key file has contents, try to initialize the keys
            // from it. In no keys are loaded reset the data file.

            if ( keyFile.length() > 0 )
            {
                loadKeys();

                if ( keyHash.size() == 0 )
                {
                    dataFile.reset();
                }
            }

            // Otherwise start with a new empty map for the keys, and reset
            // the data file if it has contents.

            else
            {
                keyHash = new HashMap();

                if ( dataFile.length() > 0 )
                {
                    dataFile.reset();
                }
            }

            // Initialization finished successfully, so set alive to true.

            alive = true;
        }
        catch ( Exception e )
        {
            log.error( "Failure initializing for fileName: " + fileName
                + " and root directory: " + rootDirName, e );
        }
    }

    /**
     * Description of the Method
     */
    private void loadKeys()
        throws InterruptedException
    {
        lock.writeLock();

        try
        {
            keyHash = ( HashMap ) keyFile.readObject( 0 );

            if ( keyHash == null )
            {
                keyHash = new HashMap();
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Loaded keys from: " + fileName +
                    ", key count: " + keyHash.size() );
            }
        }
        catch ( Exception e )
        {
            log.error( fileName, e );
        }
        finally
        {
            lock.done();
        }
    }

    /**
     * Saves key file to disk
     */
    private void saveKeys()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Saving keys to: " + fileName +
                    ", key count: " + keyHash.size() );
            }

            lock.writeLock();

            try
            {
                keyFile.reset();

                if ( keyHash.size() > 0 )
                {
                    keyFile.writeObject( keyHash, 0 );
                }
            }
            finally
            {
                lock.done();
            }
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    /**
     * Update the disk cache. Called from the Queue. Makes sure the Item has not
     * been retireved from purgatory while in queue for disk. Remove items from
     * purgatory when they go to disk.
     *
     * @param ce
     * @exception IOException
     */
    public void doUpdate( ICacheElement ce )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Storing element on disk, key: " + ce.getKey() );
        }

        IndexedDiskElementDescriptor ded = null;

        try
        {
            ded = new IndexedDiskElementDescriptor();
            byte[] data = IndexedDisk.serialize( ce );
            ded.init( dataFile.length(), data );

            // make sure this only locks for one particular cache region
            lock.writeLock();

            try
            {
                if ( !alive )
                {
                    return;
                }

                IndexedDiskElementDescriptor old =
                    ( IndexedDiskElementDescriptor )
                        keyHash.put( ce.getKey(), ded );

                // Item with the same key already exists in file.
                // Try to reuse the location if possible.
                if ( old != null && ded.len <= old.len )
                {
                    ded.pos = old.pos;
                }

                dataFile.write( data, ded.pos );
            }
            finally
            {
                lock.done();
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "Put to file: " + fileName +
                           ", key: " + ce.getKey() +
                           ", position: " + ded.pos +
                           ", size: " + ded.len );
            }
        }
        catch ( ConcurrentModificationException cme )
        {
            // do nothing, this means it has gone back to memory mid serialization
        }
        catch ( Exception e )
        {
            log.error( "Failure updating element, cacheName: " + cacheName +
                       ", key: " + ce.getKey(), e );
        }
        return;
    }

    /**
     * Description of the Method
     *
     * @return
     * @param key
     * @param container
     * @param lock
     */
    protected Serializable doGet( Serializable key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Trying to get from disk: " + key );
        }

        Serializable object = null;

        try
        {
            lock.readLock();

            if ( !alive )
            {
                log.debug( "No longer alive so returning null, cacheName: " +
                           cacheName + ", key = " + key );

                return null;
            }

            object = readElement( key );

        }
        catch ( Exception e )
        {
            log.error( "Failure getting from disk, cacheName: " + cacheName +
                       ", key = " + key, e );
        }
        finally
        {
            lock.done();
        }

        return object;
    }

    private Serializable readElement( Serializable key )
        throws Exception
    {
        Serializable object = null;

        IndexedDiskElementDescriptor ded =
            ( IndexedDiskElementDescriptor ) keyHash.get( key );

        if ( ded != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Found on disk, key: " + key );
            }

            object = dataFile.readObject( ded.pos );
        }

        return object;
    }

    /**
     * Returns true if the removal was succesful; or false if there is nothing
     * to remove. Current implementation always result in a disk orphan.
     *
     * @return
     * @param key
     */
    public boolean doRemove( Serializable key )
    {
        try
        {
            lock.writeLock();

            if ( key instanceof String
                 && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.
                boolean removed = false;

                Iterator iter = keyHash.entrySet().iterator();

                while ( iter.hasNext() )
                {
                    Map.Entry entry = ( Map.Entry ) iter.next();

                    Object k = entry.getKey();

                    if ( k instanceof String
                         && k.toString().startsWith( key.toString() ) )
                    {
                        iter.remove();
                        removed = true;
                    }
                }
                return removed;
            }
            else
            {
                // remove single item.
                return keyHash.remove( key ) != null;
            }
        }
        catch ( Exception e )
        {
            log.error( e );
            reset();
        }
        finally
        {
            lock.done();
        }

        return false;
    }

    /**
     * Description of the Method
     */
    public void doRemoveAll()
    {
        try
        {
            reset();
        }
        catch ( Exception e )
        {
            log.error( e );
            reset();
        }
        finally
        {
        }
    }
    // end removeAll

    /**
     * handle error by last resort, force content update, or removeall
     */
    private void reset()
    {
        log.debug( "Reseting cache" );

        try
        {
            lock.writeLock();

            dataFile.close();
            File file = new File( rafDir, fileName + ".data" );
            file.delete();

            keyFile.close();
            File file2 = new File( rafDir, fileName + ".key" );
            file2.delete();

            dataFile =
                new IndexedDisk( new File( rafDir, fileName + ".data" ) );

            keyFile =
                new IndexedDisk( new File( rafDir, fileName + ".key" ) );

            keyHash = new HashMap();
        }
        catch ( Exception e )
        {
            log.error( "Failure reseting state", e );
        }
        finally
        {
            lock.done();
        }
    }

    /**
     * Gets the stats attribute of the DiskCache object
     *
     * @return The stats value
     */
    public String getStats()
    {
        return "fileName = " + fileName;
    }

    /**
     * Description of the Method
     */
    public void doDispose()
    {
        try
        {
            lock.writeLock();

            if ( !alive )
            {
                log.debug( "Not alive and dispose was called, filename: " +
                    fileName );
                return;
            }

            try
            {
                optimizeFile();
            }
            catch ( Exception e )
            {
                log.error( fileName, e );
            }
            try
            {
                log.warn( "Closing files, base filename: " + fileName );
                dataFile.close();
                dataFile = null;
                keyFile.close();
                keyFile = null;
            }
            catch ( Exception e )
            {
                log.error( "Failure closing files in dispose, filename: " +
                    fileName, e );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failure in dispose", e );
        }
        finally
        {
            alive = false;
            lock.done();
        }
    }

    /**
     * Note: synchronization currently managed by the only caller method -
     * dispose.
     */
    private void optimizeFile()
    {
        try
        {
            // Migrate from keyHash to keyHshTemp in memory,
            // and from dataFile to dataFileTemp on disk.
            HashMap keyHashTemp = new HashMap();

            IndexedDisk dataFileTemp =
                new IndexedDisk( new File( rafDir, fileName + "Temp.data" ) );

            if ( log.isDebugEnabled() )
            {
                log.info( "optomizing file keyHash.size()=" + keyHash.size() );
            }

            Iterator itr = keyHash.keySet().iterator();

            while ( itr.hasNext() )
            {
                Serializable key = ( Serializable ) itr.next();

                CacheElement tempDe = ( CacheElement ) readElement( key );
                try
                {
                    IndexedDiskElementDescriptor de =
                        dataFileTemp.appendObject( tempDe );

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Put to temp disk cache: " + fileName +
                                   ", key: " + key );
                    }

                    keyHashTemp.put( key, de );
                }
                catch ( Exception e )
                {
                    log.error( "Failed to put to temp disk cache: " + fileName
                               + ", key: " + key, e );
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( fileName
                    + " -- keyHashTemp.size(): " + keyHashTemp.size()
                    + ", keyHash.size(): " + keyHash.size() );
            }

            // Make dataFileTemp to become dataFile on disk.
            dataFileTemp.close();
            dataFile.close();
            File oldData = new File( rafDir, fileName + ".data" );
            if ( oldData.exists() )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( fileName + " -- oldData.length() = " +
                        oldData.length() );
                }
                oldData.delete();
            }
            File newData = new File( rafDir, fileName + "Temp.data" );
            File newFileName = new File( rafDir, fileName + ".data" );
            if ( newData.exists() )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( fileName + " -- newData.length() = " +
                        newData.length() );
                }
                newData.renameTo( newFileName );
            }
            keyHash = keyHashTemp;
            keyFile.reset();
            saveKeys();
        }
        catch ( Exception e )
        {
            log.error( fileName, e );
        }
    }

    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    public int getSize()
    {
        return keyHash.size();
    }

    /**
     * For debugging.
     */
    public void dump()
    {
        log.debug( "[dump] Number of keys: " + keyHash.size() );

        Iterator itr = keyHash.entrySet().iterator();

        while ( itr.hasNext() )
        {
            Map.Entry e = ( Map.Entry ) itr.next();

            Serializable key = ( Serializable ) e.getKey();

            IndexedDiskElementDescriptor ded =
                ( IndexedDiskElementDescriptor ) e.getValue();

            Serializable val = get( key );

            log.debug( "[dump] Disk element, key: " + key +
                       ", val: " + val +
                       ", pos: " + ded.pos );
        }
    }
}

