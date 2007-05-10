package org.apache.jcs.auxiliary.disk.indexed;

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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.auxiliary.disk.LRUMapJCS;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import org.apache.jcs.utils.struct.SortedPreferentialArray;
import org.apache.jcs.utils.timing.ElapsedTimer;

import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * Disk cache that uses a RandomAccessFile with keys stored in memory. The maximum number of keys
 * stored in memory is configurable. The disk cache tries to recycle spots on disk to limit file
 * expansion.
 */
public class IndexedDiskCache
    extends AbstractDiskCache
{
    /** Don't change */
    private static final long serialVersionUID = -265035607729729629L;

    /** The logger */
    private static final Log log = LogFactory.getLog( IndexedDiskCache.class );

    private final String logCacheName;

    private String fileName;

    private IndexedDisk dataFile;

    private IndexedDisk keyFile;

    private Map keyHash;

    private int maxKeySize;

    private File rafDir;

    boolean doRecycle = true;

    boolean isRealTimeOptimizationEnabled = true;

    boolean isShutdownOptimizationEnabled = true;

    /** are we currenlty optimizing the files */
    boolean isOptimizing = false;

    private int timesOptimized = 0;

    private volatile Thread currentOptimizationThread;

    /** used for counting the number of requests */
    private int removeCount = 0;

    private boolean queueInput = false;

    /** list where puts made during optimization are made */
    private LinkedList queuedPutList = new LinkedList();

    /** RECYLCE BIN -- array of empty spots */
    private SortedPreferentialArray recycle;

    private IndexedDiskCacheAttributes cattr;

    private int recycleCnt = 0;

    private int startupSize = 0;

    /** the number of bytes free on disk. */
    private long bytesFree = 0;

    private int hitCount = 0;

    /**
     * Use this lock to synchronize reads and writes to the underlying storage mechansism.
     */
    protected ReentrantWriterPreferenceReadWriteLock storageLock = new ReentrantWriterPreferenceReadWriteLock();

    /**
     * Constructor for the DiskCache object.
     * <p>
     * @param cattr
     */
    public IndexedDiskCache( IndexedDiskCacheAttributes cattr )
    {
        super( cattr );

        String rootDirName = cattr.getDiskPath();
        this.maxKeySize = cattr.getMaxKeySize();

        this.isRealTimeOptimizationEnabled = cattr.getOptimizeAtRemoveCount() > 0;
        this.isShutdownOptimizationEnabled = cattr.isOptimizeOnShutdown();

        this.cattr = cattr;

        this.logCacheName = "Region [" + getCacheName() + "] ";
        this.fileName = getCacheName();

        this.rafDir = new File( rootDirName );
        this.rafDir.mkdirs();

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Cache file root directory: " + rootDirName );
        }

        try
        {
            this.dataFile = new IndexedDisk( new File( rafDir, fileName + ".data" ) );

            this.keyFile = new IndexedDisk( new File( rafDir, fileName + ".key" ) );

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
                    boolean isOk = checkKeyDataConsistency( false );
                    if ( !isOk )
                    {
                        keyHash.clear();
                        keyFile.reset();
                        dataFile.reset();
                        log.warn( logCacheName + "Corruption detected.  Reset data and keys files." );
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
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Indexed Disk Cache is alive." );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Failure initializing for fileName: " + fileName + " and root directory: "
                + rootDirName, e );
        }

        // TODO: Should we improve detection of whether or not the file should be optimized.
        if ( isRealTimeOptimizationEnabled && keyHash.size() > 0 )
        {
            // Kick off a real time optimization, in case we didn't do a final optimization.
            doOptimizeRealTime();
        }
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    /**
     * Loads the keys from the .key file. The keys are stored in a HashMap on disk. This is
     * converted into a LRUMap.
     * <p>
     * @throws InterruptedException
     */
    protected void loadKeys()
        throws InterruptedException
    {
        storageLock.writeLock().acquire();

        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Loading keys for " + keyFile.toString() );
        }

        try
        {
            // create a key map to use.
            initKeyMap();

            HashMap keys = (HashMap) keyFile.readObject( new IndexedDiskElementDescriptor( 0, (int) keyFile.length()
                - IndexedDisk.RECORD_HEADER ) );

            if ( keys != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( logCacheName + "Found " + keys.size() + " in keys file." );
                }

                keyHash.putAll( keys );

                if ( log.isInfoEnabled() )
                {
                    log.info( logCacheName + "Loaded keys from [" + fileName + "], key count: " + keyHash.size()
                        + "; up to " + maxKeySize + " will be available." );
                }
            }

            if ( log.isDebugEnabled() )
            {
                dump( false );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Problem loading keys for file " + fileName, e );
        }
        finally
        {
            storageLock.writeLock().release();
        }
    }

    /**
     * Check for minimal consitency between the keys and the datafile. Makes sure no starting
     * positions in the keys exceed the file length.
     * <p>
     * The caller should take the appropriate action if the keys and data are not consistent.
     * @param checkForDedOverlaps if <code>true</code>, do a more thorough check by checking for
     *            data overlap
     * @return <code>true</code> if the test passes
     */
    private boolean checkKeyDataConsistency( boolean checkForDedOverlaps )
    {
        ElapsedTimer timer = new ElapsedTimer();
        log.debug( logCacheName + "Performing inital consistency check" );

        boolean isOk = true;
        long fileLength = 0;
        try
        {
            fileLength = dataFile.length();

            Iterator itr = keyHash.entrySet().iterator();
            while ( itr.hasNext() )
            {
                Map.Entry e = (Map.Entry) itr.next();
                IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) e.getValue();

                isOk = ( ded.pos + IndexedDisk.RECORD_HEADER + ded.len <= fileLength );

                if ( !isOk )
                {
                    log.warn( logCacheName + "The dataFile is corrupted!" + "\n raf.length() = " + fileLength
                        + "\n ded.pos = " + ded.pos );
                    break;
                }
            }

            if ( isOk && checkForDedOverlaps )
            {
                isOk = checkForDedOverlaps( createPositionSortedDescriptorList() );
            }
        }
        catch ( Exception e )
        {
            log.error( e );
            isOk = false;
        }

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Finished inital consistency check, isOk = " + isOk + " in "
                + timer.getElapsedTimeString() );
        }

        return isOk;
    }

    /**
     * Detects any overlapping elements. This expects a sorted list.
     * <p>
     * The total length of an item is IndexedDisk.RECORD_HEADER + ded.len.
     * <p>
     * @param sortedDescriptors
     * @return false if there are overlaps.
     */
    protected boolean checkForDedOverlaps( IndexedDiskElementDescriptor[] sortedDescriptors )
    {
        long start = System.currentTimeMillis();
        boolean isOk = true;
        long expectedNextPos = 0;
        for ( int i = 0; i < sortedDescriptors.length; i++ )
        {
            IndexedDiskElementDescriptor ded = sortedDescriptors[i];
            if ( expectedNextPos > ded.pos )
            {
                log.error( logCacheName + "Corrupt file: overlapping deds " + ded );
                isOk = false;
                break;
            }
            else
            {
                expectedNextPos = ded.pos + IndexedDisk.RECORD_HEADER + ded.len;
            }
        }
        long end = System.currentTimeMillis();
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Check for DED overlaps took " + ( end - start ) + " ms." );
        }

        return isOk;
    }

    /**
     * Saves key file to disk. This converts the LRUMap to a HashMap for deserialzation.
     */
    protected void saveKeys()
    {
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Saving keys to: " + fileName + ", key count: " + keyHash.size() );
            }

            keyFile.reset();

            HashMap keys = new HashMap();
            keys.putAll( keyHash );

            if ( keys.size() > 0 )
            {
                keyFile.writeObject( keys, 0 );
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Finished saving keys." );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Problem storing keys.", e );
        }
    }

    /**
     * Update the disk cache. Called from the Queue. Makes sure the Item has not been retireved from
     * purgatory while in queue for disk. Remove items from purgatory when they go to disk.
     * <p>
     * @param ce The ICacheElement to put to disk.
     */
    public void doUpdate( ICacheElement ce )
    {
        if ( !alive )
        {
            log.error( logCacheName + "No longer alive; aborting put of key = " + ce.getKey() );
            return;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Storing element on disk, key: " + ce.getKey() );
        }

        IndexedDiskElementDescriptor ded = null;

        // old element with same key
        IndexedDiskElementDescriptor old = null;

        try
        {
            byte[] data = IndexedDisk.serialize( ce );

            // make sure this only locks for one particular cache region
            storageLock.writeLock().acquire();
            try
            {
                old = (IndexedDiskElementDescriptor) keyHash.get( ce.getKey() );

                // Item with the same key already exists in file.
                // Try to reuse the location if possible.
                if ( old != null && data.length <= old.len )
                {
                    // Reuse the old ded. The defrag relies on ded updates by reference, not
                    // replacement.
                    ded = old;
                    ded.len = data.length;
                }
                else
                {
                    // we need this to compare in the recycle bin
                    ded = new IndexedDiskElementDescriptor( dataFile.length(), data.length );

                    if ( doRecycle )
                    {
                        IndexedDiskElementDescriptor rep = (IndexedDiskElementDescriptor) recycle
                            .takeNearestLargerOrEqual( ded );
                        if ( rep != null )
                        {
                            ded = rep;
                            ded.len = data.length;
                            recycleCnt++;
                            this.adjustBytesFree( ded, false );
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( logCacheName + "using recycled ded " + ded.pos + " rep.len = " + rep.len
                                    + " ded.len = " + ded.len );
                            }
                        }
                    }

                    // Put it in the map
                    keyHash.put( ce.getKey(), ded );

                    if ( queueInput )
                    {
                        queuedPutList.add( ded );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( logCacheName + "added to queued put list." + queuedPutList.size() );
                        }
                    }
                }

                dataFile.write( ded, data );
            }
            finally
            {
                storageLock.writeLock().release();
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Put to file: " + fileName + ", key: " + ce.getKey() + ", position: "
                    + ded.pos + ", size: " + ded.len );
            }
        }
        catch ( ConcurrentModificationException cme )
        {
            // do nothing, this means it has gone back to memory mid
            // serialization
            if ( log.isDebugEnabled() )
            {
                // this shouldn't be possible
                log.debug( logCacheName + "Caught ConcurrentModificationException." + cme );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Failure updating element, key: " + ce.getKey() + " old: " + old, e );
        }
    }

    /**
     * @param key
     * @return ICacheElement or null
     * @see AbstractDiskCache#doGet
     */
    protected ICacheElement doGet( Serializable key )
    {
        if ( !alive )
        {
            log.error( logCacheName + "No longer alive so returning null for key = " + key );

            return null;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Trying to get from disk: " + key );
        }

        ICacheElement object = null;
        try
        {
            storageLock.readLock().acquire();
            try
            {
                object = readElement( key );
            }
            finally
            {
                storageLock.readLock().release();
            }

            if ( object != null )
            {
                incrementHitCount();
            }
        }
        catch ( IOException ioe )
        {
            log.error( logCacheName + "Failure getting from disk, key = " + key, ioe );
            reset();
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Failure getting from disk, key = " + key, e );
        }

        return object;
    }

    /**
     * Reads the item from disk.
     * <p>
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    private ICacheElement readElement( Serializable key )
        throws IOException
    {
        ICacheElement object = null;

        IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( key );

        if ( ded != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Found on disk, key: " + key );
            }
            try
            {
                object = (ICacheElement) dataFile.readObject( ded );
            }
            catch ( IOException e )
            {
                log.error( logCacheName + "IO Exception, Problem reading object from file", e );
                throw e;
            }
            catch ( Exception e )
            {
                log.error( logCacheName + "Exception, Problem reading object from file", e );
                throw new IOException( logCacheName + "Problem reading object from disk. " + e.getMessage() );
            }
        }

        return object;
    }

    /**
     * Gets the group keys from the disk.
     * <p>
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getGroupKeys(java.lang.String)
     */
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
            log.error( logCacheName + "Failure getting from disk, group = " + groupName, e );
        }
        finally
        {
            storageLock.readLock().release();
        }

        return keys;
    }

    /**
     * Returns true if the removal was succesful; or false if there is nothing to remove. Current
     * implementation always result in a disk orphan.
     * <p>
     * @return true if at least one item was removed.
     * @param key
     */
    public boolean doRemove( Serializable key )
    {
        if ( !alive )
        {
            log.error( logCacheName + "No longer alive so returning false for key = " + key );
            return false;
        }

        if ( key == null )
        {
            return false;
        }

        boolean reset = false;
        boolean removed = false;
        try
        {
            storageLock.writeLock().acquire();

            if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
            {
                removed = performPartialKeyRemoval( (String) key );
            }
            else if ( key instanceof GroupId )
            {
                removed = performGroupRemoval( (GroupId) key );
            }
            else
            {
                removed = performSingleKeyRemoval( key );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Problem removing element.", e );
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

        // this increments the removecount.
        // there is no reason to call this if an item was not removed.
        if ( removed )
        {
            doOptimizeRealTime();
        }

        return removed;
    }

    /**
     * Iterates over the keyset. Builds a list of matches. Removes all the keys in the list . Does
     * not remove via the iterator, since the map impl may not support it.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     * @param key
     * @return true if there was a match
     */
    private boolean performPartialKeyRemoval( String key )
    {
        boolean removed = false;

        // remove all keys of the same name hierarchy.
        List itemsToRemove = new LinkedList();

        Iterator iter = keyHash.entrySet().iterator();
        while ( iter.hasNext() )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object k = entry.getKey();
            if ( k instanceof String && k.toString().startsWith( key.toString() ) )
            {
                itemsToRemove.add( k );
            }
        }

        // remove matches.
        Iterator itToRemove = itemsToRemove.iterator();
        while ( itToRemove.hasNext() )
        {
            String fullKey = (String) itToRemove.next();
            IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( fullKey );
            addToRecycleBin( ded );
            performSingleKeyRemoval( fullKey );
            removed = true;
            // TODO this needs to update the remove count separately
        }

        return removed;
    }

    /**
     * Remove all elements from the group. This does not use the iterator to remove. It builds a
     * list of group elemetns and then removes them one by one.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     * @param key
     * @return true if an element was removed
     */
    private boolean performGroupRemoval( GroupId key )
    {
        boolean removed = false;

        // remove all keys of the same name group.
        List itemsToRemove = new LinkedList();

        // remove all keys of the same name hierarchy.
        Iterator iter = keyHash.entrySet().iterator();
        while ( iter.hasNext() )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object k = entry.getKey();

            if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( key ) )
            {
                itemsToRemove.add( k );
            }
        }

        // remove matches.
        Iterator itToRemove = itemsToRemove.iterator();
        while ( itToRemove.hasNext() )
        {
            GroupAttrName keyToRemove = (GroupAttrName) itToRemove.next();
            IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.get( keyToRemove );
            addToRecycleBin( ded );
            performSingleKeyRemoval( keyToRemove );
            removed = true;
        }
        return removed;
    }

    /**
     * Removes an individual key from the cache.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     * @param key
     * @return true if an item was removed.
     */
    private boolean performSingleKeyRemoval( Serializable key )
    {
        boolean removed;
        // remove single item.
        IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) keyHash.remove( key );
        removed = ( ded != null );
        addToRecycleBin( ded );

        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Disk removal: Removed from key hash, key [" + key + "] removed = " + removed );
        }
        return removed;
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
            log.error( logCacheName + "Problem removing all.", e );
            reset();
        }
    }

    /**
     * Reset effectively clears the disk cache, creating new files, recyclebins, and keymaps.
     * <p>
     * It can be used to handle errors by last resort, force content update, or removeall.
     */
    private void reset()
    {
        if ( log.isWarnEnabled() )
        {
            log.warn( logCacheName + "Reseting cache" );
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
            log.error( logCacheName + "Failure reseting state", e );
        }
        finally
        {
            storageLock.writeLock().release();
        }
    }

    /**
     * If the maxKeySize is < 0, use 5000, no way to have an unlimted recycle bin right now, or one
     * less than the mazKeySize.
     */
    private void initRecycleBin()
    {
        int recycleBinSize = cattr.getMaxRecycleBinSize() >= 0 ? cattr.getMaxRecycleBinSize() : 0;
        recycle = new SortedPreferentialArray( recycleBinSize );
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Set recycle max Size to MaxRecycleBinSize: '" + recycleBinSize + "'" );
        }
    }

    /**
     * Create the map for keys that contain the index position on disk.
     */
    private void initKeyMap()
    {
        keyHash = null;
        if ( maxKeySize >= 0 )
        {
            keyHash = new LRUMap( maxKeySize );
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Set maxKeySize to: '" + maxKeySize + "'" );
            }
        }
        else
        {
            // If no max size, use a plain map for memory and processing efficiency.
            keyHash = new HashMap();
            // keyHash = Collections.synchronizedMap( new HashMap() );
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Set maxKeySize to unlimited'" );
            }
        }
    }

    /**
     * Dispose of the disk cache in a background thread. Joins against this thread to put a cap on
     * the disposal time.
     * <p>
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
        Thread t = new Thread( disR, "IndexedDiskCache-DisposalThread" );
        t.start();
        // wait up to 60 seconds for dispose and then quit if not done.
        try
        {
            t.join( 60 * 1000 );
        }
        catch ( InterruptedException ex )
        {
            log.error( logCacheName + "Interrupted while waiting for disposal thread to finish.", ex );
        }
    }

    /**
     * Internal method that handles the disposal.
     */
    private void disposeInternal()
    {
        if ( !alive )
        {
            log.error( logCacheName + "Not alive and dispose was called, filename: " + fileName );
            return;
        }

        // Prevents any interaction with the cache while we're shutting down.
        alive = false;

        Thread optimizationThread = currentOptimizationThread;
        if ( isRealTimeOptimizationEnabled && optimizationThread != null )
        {
            // Join with the current optimization thread.
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "In dispose, optimization already " + "in progress; waiting for completion." );
            }
            try
            {
                optimizationThread.join();
            }
            catch ( InterruptedException e )
            {
                log.error( logCacheName + "Unable to join current optimization thread.", e );
            }
        }
        else if ( isShutdownOptimizationEnabled && this.getBytesFree() > 0 )
        {
            optimizeFile();
        }

        saveKeys();

        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Closing files, base filename: " + fileName );
            }
            dataFile.close();
            dataFile = null;
            keyFile.close();
            keyFile = null;
        }
        catch ( IOException e )
        {
            log.error( logCacheName + "Failure closing files in dispose, filename: " + fileName, e );
        }

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Shutdown complete." );
        }
    }

    /**
     * Add descriptor to recycle bin if it is not null. Adds the length of the item to the bytes
     * free.
     * <p>
     * @param ded
     */
    private void addToRecycleBin( IndexedDiskElementDescriptor ded )
    {
        // reuse the spot
        if ( ded != null )
        {
            this.adjustBytesFree( ded, true );

            if ( doRecycle )
            {

                recycle.add( ded );
                if ( log.isDebugEnabled() )
                {
                    log.debug( logCacheName + "recycled ded" + ded );
                }

            }
        }
    }

    /**
     * Performs the check for optimization, and if it is required, do it.
     */
    private void doOptimizeRealTime()
    {
        if ( isRealTimeOptimizationEnabled && !isOptimizing && ( removeCount++ >= cattr.getOptimizeAtRemoveCount() ) )
        {
            isOptimizing = true;

            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Optimizing file. removeCount [" + removeCount + "] OptimizeAtRemoveCount ["
                    + cattr.getOptimizeAtRemoveCount() + "]" );
            }

            if ( currentOptimizationThread == null )
            {
                try
                {
                    storageLock.writeLock().acquire();
                    if ( currentOptimizationThread == null )
                    {
                        currentOptimizationThread = new Thread( new Runnable()
                        {
                            public void run()
                            {
                                optimizeFile();

                                currentOptimizationThread = null;
                            }
                        }, "IndexedDiskCache-OptimizationThread" );
                    }
                }
                catch ( InterruptedException e )
                {
                    log.error( logCacheName + "Unable to aquire storage write lock.", e );
                }
                finally
                {
                    storageLock.writeLock().release();
                }

                if ( currentOptimizationThread != null )
                {
                    currentOptimizationThread.start();
                }
            }
        }
    }

    /**
     * File optimization is handled by this method. It works as follows:
     * <ol>
     * <li>Shutdown recycling and turn on queuing of puts. </li>
     * <li>Take a snapshot of the current descriptors. If there are any removes, ignore them, as
     * they will be compacted during the next optimization.</li>
     * <li>Optimize the snapshot. For each descriptor:
     * <ol>
     * <li>Obtain the write-lock.</li>
     * <li>Shift the element on the disk, in order to compact out the free space. </li>
     * <li>Release the write-lock. This allows elements to still be accessible during optimization.</li>
     * </ol>
     * <li>Obtain the write-lock.</li>
     * <li>All queued puts are made at the end of the file. Optimize these under a single
     * write-lock.</li>
     * <li>Truncate the file.</li>
     * <li>Release the write-lock.</li>
     * <li>Restore system to standard operation.</li>
     * </ol>
     */
    protected void optimizeFile()
    {
        ElapsedTimer timer = new ElapsedTimer();
        timesOptimized++;
        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Beginning Optimization #" + timesOptimized );
        }

        // CREATE SNAPSHOT
        IndexedDiskElementDescriptor[] defragList = null;
        try
        {
            storageLock.writeLock().acquire();
            queueInput = true;
            // shut off recycle while we're optimizing,
            doRecycle = false;
            defragList = createPositionSortedDescriptorList();
            // Release iff I aquired.
            storageLock.writeLock().release();
        }
        catch ( InterruptedException e )
        {
            log.error( logCacheName + "Error setting up optimization.", e );
            return;
        }

        // Defrag the file outside of the write lock. This allows a move to be made,
        // and yet have the element still accessible for reading or writing.
        long expectedNextPos = defragFile( defragList, 0 );

        // ADD THE QUEUED ITEMS to the end and then truncate
        try
        {
            storageLock.writeLock().acquire();

            if ( !queuedPutList.isEmpty() )
            {
                // This is perhaps unecessary, but the list might not be as sorted as we think.
                defragList = new IndexedDiskElementDescriptor[queuedPutList.size()];
                queuedPutList.toArray( defragList );
                Arrays.sort( defragList, new PositionComparator() );

                // pack them at the end
                expectedNextPos = defragFile( defragList, expectedNextPos );
            }
            // TRUNCATE THE FILE
            dataFile.truncate( expectedNextPos );
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Error optimizing queued puts.", e );
        }
        finally
        {
            // RESTORE NORMAL OPERATION
            removeCount = 0;
            bytesFree = 0;
            initRecycleBin();
            queuedPutList.clear();
            queueInput = false;
            // turn recycle back on.
            doRecycle = true;
            isOptimizing = false;

            storageLock.writeLock().release();
        }

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Finished #" + timesOptimized + " Optimization took "
                + timer.getElapsedTimeString() );
        }
    }

    /**
     * Defragments the file inplace by compacting out the free space (i.e., moving records forward).
     * If there were no gaps the resulting file would be the same size as the previous file. This
     * must be supplied an ordered defragList.
     * <p>
     * @param defragList sorted list of descriptors for optimization
     * @param startingPos the start position in the file
     * @return this is the potential new file end
     */
    private long defragFile( IndexedDiskElementDescriptor[] defragList, long startingPos )
    {
        ElapsedTimer timer = new ElapsedTimer();
        long preFileSize = 0;
        long postFileSize = 0;
        long expectedNextPos = 0;
        try
        {
            preFileSize = this.dataFile.length();
            // find the first gap in the disk and start defragging.
            expectedNextPos = startingPos;
            for ( int i = 0; i < defragList.length; i++ )
            {
                storageLock.writeLock().acquire();
                try
                {
                    if ( expectedNextPos != defragList[i].pos )
                    {
                        dataFile.move( defragList[i], expectedNextPos );
                    }
                    expectedNextPos = defragList[i].pos + IndexedDisk.RECORD_HEADER + defragList[i].len;
                }
                finally
                {
                    storageLock.writeLock().release();
                }
            }

            postFileSize = this.dataFile.length();

            // this is the potential new file end
            return expectedNextPos;
        }
        catch ( IOException e )
        {
            log.error( logCacheName + "Error occurred during defragmentation.", e );
        }
        catch ( InterruptedException e )
        {
            log.error( logCacheName + "Threading problem", e );
        }
        finally
        {
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Defragmentation took " + timer.getElapsedTimeString()
                    + ". File Size (before=" + preFileSize + ") (after=" + postFileSize + ") (truncating to "
                    + expectedNextPos + ")" );
            }
        }

        return 0;
    }

    /**
     * Creates a snapshot of the IndexedDiskElementDescriptors in the keyHash and returns them
     * sorted by position in the dataFile.
     * <p>
     * TODO fix values() method on the LRU map.
     * <p>
     * @return IndexedDiskElementDescriptor[]
     */
    private IndexedDiskElementDescriptor[] createPositionSortedDescriptorList()
    {
        IndexedDiskElementDescriptor[] defragList = new IndexedDiskElementDescriptor[keyHash.size()];
        Iterator iterator = keyHash.entrySet().iterator();
        for ( int i = 0; iterator.hasNext(); i++ )
        {
            Object next = iterator.next();
            defragList[i] = (IndexedDiskElementDescriptor) ( (Map.Entry) next ).getValue();
        }

        Arrays.sort( defragList, new PositionComparator() );

        return defragList;
    }

    /**
     * Returns the current cache size.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return keyHash.size();
    }

    /**
     * Returns the size of the recyclebin in number of elements.
     * <p>
     * @return The number of items in the bin.
     */
    protected int getRecyleBinSize()
    {
        return this.recycle.size();
    }

    /**
     * Returns the number of times we have used spots from the recycle bin.
     * <p>
     * @return The number of spots used.
     */
    protected int getRecyleCount()
    {
        return this.recycleCnt;
    }

    /**
     * Returns the number of bytes that are free. When an item is removed, its length is recorded.
     * When a spot is used form the recycle bin, the length of the item stored is recorded.
     * <p>
     * @return The number bytes free on the disk file.
     */
    protected synchronized long getBytesFree()
    {
        return this.bytesFree;
    }

    /**
     * To subtract you can pass in false for add..
     * <p>
     * @param ded
     * @param add
     */
    private synchronized void adjustBytesFree( IndexedDiskElementDescriptor ded, boolean add )
    {
        if ( ded != null )
        {
            int amount = ded.len + IndexedDisk.RECORD_HEADER;

            if ( add )
            {
                this.bytesFree += amount;
            }
            else
            {
                this.bytesFree -= amount;
            }
        }
    }

    /**
     * This is for debugging and testing.
     * <p>
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
            if ( dataFile != null )
            {
                size = dataFile.length();
            }
        }
        catch ( InterruptedException e )
        {
            // nothing
        }
        finally
        {
            storageLock.readLock().release();
        }
        return size;
    }

    /**
     * For debugging. This dumps the values by defualt.
     */
    public void dump()
    {
        dump( true );
    }

    /**
     * For debugging.
     * <p>
     * @param dumpValues A boolean indicating if values should be dumped.
     */
    public void dump( boolean dumpValues )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "[dump] Number of keys: " + keyHash.size() );

            Iterator itr = keyHash.entrySet().iterator();

            while ( itr.hasNext() )
            {
                Map.Entry e = (Map.Entry) itr.next();
                Serializable key = (Serializable) e.getKey();
                IndexedDiskElementDescriptor ded = (IndexedDiskElementDescriptor) e.getValue();

                log.debug( logCacheName + "[dump] Disk element, key: " + key + ", pos: " + ded.pos + ", ded.len"
                    + ded.len + ( ( dumpValues ) ? ( ", val: " + get( key ) ) : "" ) );
            }
        }
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.cattr;
    }

    /**
     * Increments the hit count in a thread safe manner.
     */
    private synchronized void incrementHitCount()
    {
        hitCount++;
    }

    /**
     * Gets basic stats for the disk cache.
     * <p>
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * Returns info about the disk cache.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
     */
    public synchronized IStats getStatistics()
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
        se.setName( "Hit Count" );
        se.setData( "" + this.hitCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Bytes Free" );
        se.setData( "" + this.bytesFree );
        elems.add( se );

        se = new StatElement();
        se.setName( "Optimize Operation Count" );
        se.setData( "" + this.removeCount );
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
        se.setName( "Recycle Bin Size" );
        se.setData( "" + this.recycle.size() );
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

    /**
     * This is exposed for testing.
     * <p>
     * @return Returns the timesOptimized.
     */
    protected int getTimesOptimized()
    {
        return timesOptimized;
    }

    /**
     * Compares IndexedDiskElementDescriptor based on their position.
     * <p>
     */
    private static final class PositionComparator
        implements Comparator
    {
        /**
         * Compares two descriptors based on position.
         * <p>
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare( Object o1, Object o2 )
        {
            IndexedDiskElementDescriptor ded1 = (IndexedDiskElementDescriptor) o1;
            IndexedDiskElementDescriptor ded2 = (IndexedDiskElementDescriptor) o2;

            if ( ded1.pos < ded2.pos )
            {
                return -1;
            }
            else if ( ded1.pos == ded2.pos )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }

    /**
     * Class for recylcing and lru. This implments the LRU overflow callback, so we can add items to
     * the recycle bin.
     */
    public class LRUMap
        extends LRUMapJCS
    {
        /** Don't change */
        private static final long serialVersionUID = 4955079991472142198L;

        /**
         * <code>tag</code> tells us which map we are working on.
         */
        public String tag = "orig";

        /**
         * Default
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

        /**
         * This is called when the may key size is reaced. The least recently used item will be
         * passed here. We will store the position and size of the spot on disk in the recycle bin.
         * <p>
         * @param key
         * @param value
         */
        protected void processRemovedLRU( Object key, Object value )
        {
            addToRecycleBin( (IndexedDiskElementDescriptor) value );
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Removing key: [" + key + "] from key store." );
                log.debug( logCacheName + "Key store size: [" + this.size() + "]." );
            }

            doOptimizeRealTime();
        }
    }

    /**
     * Called on shutdown. This gives use a chance to store the keys and to optimize even if the
     * cache manager's shutdown method was not called.
     */
    class ShutdownHook
        extends Thread
    {
        /**
         * This will persist the keys on shutdown.
         * <p>
         * @see java.lang.Thread#run()
         */
        public void run()
        {
            if ( alive )
            {
                log.warn( logCacheName + "Disk cache not shutdown properly, shutting down now." );
                doDispose();
            }
        }
    }
}
