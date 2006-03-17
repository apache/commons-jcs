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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.auxiliary.disk.LRUMapJCS;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import org.apache.jcs.utils.struct.SortedPreferentialArray;

import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * Disk cache that uses a RandomAccessFile with keys stored in memory.
 * 
 * The maximum number of keys stored in memory is configurable.
 * 
 * The disk cache tries to recycle spots on disk to limit file expansion.
 * 
 * @version $Id$
 */
public class IndexedDiskCache
    extends AbstractDiskCache
{
    private static final long serialVersionUID = -265035607729729629L;

    private static final Log log = LogFactory.getLog( IndexedDiskCache.class );

    private String fileName;

    private IndexedDisk dataFile;

    private IndexedDisk keyFile;

    private Map keyHash;

    private int maxKeySize;

    private File rafDir;

    boolean doRecycle = true;

    // are we currenlty optimizing the files
    boolean isOptomizing = false;

    // list where puts made during optimization are made, may need a removeList
    // too
    private LinkedList optimizingPutList = new LinkedList();

    // RECYLCE BIN -- array of empty spots
    private SortedPreferentialArray recycle;

    private IndexedDiskCacheAttributes cattr;

    // used for counting the number of requests
    private int optCnt = 0;

    private int recycleCnt = 0;

    private int startupSize = 0;
    
    /**
     * use this lock to synchronize reads and writes to the underlying storage
     * mechansism.
     */
    protected WriterPreferenceReadWriteLock storageLock = new WriterPreferenceReadWriteLock();

    /**
     * Constructor for the DiskCache object
     * 
     * @param cattr
     */
    public IndexedDiskCache( IndexedDiskCacheAttributes cattr )
    {
        super( cattr );

        String cacheName = cattr.getCacheName();
        String rootDirName = cattr.getDiskPath();
        maxKeySize = cattr.getMaxKeySize();

        this.cattr = cattr;

        this.fileName = cacheName;

        rafDir = new File( rootDirName );
        rafDir.mkdirs();

        log.info( "Cache file root directory: " + rootDirName );

        try
        {
            dataFile = new IndexedDisk( new File( rafDir, fileName + ".data" ) );

            keyFile = new IndexedDisk( new File( rafDir, fileName + ".key" ) );

            // If the key file has contents, try to initialize the keys
            // from it. In no keys are loaded reset the data file.

            if ( keyFile.length() > 0 )
            {
                loadKeys();

                if ( keyHash.size() == 0 )
                {
                    dataFile.reset();
                }
                else
                {
                    boolean isOk = checkKeyDataConsistency();
                    if ( !isOk )
                    {
                        keyHash.clear();
                        keyFile.reset();
                        dataFile.reset();
                        log.warn( "Corruption detected.  Reset data and keys files." );
                    }
                    else
                    {
                        startupSize = keyHash.size();
                    }
                }
            }

            // Otherwise start with a new empty map for the keys, and reset
            // the data file if it has contents.

            else
            {
                initKeyMap();

                if ( dataFile.length() > 0 )
                {
                    dataFile.reset();
                }
            }

            // create the recyclebin
            initRecycleBin();

            // Initialization finished successfully, so set alive to true.
            alive = true;
        }
        catch ( Exception e )
        {
            log.error( "Failure initializing for fileName: " + fileName + " and root directory: " + rootDirName, e );
        }
        
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    /**
     * Loads the keys from the .key file. The keys are stored in a HashMap on
     * disk. This is converted into a LRUMap.
     * 
     * @throws InterruptedException
     */
    protected void loadKeys()
        throws InterruptedException
    {
        storageLock.writeLock().acquire();

        if ( log.isInfoEnabled() )
        {
            log.info( "Loading keys for " + keyFile.toString() );
        }

        try
        {

            // create a key map to use.
            initKeyMap();

            HashMap keys = (HashMap) keyFile.readObject( 0 );

            if ( keys != null )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Found " + keys.size() + " in keys file." );
                }

                keyHash.putAll( keys );

                if ( log.isInfoEnabled() )
                {
                    log.info( "Loaded keys from: " + fileName + ", key count: " + keyHash.size() + "; upto "
                        + maxKeySize + " will be available." );
                }

            }

            if ( log.isDebugEnabled() )
            {
                Iterator itr = keyHash.entrySet().iterator();
                while ( itr.hasNext() )
                {
                    Map.Entry e = (Map.Entry) itr.next();
                    String key = (String) e.getKey();
                    IndexedDiskElementDescriptor de = (IndexedDiskElementDescriptor) e.getValue();
                    log.debug( "key entry: " + key + ", ded.pos" + de.pos + ", ded.len" + de.len );
                }
            }

        }
        catch ( Exception e )
        {
            log.error( "Problem loading keys for file " + fileName, e );
        }
        finally
        {
            storageLock.writeLock().release();
        }
    }

    /**
     * Check for minimal consitency between the keys and the datafile. Makes
     * sure no starting positions in the keys exceed the file length.
     * <p>
     * The caller should take the appropriate action if the keys and data are
     * not consistent.
     * 
     * @return True if the test passes
     */
    private boolean checkKeyDataConsistency()
    {

        log.info( "Performing inital consistency check" );

        boolean isOk = true;
        long len = 0;
        try
        {
            len = dataFile.length();
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        Iterator itr = keyHash.entrySet().iterator();
        while ( itr.hasNext() )
        {
            Map.Entry e = (Map.Entry) itr.next();
            IndexedDiskElementDescriptor de = (IndexedDiskElementDescriptor) e.getValue();
            long pos = de.pos;

            if ( pos > len )
            {
                isOk = false;
            }

            if ( !isOk )
            {
                log.warn( "\n The dataFile is corrupted!" + "\n raf.length() = " + len + "\n pos = " + pos );
                return isOk;
            }
        }

        log.info( "Finished inital consistency check, isOk = " + isOk );

        return isOk;
    }

    /**
     * Saves key file to disk. This converts the LRUMap to a HashMap for
     * deserialzation.
     */
    protected void saveKeys()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Saving keys to: " + fileName + ", key count: " + keyHash.size() );
            }

            try
            {
                keyFile.reset();

                HashMap keys = new HashMap();
                keys.putAll( keyHash );

                if ( keys.size() > 0 )
                {
                    keyFile.writeObject( keys, 0 );
                }
            }
            finally
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Finished saving keys." );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem storing keys.", e );
        }
    }

    /**
     * Update the disk cache. Called from the Queue. Makes sure the Item has not
     * been retireved from purgatory while in queue for disk. Remove items from
     * purgatory when they go to disk.
     * 
     * @param ce
     *            The ICacheElement to put to disk.
     */
    public void doUpdate( ICacheElement ce )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "Storing element on disk, key: " + ce.getKey() );
        }

        if ( !alive )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Disk is not alive, aborting put." );
            }
            return;
        }

        IndexedDiskElementDescriptor ded = null;

        // old element with same key
        IndexedDiskElementDescriptor old = null;

        try
        {
            ded = new IndexedDiskElementDescriptor();
            byte[] data = IndexedDisk.serialize( ce );

            // make sure this only locks for one particular cache region
            storageLock.writeLock().acquire();            
            try
            {
                ded.init( dataFile.length(), data );

                old = (IndexedDiskElementDescriptor) keyHash.put( ce.getKey(), ded );

                // Item with the same key already exists in file.
                // Try to reuse the location if possible.
                if ( old != null && ded.len <= old.len )
                {
                    ded.pos = old.pos;
                }
                else
                {
                    if ( doRecycle )
                    {
                        IndexedDiskElementDescriptor rep = (IndexedDiskElementDescriptor) recycle
                            .takeNearestLargerOrEqual( ded );
                        if ( rep != null )
                        {
                            ded.pos = rep.pos;
                            recycleCnt++;
                            if ( log.isDebugEnabled() )
                            {

                                log.debug( "using recycled ded " + ded.pos + " rep.len = " + rep.len + " ded.len = "
                                    + ded.len );
                            }
                        }
                        else
                        {
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "no ded to recycle" );
                            }

                        }
                    }
                }
                dataFile.write( data, ded.pos );

                if ( this.isOptomizing )
                {
                    optimizingPutList.addLast( ce.getKey() );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "added to optimizing put list." + optimizingPutList.size() );
                    }
                }

            }
            finally
            {   
                storageLock.writeLock().release();
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Put to file: " + fileName + ", key: " + ce.getKey() + ", position: " + ded.pos + ", size: "
                    + ded.len );
            }
        }
        catch ( ConcurrentModificationException cme )
        {
            // do nothing, this means it has gone back to memory mid
            // serialization
            if ( log.isInfoEnabled() )
            {
                // this shouldn't be possible
                log.info( "Caught ConcurrentModificationException." + cme );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failure updating element, cacheName: " + cacheName + ", key: " + ce.getKey() + " old: " + old,
                       e );
        }
    }

    /**
     * @param key
     * @return ICacheElement or null
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
            storageLock.readLock().acquire();
            try
            {
                if ( !alive )
                {
                    log.debug( "No longer alive so returning null, cacheName: " + cacheName + ", key = " + key );

                    return null;
                }

                object = readElement( key );
            }
            finally
            {
                storageLock.readLock().release();
            }
        }
        catch ( IOException ioe )
        {
            log.error( "Failure getting from disk, cacheName: " + cacheName + ", key = " + key, ioe );
            reset();
        }
        catch ( Exception e )
        {
            log.error( "Failure getting from disk, cacheName: " + cacheName + ", key = " + key, e );
        }

        return object;
    }

    /**
     * Reads the item from disk.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    private CacheElement readElement( Serializable key )
        throws IOException
    {
        CacheElement object = null;

        IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( key );

        if ( ded != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Found on disk, key: " + key );
            }
            try
            {
                object = (CacheElement) dataFile.readObject( ded.pos );
            }
            catch ( IOException e )
            {
                log.error( "Problem reading object from file" );
                throw e;
            }

        }

        return object;
    }

    public Set getGroupKeys( String groupName )
    {
        GroupId groupId = new GroupId( cacheName, groupName );
        HashSet keys = new HashSet();
        try
        {
            storageLock.readLock().acquire();

            for ( Iterator itr = keyHash.keySet().iterator(); itr.hasNext(); )
            {
                // Map.Entry entry = (Map.Entry) itr.next();
                // Object k = entry.getKey();
                Object k = itr.next();
                if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( groupId ) )
                {
                    keys.add( ( (GroupAttrName) k ).attrName );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Failure getting from disk, cacheName: " + cacheName + ", group = " + groupName, e );
        }
        finally
        {
            storageLock.readLock().release();
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

        optCnt++;
        if ( !this.isOptomizing && optCnt == this.cattr.getOptimizeAtRemoveCount() )
        {
            doOptimizeRealTime();
            if ( log.isInfoEnabled() )
            {
                log.info( "optCnt = " + optCnt );
            }
        }

        boolean reset = false;
        boolean removed = false;
        try
        {
            storageLock.writeLock().acquire();

            if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                // remove all keys of the same name group.

                Iterator iter = keyHash.entrySet().iterator();

                while ( iter.hasNext() )
                {
                    Map.Entry entry = (Map.Entry) iter.next();

                    Object k = entry.getKey();

                    if ( k instanceof String && k.toString().startsWith( key.toString() ) )
                    {

                        if ( doRecycle )
                        {
                            // reuse the spot
                            IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( key );
                            if ( ded != null )
                            {
                                recycle.add( ded );
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "recycling ded " + ded );
                                }
                            }
                        }

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

                    if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( key ) )
                    {
                        if ( doRecycle )
                        {
                            // reuse the spot
                            IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( key );
                            if ( ded != null )
                            {
                                recycle.add( ded );
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "recycling ded " + ded );
                                }
                            }
                        }

                        iter.remove();
                        removed = true;
                    }
                }
            }
            else
            {
                if ( doRecycle )
                {
                    // reuse the spot
                    IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( key );
                    if ( ded != null )
                    {
                        recycle.add( ded );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Adding to recycle bin: " + ded );
                        }
                    }
                }

                // remove single item.
                removed = keyHash.remove( key ) != null;

                if ( log.isDebugEnabled() )
                {
                    log.debug( "Disk removal: Removed from key hash, key " + key + " removed = " + removed );
                }

                return removed;
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem removing element.", e );
            reset = true;
        }
        finally
        {
            storageLock.writeLock().release();
        }

        if ( reset )
        {
            reset();
        }

        return false;
    }

    /**
     * Remove all the items from the disk cache by reseting everything.
     */
    public void doRemoveAll()
    {
        try
        {
            reset();
        }
        catch ( Exception e )
        {
            log.error( "Problem removing all.", e );
            reset();
        }
    }

    /**
     * Reset effectively clears the disk cache, creating new files, recyclebins,
     * and keymaps.
     * <p>
     * It can be used to handle errors by last resort, force content update, or
     * removeall.
     */
    private void reset()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Reseting cache" );
        }

        try
        {
            storageLock.writeLock().acquire();

            if ( dataFile != null )
            {
                dataFile.close();
            }
            File dataFileTemp = new File( rafDir, fileName + ".data" );
            dataFileTemp.delete();

            if ( keyFile != null )
            {
                keyFile.close();
            }
            File keyFileTemp = new File( rafDir, fileName + ".key" );
            keyFileTemp.delete();

            dataFile = new IndexedDisk( new File( rafDir, fileName + ".data" ) );

            keyFile = new IndexedDisk( new File( rafDir, fileName + ".key" ) );

            initRecycleBin();

            initKeyMap();
        }
        catch ( Exception e )
        {
            log.error( "Failure reseting state", e );
        }
        finally
        {
            storageLock.writeLock().release();
        }
    }

    /**
     * If the maxKeySize is < 0, use 5000, no way to have an unlimted recycle
     * bin right now, or one less than the mazKeySize.
     */
    private void initRecycleBin()
    {
        recycle = null;
        if ( cattr.getMaxRecycleBinSize() >= 0 )
        {
            recycle = new SortedPreferentialArray( cattr.getMaxRecycleBinSize() );
            if ( log.isInfoEnabled() )
            {
                log.info( "Set recycle max Size to MaxRecycleBinSize: '" + cattr.getMaxRecycleBinSize() + "'" );
            }
        }
        else
        {
            // this is a fail safetly. Will no
            recycle = new SortedPreferentialArray( 0 );
            if ( log.isInfoEnabled() )
            {
                log.warn( "Set recycle maxSize to 0, will not try to recycle, MaxRecycleBinSize was less than 0" );
            }
        }
    }

    /**
     * Create the map for keys that contain the index position on disk.
     * 
     */
    private void initKeyMap()
    {
        keyHash = null;
        if ( maxKeySize >= 0 )
        {
            keyHash = new LRUMapJCS( maxKeySize );
            if ( log.isInfoEnabled() )
            {
                log.info( "Set maxKeySize to: '" + maxKeySize + "'" );
            }
        }
        else
        {
            keyHash = new HashMap();
            // keyHash = Collections.synchronizedMap( new HashMap() );
            if ( log.isInfoEnabled() )
            {
                log.info( "Set maxKeySize to unlimited'" );
            }
        }
    }

    /**
     * Dispose of the disk cache in a background thread. Joins against this
     * thread to put a cap on the disposal time.
     * 
     * @todo make dispose window configurable.
     */
    public void doDispose()
    {
        Runnable disR = new Runnable()
        {
            public void run()
            {
                disposeInternal();
            }
        };
        Thread t = new Thread( disR );
        t.start();
        // wait up to 60 seconds for dispose and then quit if not done.
        try
        {
            t.join( 60 * 1000 );
        }
        catch ( InterruptedException ex )
        {
            log.error( "Interrupted while waiting for disposal thread to finish.", ex );
        }
    }

    /**
     * Internal method that handles the disposal.
     */
    private void disposeInternal()
    {
        try
        {
            storageLock.writeLock().acquire();

            if ( !alive )
            {
                log.debug( "Not alive and dispose was called, filename: " + fileName );
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
                log.error( "Failure closing files in dispose, filename: " + fileName, e );
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
                storageLock.writeLock().release();
            }
            catch ( Exception e )
            {
                log.error( "Failure releasing lock on shutdown " + e );
            }
        }

    }

    // ///////////////////////////////////////////////////////////////////////////////
    // OPTIMIZATION METHODS

    /**
     * Dispose of the disk cache in a background thread. Joins against this
     * thread to put a cap on the disposal time.
     */
    public synchronized void doOptimizeRealTime()
    {
        if ( !this.isOptomizing )
        {
            this.isOptomizing = true;
            Runnable optR = new Runnable()
            {
                public void run()
                {
                    optimizeRealTime();
                }
            };
            Thread t = new Thread( optR );
            t.start();
        }
        /*
         * // wait up to 60 seconds for dispose and then quit if not done. try {
         * t.join(60 * 1000); } catch (InterruptedException ex) { log.error(ex); }
         */
    }

    private int timesOptimized = 0;

    /**
     * Realtime optimization is handled by this method.
     * 
     * It works in this way:
     * <ul>
     * <li>1. Lock the active file, create a new file.</li>
     * <li>2. Copy the keys for iteration.</li>
     * <li>3. For each key in the copy, make sure it is still in the active
     * keyhash to prevent putting items on disk that have been removed. It also
     * checks the new keyHash to make sure that a newer version hasn't already
     * been put.</li>
     * <li>4. Write the element for the key copy to disk in the normal
     * proceedure.</li>
     * <li>5. All gets will be serviced by the new file. </li>
     * <li>6. All puts are made on the new file.</li>
     * </ul>
     * 
     */
    protected void optimizeRealTime()
    {

        long start = System.currentTimeMillis();
        if ( log.isInfoEnabled() )
        {
            log.info( "Beginning Real Time Optimization #" + ++timesOptimized );
        }

        Object[] keys = null;

        try
        {
            storageLock.readLock().acquire();
            try
            {
                keys = keyHash.keySet().toArray();
            }
            finally
            {
                storageLock.readLock().release();
            }

            LRUMap keyHashTemp = new LRUMap( this.maxKeySize );
            keyHashTemp.tag = "Round=" + timesOptimized;
            IndexedDisk dataFileTemp = new IndexedDisk( new File( rafDir, fileName + "Temp.data" ) );
            // dataFileTemp.reset();

            // make sure flag is set to true
            isOptomizing = true;

            int len = keys.length;
            // while ( itr.hasNext() )
            if ( log.isInfoEnabled() )
            {
                log.info( "Optimizing RT -- TempKeys, length = " + len );
            }
            for ( int i = 0; i < len; i++ )
            {
                // lock so no more gets to the queue -- optimizingPutList
                // storageLock.writeLock();
                storageLock.writeLock().acquire();
                try
                {
                    // Serializable key = ( Serializable ) itr.next();
                    Serializable key = (Serializable) keys[i];
                    this.moveKeyDataToTemp( key, keyHashTemp, dataFileTemp );
                }
                finally
                {
                    // storageLock.done();
                    storageLock.writeLock().release();
                }
            }

            // potentially, this will cause the longest delay
            // lock so no more gets to the queue -- optimizingPutList
            storageLock.writeLock().acquire();
            try
            {
                // switch primary and do the same for those on the list
                if ( log.isInfoEnabled() )
                {
                    log.info( "Optimizing RT -- PutList, size = " + optimizingPutList.size() );
                }

                while ( optimizingPutList.size() > 0 )
                {
                    Serializable key = (Serializable) optimizingPutList.removeFirst();
                    this.moveKeyDataToTemp( key, keyHashTemp, dataFileTemp );
                }
                if ( log.isInfoEnabled() )
                {
                    log.info( "keyHashTemp, size = " + keyHashTemp.size() );
                }

                // switch files.
                // main
                if ( log.isInfoEnabled() )
                {
                    log.info( "Optimizing RT -- Replacing Files" );
                }
                tempToPrimary( keyHashTemp, dataFileTemp );
            }
            finally
            {
                storageLock.writeLock().release();
            }

        }
        catch ( Exception e )
        {
            log.error( "Failure Optimizing RealTime, cacheName: " + cacheName, e );
        }
        optCnt = 0;
        isOptomizing = false;

        long end = System.currentTimeMillis();
        long time = end - start;
        if ( log.isInfoEnabled() )
        {
            log.info( "Finished #" + timesOptimized + " Real Time Optimization in " + time + " millis." );
        }

    }

    /**
     * Note: synchronization currently must be managed by the caller method--
     * dispose.
     */
    protected void optimizeFile()
    {
        try
        {
            // Migrate from keyHash to keyHshTemp in memory,
            // and from dataFile to dataFileTemp on disk.
            LRUMap keyHashTemp = new LRUMap( this.maxKeySize );

            IndexedDisk dataFileTemp = new IndexedDisk( new File( rafDir, fileName + "Temp.data" ) );
            // dataFileTemp.reset();

            if ( log.isInfoEnabled() )
            {
                log.info( "Optomizing file keyHash.size()=" + keyHash.size() );
            }

            // Iterator itr = keyHash.keySet().iterator();

            Object[] keys = keyHash.keySet().toArray();
            int len = keys.length;

            try
            {

                // while ( itr.hasNext() )
                for ( int i = 0; i < len; i++ )
                {
                    // Serializable key = ( Serializable ) itr.next();
                    Serializable key = (Serializable) keys[i];
                    this.moveKeyDataToTemp( key, keyHashTemp, dataFileTemp );
                }

                // main
                tempToPrimary( keyHashTemp, dataFileTemp );

            }
            catch ( IOException e )
            {
                log.error( "Problem in optimization, abandoning attempt" );
            }

        }
        catch ( Exception e )
        {
            log.error( fileName, e );
        }
    }

    /**
     * Copies data for a key from main file to temp file and key to temp keyhash
     * Clients must manage locking.
     * 
     * @param key
     *            Serializable
     * @param keyHashTemp
     * @param dataFileTemp
     *            IndexedDisk
     * @throws Exception
     */
    private void moveKeyDataToTemp( Serializable key, LRUMap keyHashTemp, IndexedDisk dataFileTemp )
        throws Exception
    {

        CacheElement tempDe = null;
        try
        {
            tempDe = readElement( key );
        }
        catch ( IOException e )
        {
            log.error( "Failed to get orinigal off disk cache: " + fileName + ", key: " + key + "" );
            // reset();
            throw e;
        }

        try
        {
            // IndexedDiskElementDescriptor de =
            // dataFileTemp.appendObject( tempDe );

            IndexedDiskElementDescriptor ded = new IndexedDiskElementDescriptor();
            byte[] data = IndexedDisk.serialize( tempDe );
            ded.init( dataFileTemp.length(), data );
            dataFileTemp.write( data, ded.pos );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Optomize: Put to temp disk cache: " + fileName + ", key: " + key + ", ded.pos:" + ded.pos
                    + ", ded.len:" + ded.len );
            }

            keyHashTemp.put( key, ded );
        }
        catch ( Exception e )
        {
            log.error( "Failed to put to temp disk cache: " + fileName + ", key: " + key, e );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( fileName + " -- keyHashTemp.size(): " + keyHashTemp.size() + ", keyHash.size(): "
                + keyHash.size() );
        }

    }

    /**
     * Replaces current keyHash, data file, and recylce bin. Temp file passed in
     * must follow Temp.data naming convention.
     * 
     * @param keyHashTemp
     *            LRUMap
     * @param dataFileTemp
     *            IndexedDisk
     */
    private void tempToPrimary( LRUMap keyHashTemp, IndexedDisk dataFileTemp )
    {

        try
        {
            // Make dataFileTemp to become dataFile on disk.
            dataFileTemp.close();
            dataFile.close();
            File oldData = new File( rafDir, fileName + ".data" );
            if ( oldData.exists() )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( fileName + " -- oldData.length() = " + oldData.length() );
                }
                oldData.delete();
            }
            File newData = new File( rafDir, fileName + "Temp.data" );
            File newFileName = new File( rafDir, fileName + ".data" );
            if ( newData.exists() )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( fileName + " -- newData.length() = " + newData.length() );
                }

                boolean success = newData.renameTo( newFileName );
                if ( log.isInfoEnabled() )
                {
                    log.info( " rename success = " + success );
                }
            }
            dataFile = new IndexedDisk( newFileName );

            if ( log.isInfoEnabled() )
            {
                log.info( "1 dataFile.length() " + dataFile.length() );
            }

            keyHash = keyHashTemp;
            keyFile.reset();
            saveKeys();

            // clean up the recycle store
            recycle = null;
            recycle = new SortedPreferentialArray( maxKeySize );
        }
        catch ( Exception e )
        {
            log.error( "Failed to put to temp disk cache", e );
        }

    }

    // /////////////////////////////////////////////////////////////////////////////
    // DEBUG
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
     * This is for debugging and testing.
     * 
     * @return the length of the data file.
     * @throws IOException
     */
    protected long getDataFileSize()
        throws IOException
    {
        long size = 0;

        try
        {
            storageLock.readLock().acquire();

            try
            {
                if ( dataFile != null )
                {
                    size = dataFile.length();
                }
            }
            finally
            {
                storageLock.readLock().release();
            }
        }
        catch ( InterruptedException e )
        {
            // nothing
        }

        return size;
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
            Map.Entry e = (Map.Entry) itr.next();

            Serializable key = (Serializable) e.getKey();

            IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) e.getValue();

            Serializable val = get( key );

            log.debug( "[dump] Disk element, key: " + key + ", val: " + val + ", pos: " + ded.pos );
        }
    }

    /**
     * Gets basic stats for the disk cache.
     * 
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Indexed Disk Cache" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Is Alive" );
        se.setData( "" + alive );
        elems.add( se );

        se = new StatElement();
        se.setName( "Key Map Size" );
        if ( this.keyHash != null )
        {
            se.setData( "" + this.keyHash.size() );
        }
        else
        {
            se.setData( "-1" );
        }
        elems.add( se );

        try
        {
            se = new StatElement();
            se.setName( "Data File Length" );
            if ( this.dataFile != null )
            {
                se.setData( "" + this.dataFile.length() );
            }
            else
            {
                se.setData( "-1" );
            }
            elems.add( se );
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        se = new StatElement();
        se.setName( "Optimize Operation Count" );
        se.setData( "" + this.optCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Times Optimized" );
        se.setData( "" + this.timesOptimized );
        elems.add( se );

        se = new StatElement();
        se.setName( "Recycle Count" );
        se.setData( "" + this.recycleCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Startup Size" );
        se.setData( "" + this.startupSize );
        elems.add( se );

        // get the stats from the super too
        // get as array, convert to list, add list to our outer list
        IStats sStats = super.getStatistics();
        IStatElement[] sSEs = sStats.getStatElements();
        List sL = Arrays.asList( sSEs );
        elems.addAll( sL );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

    // /////////////////////////////////////////////////////////////////////////////
    // RECYLCE INNER CLASS
    /**
     * class for recylcing and lru
     */
    public class LRUMap
        extends LRUMapJCS
    {

        private static final long serialVersionUID = 4955079991472142198L;

        /**
         * <code>tag</code> tells us which map we are working on.
         */
        public String tag = "orig";

        /**
         * 
         */
        public LRUMap()
        {
            super();
        }

        /**
         * @param maxKeySize
         */
        public LRUMap( int maxKeySize )
        {
            super( maxKeySize );
        }

        protected void processRemovedLRU( Object key, Object value )
        {

            if ( doRecycle )
            {
                // reuse the spot
                IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) value;
                if ( ded != null )
                {
                    recycle.add( ded );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "recycled ded in LRU" + ded );
                    }
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Removing key: '" + key + "' from key store." );
                log.debug( "Key store size: '" + this.size() + "'." );
            }

        }
    }
    
    /**
     * Called on shutdown
     *
     * @author Aaron Smuts
     *
     */
    class ShutdownHook extends Thread
    {
        
        public void run()
        {
            if ( alive )
            {
                log.info( "Disk cache was not shutdown properly.  Will try to dispose." );
                
                doDispose();
            }            
        }
        
    }
}
