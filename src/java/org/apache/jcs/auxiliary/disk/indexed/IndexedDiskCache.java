package org.apache.jcs.auxiliary.disk.indexed;


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



import java.io.File;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.locking.ReadWriteLock;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.control.group.GroupAttrName;

/**
 * Disk cache that uses a RandomAccessFile with keys stored in memory
 *
 * @version $Id$
 */
public class IndexedDiskCache extends AbstractDiskCache
{
    private static final Log log =
        LogFactory.getLog( IndexedDiskCache.class );

    private String fileName;
    private IndexedDisk dataFile;
    private IndexedDisk keyFile;
    private HashMap keyHash;

    private File rafDir;

    IndexedDiskCacheAttributes cattr;

    /**
     * Each instance of a Disk cache should use this lock to synchronize reads
     * and writes to the underlying storage mechansism.
     */
    protected ReadWriteLock storageLock = new ReadWriteLock();

    /**
     * Constructor for the DiskCache object
     *
     * @param cattr
     */
    public IndexedDiskCache( IndexedDiskCacheAttributes cattr )
    {
        super( cattr.getCacheName() );

        String cacheName = cattr.getCacheName();
        String rootDirName = cattr.getDiskPath();

        this.cattr = cattr;

        this.fileName = cacheName;

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
        storageLock.writeLock();

        if ( log.isInfoEnabled() )
        {
          log.info( "loading keys for " + keyFile.toString() );
        }

        try
        {
            keyHash = ( HashMap ) keyFile.readObject( 0 );

            if ( keyHash == null )
            {
                keyHash = new HashMap();
            }

            if ( log.isInfoEnabled() )
            {
               log.info( "Loaded keys from: " + fileName +
                    ", key count: " + keyHash.size() );
            }

            if ( log.isDebugEnabled() )
            {
                Iterator itr = keyHash.entrySet().iterator();
                while ( itr.hasNext() )
                {
                   Map.Entry e = (Map.Entry)itr.next();
                   String key = (String)e.getKey();
                   IndexedDiskElementDescriptor de = (IndexedDiskElementDescriptor)e.getValue();
                   log.debug( "key entry: " + key + ", ded.pos" + de.pos + ", ded.len" + de.len );
                }
            }

        }
        catch ( Exception e )
        {
            log.error( fileName, e );
        }
        finally
        {
            storageLock.done();
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

            storageLock.writeLock();

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
                storageLock.done();
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
            storageLock.writeLock();

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
                storageLock.done();
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
     * @see AbstractDiskCache#doGet
     */
    protected ICacheElement doGet( Serializable key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Trying to get from disk: " + key );
        }

        ICacheElement object = null;

        try
        {
            storageLock.readLock();

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
            storageLock.done();
        }

        return object;
    }

    private CacheElement readElement( Serializable key )
        throws Exception
    {
        CacheElement object = null;

        IndexedDiskElementDescriptor ded =
            ( IndexedDiskElementDescriptor ) keyHash.get( key );

        if ( ded != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Found on disk, key: " + key );
            }

            object = ( CacheElement ) dataFile.readObject( ded.pos );
        }

        return object;
    }

    public Set getGroupKeys(String groupName)
    {
        GroupId groupId = new GroupId(cacheName, groupName);
        HashSet keys = new HashSet();
        try
        {
            storageLock.readLock();

            for (Iterator itr = keyHash.keySet().iterator(); itr.hasNext();)
            {
                //Map.Entry entry = (Map.Entry) itr.next();
                //Object k = entry.getKey();
                Object k = itr.next();
                if ( k instanceof GroupAttrName
                     && ((GroupAttrName)k).groupId.equals(groupId) )
                {
                    keys.add(((GroupAttrName)k).attrName);
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Failure getting from disk, cacheName: " + cacheName +
                       ", group = " + groupName, e );
        }
        finally
        {
            storageLock.done();
        }

        return keys;
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
        boolean removed = false;
        try
        {
            storageLock.writeLock();

            if ( key instanceof String
                 && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.

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
            else if ( key instanceof GroupId )
            {
                // remove all keys of the same name hierarchy.
                Iterator iter = keyHash.entrySet().iterator();
                while ( iter.hasNext() )
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object k = entry.getKey();

                    if ( k instanceof GroupAttrName
                         && ((GroupAttrName)k).groupId.equals(key) )
                    {
                        iter.remove();
                        removed = true;
                    }
                }
            }
            else
            {

                if ( log.isDebugEnabled() )
                {
                    log.debug( "Disk removal: Removed from key hash, key " + key );
                }

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
            storageLock.done();
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
            storageLock.writeLock();

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
            storageLock.done();
        }
    }

    /**
     * Description of the Method
     */
    public void doDispose()
    {
        try
        {
            storageLock.writeLock();

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

            try
            {
              storageLock.done();
            } catch ( Exception e )
            {
                log.error( "Failure releasing lock on shutdown " + e );
            }
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

            if ( log.isInfoEnabled() )
            {
                log.info( "Optomizing file keyHash.size()=" + keyHash.size() );
            }

            Iterator itr = keyHash.keySet().iterator();

            while ( itr.hasNext() )
            {
                Serializable key = ( Serializable ) itr.next();

                CacheElement tempDe = ( CacheElement ) readElement( key );
                try
                {
                    //IndexedDiskElementDescriptor de =
                    //    dataFileTemp.appendObject( tempDe );

                    IndexedDiskElementDescriptor ded = new IndexedDiskElementDescriptor();
                    byte[] data = IndexedDisk.serialize( tempDe );
                    ded.init( dataFileTemp.length(), data );
                    dataFileTemp.write( data, ded.pos );

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Optomize: Put to temp disk cache: " + fileName +
                                   ", key: " + key + ", ded.pos:" + ded.pos + ", ded.len:" + ded.len);
                    }

                    keyHashTemp.put( key, ded );
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

