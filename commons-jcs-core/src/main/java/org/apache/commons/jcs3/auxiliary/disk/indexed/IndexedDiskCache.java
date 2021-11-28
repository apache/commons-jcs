package org.apache.commons.jcs3.auxiliary.disk.indexed;

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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache;
import org.apache.commons.jcs3.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.control.group.GroupId;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.struct.AbstractLRUMap;
import org.apache.commons.jcs3.utils.struct.LRUMap;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;

/**
 * Disk cache that uses a RandomAccessFile with keys stored in memory. The maximum number of keys
 * stored in memory is configurable. The disk cache tries to recycle spots on disk to limit file
 * expansion.
 */
public class IndexedDiskCache<K, V> extends AbstractDiskCache<K, V>
{
    /** The logger */
    private static final Log log = LogManager.getLog(IndexedDiskCache.class);

    /** Cache name used in log messages */
    protected final String logCacheName;

    /** The name of the file where the data is stored */
    private final String fileName;

    /** The IndexedDisk manages reads and writes to the data file. */
    private IndexedDisk dataFile;

    /** The IndexedDisk manages reads and writes to the key file. */
    private IndexedDisk keyFile;

    /** Map containing the keys and disk offsets. */
    private final Map<K, IndexedDiskElementDescriptor> keyHash;

    /** The maximum number of keys that we will keep in memory. */
    private final int maxKeySize;

    /** A handle on the data file. */
    private File rafDir;

    /** Should we keep adding to the recycle bin. False during optimization. */
    private boolean doRecycle = true;

    /** Should we optimize real time */
    private boolean isRealTimeOptimizationEnabled = true;

    /** Should we optimize on shutdown. */
    private boolean isShutdownOptimizationEnabled = true;

    /** are we currently optimizing the files */
    private boolean isOptimizing;

    /** The number of times the file has been optimized. */
    private int timesOptimized;

    /** The thread optimizing the file. */
    private volatile Thread currentOptimizationThread;

    /** used for counting the number of requests */
    private int removeCount;

    /** Should we queue puts. True when optimizing. We write the queue post optimization. */
    private boolean queueInput;

    /** list where puts made during optimization are made */
    private final ConcurrentSkipListSet<IndexedDiskElementDescriptor> queuedPutList;

    /** RECYCLE BIN -- array of empty spots */
    private final ConcurrentSkipListSet<IndexedDiskElementDescriptor> recycle;

    /** User configurable parameters */
    private final IndexedDiskCacheAttributes cattr;

    /** How many slots have we recycled. */
    private int recycleCnt;

    /** How many items were there on startup. */
    private int startupSize;

    /** the number of bytes free on disk. */
    private final AtomicLong bytesFree = new AtomicLong();

    /** mode we are working on (size or count limited **/
    private DiskLimitType diskLimitType = DiskLimitType.COUNT;

    /** simple stat */
    private final AtomicInteger hitCount = new AtomicInteger(0);

    /**
     * Use this lock to synchronize reads and writes to the underlying storage mechanism.
     */
    protected ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();

    /**
     * Constructor for the DiskCache object.
     * <p>
     *
     * @param cacheAttributes
     */
    public IndexedDiskCache(final IndexedDiskCacheAttributes cacheAttributes)
    {
        this(cacheAttributes, null);
    }

    /**
     * Constructor for the DiskCache object.
     * <p>
     *
     * @param cattr
     * @param elementSerializer
     *            used if supplied, the super's super will not set a null
     */
    public IndexedDiskCache(final IndexedDiskCacheAttributes cattr, final IElementSerializer elementSerializer)
    {
        super(cattr);

        setElementSerializer(elementSerializer);

        this.cattr = cattr;
        this.maxKeySize = cattr.getMaxKeySize();
        this.isRealTimeOptimizationEnabled = cattr.getOptimizeAtRemoveCount() > 0;
        this.isShutdownOptimizationEnabled = cattr.isOptimizeOnShutdown();
        this.logCacheName = "Region [" + getCacheName() + "] ";
        this.diskLimitType = cattr.getDiskLimitType();
        // Make a clean file name
        this.fileName = getCacheName().replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        this.keyHash = createInitialKeyMap();
        this.queuedPutList = new ConcurrentSkipListSet<>(new PositionComparator());
        this.recycle = new ConcurrentSkipListSet<>();

        try
        {
            initializeFileSystem(cattr);
            initializeKeysAndData(cattr);

            // Initialization finished successfully, so set alive to true.
            setAlive(true);
            log.info("{0}: Indexed Disk Cache is alive.", logCacheName);

            // TODO: Should we improve detection of whether or not the file should be optimized.
            if (isRealTimeOptimizationEnabled && !keyHash.isEmpty())
            {
                // Kick off a real time optimization, in case we didn't do a final optimization.
                doOptimizeRealTime();
            }
        }
        catch (final IOException e)
        {
            log.error("{0}: Failure initializing for fileName: {1} and directory: {2}",
                    logCacheName, fileName, this.rafDir.getAbsolutePath(), e);
        }
    }

    /**
     * Tries to create the root directory if it does not already exist.
     * <p>
     *
     * @param cattr
     */
    private void initializeFileSystem(final IndexedDiskCacheAttributes cattr)
    {
        this.rafDir = cattr.getDiskPath();
        log.info("{0}: Cache file root directory: {1}", logCacheName, rafDir);
    }

    /**
     * Creates the key and data disk caches.
     * <p>
     * Loads any keys if they are present and ClearDiskOnStartup is false.
     * <p>
     *
     * @param cattr
     * @throws IOException
     */
    private void initializeKeysAndData(final IndexedDiskCacheAttributes cattr) throws IOException
    {
        this.dataFile = new IndexedDisk(new File(rafDir, fileName + ".data"), getElementSerializer());
        this.keyFile = new IndexedDisk(new File(rafDir, fileName + ".key"), getElementSerializer());

        if (cattr.isClearDiskOnStartup())
        {
            log.info("{0}: ClearDiskOnStartup is set to true. Ignoring any persisted data.",
                    logCacheName);
            initializeEmptyStore();
        }
        else if (!keyFile.isEmpty())
        {
            // If the key file has contents, try to initialize the keys
            // from it. In no keys are loaded reset the data file.
            initializeStoreFromPersistedData();
        }
        else
        {
            // Otherwise start with a new empty map for the keys, and reset
            // the data file if it has contents.
            initializeEmptyStore();
        }
    }

    /**
     * Initializes an empty disk cache.
     * <p>
     *
     * @throws IOException
     */
    private void initializeEmptyStore() throws IOException
    {
        this.keyHash.clear();

        if (!dataFile.isEmpty())
        {
            dataFile.reset();
        }
    }

    /**
     * Loads any persisted data and checks for consistency. If there is a consistency issue, the
     * files are cleared.
     * <p>
     *
     * @throws IOException
     */
    private void initializeStoreFromPersistedData() throws IOException
    {
        loadKeys();

        if (keyHash.isEmpty())
        {
            dataFile.reset();
        }
        else
        {
            final boolean isOk = checkKeyDataConsistency(false);
            if (!isOk)
            {
                keyHash.clear();
                keyFile.reset();
                dataFile.reset();
                log.warn("{0}: Corruption detected. Resetting data and keys files.", logCacheName);
            }
            else
            {
                synchronized (this)
                {
                    startupSize = keyHash.size();
                }
            }
        }
    }

    /**
     * Loads the keys from the .key file. The keys are stored in a HashMap on disk. This is
     * converted into a LRUMap.
     */
    protected void loadKeys()
    {
        log.debug("{0}: Loading keys for {1}", () -> logCacheName, () -> keyFile.toString());

        storageLock.writeLock().lock();

        try
        {
            // clear a key map to use.
            keyHash.clear();

            final HashMap<K, IndexedDiskElementDescriptor> keys = keyFile.readObject(
                new IndexedDiskElementDescriptor(0, (int) keyFile.length() - IndexedDisk.HEADER_SIZE_BYTES));

            if (keys != null)
            {
                log.debug("{0}: Found {1} in keys file.", logCacheName, keys.size());

                keyHash.putAll(keys);

                log.info("{0}: Loaded keys from [{1}], key count: {2}; up to {3} will be available.",
                        () -> logCacheName, () -> fileName, keyHash::size, () -> maxKeySize);
            }

            if (log.isTraceEnabled())
            {
                dump(false);
            }
        }
        catch (final Exception e)
        {
            log.error("{0}: Problem loading keys for file {1}", logCacheName, fileName, e);
        }
        finally
        {
            storageLock.writeLock().unlock();
        }
    }

    /**
     * Check for minimal consistency between the keys and the datafile. Makes sure no starting
     * positions in the keys exceed the file length.
     * <p>
     * The caller should take the appropriate action if the keys and data are not consistent.
     *
     * @param checkForDedOverlaps
     *            if <code>true</code>, do a more thorough check by checking for
     *            data overlap
     * @return <code>true</code> if the test passes
     */
    private boolean checkKeyDataConsistency(final boolean checkForDedOverlaps)
    {
        final ElapsedTimer timer = new ElapsedTimer();
        log.debug("{0}: Performing inital consistency check", logCacheName);

        boolean isOk = true;
        try
        {
            final long fileLength = dataFile.length();

            final IndexedDiskElementDescriptor corruptDed = keyHash.values().stream()
                .filter(ded -> ded.pos + IndexedDisk.HEADER_SIZE_BYTES + ded.len > fileLength)
                .findFirst()
                .orElse(null);

            if (corruptDed != null)
            {
                isOk = false;
                log.warn("{0}: The dataFile is corrupted!\n raf.length() = {1}\n ded.pos = {2}",
                        logCacheName, fileLength, corruptDed.pos);
            }
            else if (checkForDedOverlaps)
            {
                isOk = checkForDedOverlaps(createPositionSortedDescriptorList());
            }
        }
        catch (final IOException e)
        {
            log.error(e);
            isOk = false;
        }

        log.info("{0}: Finished inital consistency check, isOk = {1} in {2}",
                logCacheName, isOk, timer.getElapsedTimeString());

        return isOk;
    }

    /**
     * Detects any overlapping elements. This expects a sorted list.
     * <p>
     * The total length of an item is IndexedDisk.RECORD_HEADER + ded.len.
     * <p>
     *
     * @param sortedDescriptors
     * @return false if there are overlaps.
     */
    protected boolean checkForDedOverlaps(final IndexedDiskElementDescriptor[] sortedDescriptors)
    {
        final ElapsedTimer timer = new ElapsedTimer();
        boolean isOk = true;
        long expectedNextPos = 0;
        for (final IndexedDiskElementDescriptor ded : sortedDescriptors) {
            if (expectedNextPos > ded.pos)
            {
                log.error("{0}: Corrupt file: overlapping deds {1}", logCacheName, ded);
                isOk = false;
                break;
            }
            expectedNextPos = ded.pos + IndexedDisk.HEADER_SIZE_BYTES + ded.len;
        }
        log.debug("{0}: Check for DED overlaps took {1} ms.", () -> logCacheName,
                timer::getElapsedTime);

        return isOk;
    }

    /**
     * Saves key file to disk. This converts the LRUMap to a HashMap for deserialization.
     */
    protected void saveKeys()
    {
        try
        {
            log.info("{0}: Saving keys to: {1}, key count: {2}",
                    () -> logCacheName, () -> fileName, keyHash::size);

            keyFile.reset();

            final HashMap<K, IndexedDiskElementDescriptor> keys = new HashMap<>(keyHash);
            if (!keys.isEmpty())
            {
                keyFile.writeObject(keys, 0);
            }

            log.info("{0}: Finished saving keys.", logCacheName);
        }
        catch (final IOException e)
        {
            log.error("{0}: Problem storing keys.", logCacheName, e);
        }
    }

    /**
     * Update the disk cache. Called from the Queue. Makes sure the Item has not been retrieved from
     * purgatory while in queue for disk. Remove items from purgatory when they go to disk.
     * <p>
     *
     * @param ce
     *            The ICacheElement&lt;K, V&gt; to put to disk.
     */
    @Override
    protected void processUpdate(final ICacheElement<K, V> ce)
    {
        if (!isAlive())
        {
            log.error("{0}: No longer alive; aborting put of key = {1}",
                    () -> logCacheName, ce::getKey);
            return;
        }

        log.debug("{0}: Storing element on disk, key: {1}",
                () -> logCacheName, ce::getKey);

        // old element with same key
        IndexedDiskElementDescriptor old = null;

        try
        {
            IndexedDiskElementDescriptor ded = null;
            final byte[] data = getElementSerializer().serialize(ce);

            // make sure this only locks for one particular cache region
            storageLock.writeLock().lock();
            try
            {
                old = keyHash.get(ce.getKey());

                // Item with the same key already exists in file.
                // Try to reuse the location if possible.
                if (old != null && data.length <= old.len)
                {
                    // Reuse the old ded. The defrag relies on ded updates by reference, not
                    // replacement.
                    ded = old;
                    ded.len = data.length;
                }
                else
                {
                    // we need this to compare in the recycle bin
                    ded = new IndexedDiskElementDescriptor(dataFile.length(), data.length);

                    if (doRecycle)
                    {
                        final IndexedDiskElementDescriptor rep = recycle.ceiling(ded);
                        if (rep != null)
                        {
                            // remove element from recycle bin
                            recycle.remove(rep);
                            ded = rep;
                            ded.len = data.length;
                            recycleCnt++;
                            this.adjustBytesFree(ded, false);
                            log.debug("{0}: using recycled ded {1} rep.len = {2} ded.len = {3}",
                                    logCacheName, ded.pos, rep.len, ded.len);
                        }
                    }

                    // Put it in the map
                    keyHash.put(ce.getKey(), ded);

                    if (queueInput)
                    {
                        queuedPutList.add(ded);
                        log.debug("{0}: added to queued put list. {1}",
                                () -> logCacheName, queuedPutList::size);
                    }

                    // add the old slot to the recycle bin
                    if (old != null)
                    {
                        addToRecycleBin(old);
                    }
                }

                dataFile.write(ded, data);
            }
            finally
            {
                storageLock.writeLock().unlock();
            }

            log.debug("{0}: Put to file: {1}, key: {2}, position: {3}, size: {4}",
                    logCacheName, fileName, ce.getKey(), ded.pos, ded.len);
        }
        catch (final IOException e)
        {
            log.error("{0}: Failure updating element, key: {1} old: {2}",
                    logCacheName, ce.getKey(), old, e);
        }
    }

    /**
     * Gets the key, then goes to disk to get the object.
     * <p>
     *
     * @param key
     * @return ICacheElement&lt;K, V&gt; or null
     * @see AbstractDiskCache#doGet
     */
    @Override
    protected ICacheElement<K, V> processGet(final K key)
    {
        if (!isAlive())
        {
            log.error("{0}: No longer alive so returning null for key = {1}",
                    logCacheName, key);
            return null;
        }

        log.debug("{0}: Trying to get from disk: {1}", logCacheName, key);

        ICacheElement<K, V> object = null;
        try
        {
            storageLock.readLock().lock();
            try
            {
                object = readElement(key);
            }
            finally
            {
                storageLock.readLock().unlock();
            }

            if (object != null)
            {
                hitCount.incrementAndGet();
            }
        }
        catch (final IOException ioe)
        {
            log.error("{0}: Failure getting from disk, key = {1}", logCacheName, key, ioe);
            reset();
        }
        return object;
    }

    /**
     * Gets matching items from the cache.
     * <p>
     *
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache matching keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> processGetMatching(final String pattern)
    {
        final Map<K, ICacheElement<K, V>> elements = new HashMap<>();
        Set<K> keyArray = null;
        storageLock.readLock().lock();
        try
        {
            keyArray = new HashSet<>(keyHash.keySet());
        }
        finally
        {
            storageLock.readLock().unlock();
        }

        final Set<K> matchingKeys = getKeyMatcher().getMatchingKeysFromArray(pattern, keyArray);

        for (final K key : matchingKeys)
        {
            final ICacheElement<K, V> element = processGet(key);
            if (element != null)
            {
                elements.put(key, element);
            }
        }
        return elements;
    }

    /**
     * Reads the item from disk.
     * <p>
     *
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    private ICacheElement<K, V> readElement(final K key) throws IOException
    {
        final IndexedDiskElementDescriptor ded = keyHash.get(key);

        if (ded != null)
        {
            log.debug("{0}: Found on disk, key: ", logCacheName, key);

            try
            {
                return dataFile.readObject(ded);
                // TODO consider checking key equality and throwing if there is a failure
            }
            catch (final IOException e)
            {
                log.error("{0}: IO Exception, Problem reading object from file", logCacheName, e);
                throw e;
            }
            catch (final Exception e)
            {
                log.error("{0}: Exception, Problem reading object from file", logCacheName, e);
                throw new IOException(logCacheName + "Problem reading object from disk.", e);
            }
        }

        return null;
    }

    /**
     * Return the keys in this cache.
     * <p>
     *
     * @see org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        final HashSet<K> keys = new HashSet<>();

        storageLock.readLock().lock();

        try
        {
            keys.addAll(this.keyHash.keySet());
        }
        finally
        {
            storageLock.readLock().unlock();
        }

        return keys;
    }

    /**
     * Returns true if the removal was successful; or false if there is nothing to remove. Current
     * implementation always result in a disk orphan.
     * <p>
     *
     * @return true if at least one item was removed.
     * @param key
     */
    @Override
    protected boolean processRemove(final K key)
    {
        if (!isAlive())
        {
            log.error("{0}: No longer alive so returning false for key = {1}", logCacheName, key);
            return false;
        }

        if (key == null)
        {
            return false;
        }

        boolean removed = false;
        try
        {
            storageLock.writeLock().lock();

            if (key instanceof String && key.toString().endsWith(NAME_COMPONENT_DELIMITER))
            {
                removed = performPartialKeyRemoval((String) key);
            }
            else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName == null)
            {
                removed = performGroupRemoval(((GroupAttrName<?>) key).groupId);
            }
            else
            {
                removed = performSingleKeyRemoval(key);
            }
        }
        finally
        {
            storageLock.writeLock().unlock();
        }

        // this increments the remove count.
        // there is no reason to call this if an item was not removed.
        if (removed)
        {
            doOptimizeRealTime();
        }

        return removed;
    }

    /**
     * Iterates over the keyset. Builds a list of matches. Removes all the keys in the list. Does
     * not remove via the iterator, since the map impl may not support it.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     *
     * @param key
     * @return true if there was a match
     */
    private boolean performPartialKeyRemoval(final String key)
    {
        boolean removed = false;

        // remove all keys of the same name hierarchy.
        final List<K> itemsToRemove = new LinkedList<>();

        for (final K k : keyHash.keySet())
        {
            if (k instanceof String && k.toString().startsWith(key))
            {
                itemsToRemove.add(k);
            }
        }

        // remove matches.
        for (final K fullKey : itemsToRemove)
        {
            // Don't add to recycle bin here
            // https://issues.apache.org/jira/browse/JCS-67
            performSingleKeyRemoval(fullKey);
            removed = true;
            // TODO this needs to update the remove count separately
        }

        return removed;
    }

    /**
     * Remove all elements from the group. This does not use the iterator to remove. It builds a
     * list of group elements and then removes them one by one.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     *
     * @param key
     * @return true if an element was removed
     */
    private boolean performGroupRemoval(final GroupId key)
    {
        boolean removed = false;

        // remove all keys of the same name group.
        final List<K> itemsToRemove = new LinkedList<>();

        // remove all keys of the same name hierarchy.
        for (final K k : keyHash.keySet())
        {
            if (k instanceof GroupAttrName && ((GroupAttrName<?>) k).groupId.equals(key))
            {
                itemsToRemove.add(k);
            }
        }

        // remove matches.
        for (final K fullKey : itemsToRemove)
        {
            // Don't add to recycle bin here
            // https://issues.apache.org/jira/browse/JCS-67
            performSingleKeyRemoval(fullKey);
            removed = true;
            // TODO this needs to update the remove count separately
        }

        return removed;
    }

    /**
     * Removes an individual key from the cache.
     * <p>
     * This operates under a lock obtained in doRemove().
     * <p>
     *
     * @param key
     * @return true if an item was removed.
     */
    private boolean performSingleKeyRemoval(final K key)
    {
        final boolean removed;
        // remove single item.
        final IndexedDiskElementDescriptor ded = keyHash.remove(key);
        removed = ded != null;
        addToRecycleBin(ded);

        log.debug("{0}: Disk removal: Removed from key hash, key [{1}] removed = {2}",
                logCacheName, key, removed);
        return removed;
    }

    /**
     * Remove all the items from the disk cache by resetting everything.
     */
    @Override
    public void processRemoveAll()
    {
        final ICacheEvent<String> cacheEvent =
                createICacheEvent(getCacheName(), "all", ICacheEventLogger.REMOVEALL_EVENT);
        try
        {
            reset();
        }
        finally
        {
            logICacheEvent(cacheEvent);
        }
    }

    /**
     * Reset effectively clears the disk cache, creating new files, recycle bins, and keymaps.
     * <p>
     * It can be used to handle errors by last resort, force content update, or removeall.
     */
    private void reset()
    {
        log.info("{0}: Resetting cache", logCacheName);

        try
        {
            storageLock.writeLock().lock();

            if (dataFile != null)
            {
                dataFile.close();
            }

            final File dataFileTemp = new File(rafDir, fileName + ".data");
            Files.delete(dataFileTemp.toPath());

            if (keyFile != null)
            {
                keyFile.close();
            }
            final File keyFileTemp = new File(rafDir, fileName + ".key");
            Files.delete(keyFileTemp.toPath());

            dataFile = new IndexedDisk(dataFileTemp, getElementSerializer());
            keyFile = new IndexedDisk(keyFileTemp, getElementSerializer());

            this.recycle.clear();
            this.keyHash.clear();
        }
        catch (final IOException e)
        {
            log.error("{0}: Failure resetting state", logCacheName, e);
        }
        finally
        {
            storageLock.writeLock().unlock();
        }
    }

    /**
     * Create the map for keys that contain the index position on disk.
     *
     * @return a new empty Map for keys and IndexedDiskElementDescriptors
     */
    private Map<K, IndexedDiskElementDescriptor> createInitialKeyMap()
    {
        Map<K, IndexedDiskElementDescriptor> keyMap = null;
        if (maxKeySize >= 0)
        {
            if (this.diskLimitType == DiskLimitType.COUNT)
            {
                keyMap = new LRUMapCountLimited(maxKeySize);
            }
            else
            {
                keyMap = new LRUMapSizeLimited(maxKeySize);
            }

            log.info("{0}: Set maxKeySize to: \"{1}\"", logCacheName, maxKeySize);
        }
        else
        {
            // If no max size, use a plain map for memory and processing efficiency.
            keyMap = new HashMap<>();
            // keyHash = Collections.synchronizedMap( new HashMap() );
            log.info("{0}: Set maxKeySize to unlimited", logCacheName);
        }

        return keyMap;
    }

    /**
     * Dispose of the disk cache in a background thread. Joins against this thread to put a cap on
     * the disposal time.
     * <p>
     * TODO make dispose window configurable.
     */
    @Override
    public void processDispose()
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), "none", ICacheEventLogger.DISPOSE_EVENT);
        try
        {
            final Thread t = new Thread(this::disposeInternal, "IndexedDiskCache-DisposalThread");
            t.start();
            // wait up to 60 seconds for dispose and then quit if not done.
            try
            {
                t.join(60 * 1000);
            }
            catch (final InterruptedException ex)
            {
                log.error("{0}: Interrupted while waiting for disposal thread to finish.",
                        logCacheName, ex);
            }
        }
        finally
        {
            logICacheEvent(cacheEvent);
        }
    }

    /**
     * Internal method that handles the disposal.
     */
    protected void disposeInternal()
    {
        if (!isAlive())
        {
            log.error("{0}: Not alive and dispose was called, filename: {1}",
                    logCacheName, fileName);
            return;
        }

        // Prevents any interaction with the cache while we're shutting down.
        setAlive(false);

        final Thread optimizationThread = currentOptimizationThread;
        if (isRealTimeOptimizationEnabled && optimizationThread != null)
        {
            // Join with the current optimization thread.
            log.debug("{0}: In dispose, optimization already in progress; waiting for completion.",
                    logCacheName);

            try
            {
                optimizationThread.join();
            }
            catch (final InterruptedException e)
            {
                log.error("{0}: Unable to join current optimization thread.",
                        logCacheName, e);
            }
        }
        else if (isShutdownOptimizationEnabled && this.getBytesFree() > 0)
        {
            optimizeFile();
        }

        saveKeys();

        try
        {
            log.debug("{0}: Closing files, base filename: {1}", logCacheName,
                    fileName);
            dataFile.close();
            dataFile = null;
            keyFile.close();
            keyFile = null;
        }
        catch (final IOException e)
        {
            log.error("{0}: Failure closing files in dispose, filename: {1}",
                    logCacheName, fileName, e);
        }

        log.info("{0}: Shutdown complete.", logCacheName);
    }

    /**
     * Add descriptor to recycle bin if it is not null. Adds the length of the item to the bytes
     * free.
     * <p>
     * This is called in three places: (1) When an item is removed. All item removals funnel down to the removeSingleItem method.
     * (2) When an item on disk is updated with a value that will not fit in the previous slot. (3) When the max key size is
     * reached, the freed slot will be added.
     * <p>
     *
     * @param ded
     */
    protected void addToRecycleBin(final IndexedDiskElementDescriptor ded)
    {
        // reuse the spot
        if (ded != null)
        {
            storageLock.readLock().lock();

            try
            {
                adjustBytesFree(ded, true);

                if (doRecycle)
                {
                    recycle.add(ded);
                    log.debug("{0}: recycled ded {1}", logCacheName, ded);
                }
            }
            finally
            {
                storageLock.readLock().unlock();
            }
        }
    }

    /**
     * Performs the check for optimization, and if it is required, do it.
     */
    protected void doOptimizeRealTime()
    {
        if (isRealTimeOptimizationEnabled && !isOptimizing
            && removeCount++ >= cattr.getOptimizeAtRemoveCount())
        {
            isOptimizing = true;

            log.info("{0}: Optimizing file. removeCount [{1}] OptimizeAtRemoveCount [{2}]",
                    logCacheName, removeCount, cattr.getOptimizeAtRemoveCount());

            if (currentOptimizationThread == null)
            {
                storageLock.writeLock().lock();

                try
                {
                    if (currentOptimizationThread == null)
                    {
                        currentOptimizationThread = new Thread(() -> {
                            optimizeFile();
                            currentOptimizationThread = null;
                        }, "IndexedDiskCache-OptimizationThread");
                    }
                }
                finally
                {
                    storageLock.writeLock().unlock();
                }

                if (currentOptimizationThread != null)
                {
                    currentOptimizationThread.start();
                }
            }
        }
    }

    /**
     * File optimization is handled by this method. It works as follows:
     * <ol>
     * <li>Shutdown recycling and turn on queuing of puts.</li>
     * <li>Take a snapshot of the current descriptors. If there are any removes, ignore them, as they will be compacted during the
     * next optimization.</li>
     * <li>Optimize the snapshot. For each descriptor:
     * <ol>
     * <li>Obtain the write-lock.</li>
     * <li>Shift the element on the disk, in order to compact out the free space.</li>
     * <li>Release the write-lock. This allows elements to still be accessible during optimization.</li>
     * </ol>
     * </li>
     * <li>Obtain the write-lock.</li>
     * <li>All queued puts are made at the end of the file. Optimize these under a single write-lock.</li>
     * <li>Truncate the file.</li>
     * <li>Release the write-lock.</li>
     * <li>Restore system to standard operation.</li>
     * </ol>
     */
    protected void optimizeFile()
    {
        final ElapsedTimer timer = new ElapsedTimer();
        timesOptimized++;
        log.info("{0}: Beginning Optimization #{1}", logCacheName, timesOptimized);

        // CREATE SNAPSHOT
        IndexedDiskElementDescriptor[] defragList = null;

        storageLock.writeLock().lock();

        try
        {
            queueInput = true;
            // shut off recycle while we're optimizing,
            doRecycle = false;
            defragList = createPositionSortedDescriptorList();
        }
        finally
        {
            storageLock.writeLock().unlock();
        }

        // Defrag the file outside of the write lock. This allows a move to be made,
        // and yet have the element still accessible for reading or writing.
        long expectedNextPos = defragFile(defragList, 0);

        // ADD THE QUEUED ITEMS to the end and then truncate
        storageLock.writeLock().lock();

        try
        {
            try
            {
                if (!queuedPutList.isEmpty())
                {
                    defragList = queuedPutList.toArray(new IndexedDiskElementDescriptor[queuedPutList.size()]);

                    // pack them at the end
                    expectedNextPos = defragFile(defragList, expectedNextPos);
                }
                // TRUNCATE THE FILE
                dataFile.truncate(expectedNextPos);
            }
            catch (final IOException e)
            {
                log.error("{0}: Error optimizing queued puts.", logCacheName, e);
            }

            // RESTORE NORMAL OPERATION
            removeCount = 0;
            resetBytesFree();
            this.recycle.clear();
            queuedPutList.clear();
            queueInput = false;
            // turn recycle back on.
            doRecycle = true;
            isOptimizing = false;
        }
        finally
        {
            storageLock.writeLock().unlock();
        }

        log.info("{0}: Finished #{1}, Optimization took {2}",
                logCacheName, timesOptimized, timer.getElapsedTimeString());
    }

    /**
     * Defragments the file in place by compacting out the free space (i.e., moving records
     * forward). If there were no gaps the resulting file would be the same size as the previous
     * file. This must be supplied an ordered defragList.
     * <p>
     *
     * @param defragList
     *            sorted list of descriptors for optimization
     * @param startingPos
     *            the start position in the file
     * @return this is the potential new file end
     */
    private long defragFile(final IndexedDiskElementDescriptor[] defragList, final long startingPos)
    {
        final ElapsedTimer timer = new ElapsedTimer();
        long preFileSize = 0;
        long postFileSize = 0;
        long expectedNextPos = 0;
        try
        {
            preFileSize = this.dataFile.length();
            // find the first gap in the disk and start defragging.
            expectedNextPos = startingPos;
            for (final IndexedDiskElementDescriptor element : defragList) {
                storageLock.writeLock().lock();
                try
                {
                    if (expectedNextPos != element.pos)
                    {
                        dataFile.move(element, expectedNextPos);
                    }
                    expectedNextPos = element.pos + IndexedDisk.HEADER_SIZE_BYTES + element.len;
                }
                finally
                {
                    storageLock.writeLock().unlock();
                }
            }

            postFileSize = this.dataFile.length();

            // this is the potential new file end
            return expectedNextPos;
        }
        catch (final IOException e)
        {
            log.error("{0}: Error occurred during defragmentation.", logCacheName, e);
        }
        finally
        {
            log.info("{0}: Defragmentation took {1}. File Size (before={2}) (after={3}) (truncating to {4})",
                    logCacheName, timer.getElapsedTimeString(), preFileSize, postFileSize, expectedNextPos);
        }

        return 0;
    }

    /**
     * Creates a snapshot of the IndexedDiskElementDescriptors in the keyHash and returns them
     * sorted by position in the dataFile.
     * <p>
     *
     * @return IndexedDiskElementDescriptor[]
     */
    private IndexedDiskElementDescriptor[] createPositionSortedDescriptorList()
    {
        final List<IndexedDiskElementDescriptor> defragList = new ArrayList<>(keyHash.values());
        Collections.sort(defragList, (ded1, ded2) -> Long.compare(ded1.pos, ded2.pos));

        return defragList.toArray(new IndexedDiskElementDescriptor[0]);
    }

    /**
     * Returns the current cache size.
     * <p>
     *
     * @return The size value
     */
    @Override
    public int getSize()
    {
        return keyHash.size();
    }

    /**
     * Returns the size of the recycle bin in number of elements.
     * <p>
     *
     * @return The number of items in the bin.
     */
    protected int getRecyleBinSize()
    {
        return this.recycle.size();
    }

    /**
     * Returns the number of times we have used spots from the recycle bin.
     * <p>
     *
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
     *
     * @return The number bytes free on the disk file.
     */
    protected long getBytesFree()
    {
        return this.bytesFree.get();
    }

    /**
     * Resets the number of bytes that are free.
     */
    private void resetBytesFree()
    {
        this.bytesFree.set(0);
    }

    /**
     * To subtract you can pass in false for add..
     * <p>
     *
     * @param ded
     * @param add
     */
    private void adjustBytesFree(final IndexedDiskElementDescriptor ded, final boolean add)
    {
        if (ded != null)
        {
            final int amount = ded.len + IndexedDisk.HEADER_SIZE_BYTES;

            if (add)
            {
                this.bytesFree.addAndGet(amount);
            }
            else
            {
                this.bytesFree.addAndGet(-amount);
            }
        }
    }

    /**
     * This is for debugging and testing.
     * <p>
     *
     * @return the length of the data file.
     * @throws IOException
     */
    protected long getDataFileSize() throws IOException
    {
        long size = 0;

        storageLock.readLock().lock();

        try
        {
            if (dataFile != null)
            {
                size = dataFile.length();
            }
        }
        finally
        {
            storageLock.readLock().unlock();
        }

        return size;
    }

    /**
     * For debugging. This dumps the values by default.
     */
    public void dump()
    {
        dump(true);
    }

    /**
     * For debugging.
     * <p>
     *
     * @param dumpValues
     *            A boolean indicating if values should be dumped.
     */
    public void dump(final boolean dumpValues)
    {
        if (log.isTraceEnabled())
        {
            log.trace("{0}: [dump] Number of keys: {1}", logCacheName, keyHash.size());

            for (final Map.Entry<K, IndexedDiskElementDescriptor> e : keyHash.entrySet())
            {
                final K key = e.getKey();
                final IndexedDiskElementDescriptor ded = e.getValue();

                log.trace("{0}: [dump] Disk element, key: {1}, pos: {2}, len: {3}" +
                        (dumpValues ? ", val: " + get(key) : ""),
                        logCacheName, key, ded.pos, ded.len);
            }
        }
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.cattr;
    }

    /**
     * Returns info about the disk cache.
     * <p>
     *
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getStatistics()
     */
    @Override
    public synchronized IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName("Indexed Disk Cache");

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        elems.add(new StatElement<>("Is Alive", Boolean.valueOf(isAlive())));
        elems.add(new StatElement<>("Key Map Size", Integer.valueOf(this.keyHash != null ? this.keyHash.size() : -1)));
        try
        {
            elems.add(
                    new StatElement<>("Data File Length", Long.valueOf(this.dataFile != null ? this.dataFile.length() : -1L)));
        }
        catch (final IOException e)
        {
            log.error(e);
        }
        elems.add(new StatElement<>("Max Key Size", this.maxKeySize));
        elems.add(new StatElement<>("Hit Count", this.hitCount));
        elems.add(new StatElement<>("Bytes Free", this.bytesFree));
        elems.add(new StatElement<>("Optimize Operation Count", Integer.valueOf(this.removeCount)));
        elems.add(new StatElement<>("Times Optimized", Integer.valueOf(this.timesOptimized)));
        elems.add(new StatElement<>("Recycle Count", Integer.valueOf(this.recycleCnt)));
        elems.add(new StatElement<>("Recycle Bin Size", Integer.valueOf(this.recycle.size())));
        elems.add(new StatElement<>("Startup Size", Integer.valueOf(this.startupSize)));

        // get the stats from the super too
        final IStats sStats = super.getStatistics();
        elems.addAll(sStats.getStatElements());

        stats.setStatElements(elems);

        return stats;
    }

    /**
     * This is exposed for testing.
     * <p>
     *
     * @return Returns the timesOptimized.
     */
    protected int getTimesOptimized()
    {
        return timesOptimized;
    }

    /**
     * This is used by the event logging.
     * <p>
     *
     * @return the location of the disk, either path or ip.
     */
    @Override
    protected String getDiskLocation()
    {
        return dataFile.getFilePath();
    }

    /**
     * Compares IndexedDiskElementDescriptor based on their position.
     * <p>
     * @deprecated Use lambda instead
     */
    @Deprecated
    protected static final class PositionComparator implements Comparator<IndexedDiskElementDescriptor>, Serializable
    {
        /** serialVersionUID */
        private static final long serialVersionUID = -8387365338590814113L;

        /**
         * Compares two descriptors based on position.
         * <p>
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(final IndexedDiskElementDescriptor ded1, final IndexedDiskElementDescriptor ded2)
        {
            return Long.compare(ded1.pos, ded2.pos);
        }
    }

    /**
     * Class for recycling and lru. This implements the LRU overflow callback, so we can add items
     * to the recycle bin. This class counts the size element to decide, when to throw away an element
     */
    public class LRUMapSizeLimited extends AbstractLRUMap<K, IndexedDiskElementDescriptor>
    {
        /**
         * <code>tag</code> tells us which map we are working on.
         */
        public static final String TAG = "orig";

        // size of the content in kB
        private final AtomicInteger contentSize;
        private final int maxSize;

        /**
         * Default
         */
        public LRUMapSizeLimited()
        {
            this(-1);
        }

        /**
         * @param maxKeySize
         */
        public LRUMapSizeLimited(final int maxKeySize)
        {
            this.maxSize = maxKeySize;
            this.contentSize = new AtomicInteger(0);
        }

        // keep the content size in kB, so 2^31 kB is reasonable value
        private void subLengthFromCacheSize(final IndexedDiskElementDescriptor value)
        {
            contentSize.addAndGet((value.len + IndexedDisk.HEADER_SIZE_BYTES) / -1024 - 1);
        }

        // keep the content size in kB, so 2^31 kB is reasonable value
        private void addLengthToCacheSize(final IndexedDiskElementDescriptor value)
        {
            contentSize.addAndGet((value.len + IndexedDisk.HEADER_SIZE_BYTES) / 1024 + 1);
        }

        @Override
        public IndexedDiskElementDescriptor put(final K key, final IndexedDiskElementDescriptor value)
        {
            IndexedDiskElementDescriptor oldValue = null;

            try
            {
                oldValue = super.put(key, value);
            }
            finally
            {
                // keep the content size in kB, so 2^31 kB is reasonable value
                if (value != null)
                {
                    addLengthToCacheSize(value);
                }
                if (oldValue != null)
                {
                    subLengthFromCacheSize(oldValue);
                }
            }

            return oldValue;
        }

        @Override
        public IndexedDiskElementDescriptor remove(final Object key)
        {
            IndexedDiskElementDescriptor value = null;

            try
            {
                value = super.remove(key);
                return value;
            }
            finally
            {
                if (value != null)
                {
                    subLengthFromCacheSize(value);
                }
            }
        }

        /**
         * This is called when the may key size is reached. The least recently used item will be
         * passed here. We will store the position and size of the spot on disk in the recycle bin.
         * <p>
         *
         * @param key
         * @param value
         */
        @Override
        protected void processRemovedLRU(final K key, final IndexedDiskElementDescriptor value)
        {
            if (value != null)
            {
                subLengthFromCacheSize(value);
            }

            addToRecycleBin(value);

            log.debug("{0}: Removing key: [{1}] from key store.", logCacheName, key);
            log.debug("{0}: Key store size: [{1}].", logCacheName, this.size());

            doOptimizeRealTime();
        }

        @Override
        protected boolean shouldRemove()
        {
            return maxSize > 0 && contentSize.get() > maxSize && !this.isEmpty();
        }
    }

    /**
     * Class for recycling and lru. This implements the LRU overflow callback, so we can add items
     * to the recycle bin. This class counts the elements to decide, when to throw away an element
     */

    public class LRUMapCountLimited extends LRUMap<K, IndexedDiskElementDescriptor>
    // implements Serializable
    {
        public LRUMapCountLimited(final int maxKeySize)
        {
            super(maxKeySize);
        }

        /**
         * This is called when the may key size is reached. The least recently used item will be
         * passed here. We will store the position and size of the spot on disk in the recycle bin.
         * <p>
         *
         * @param key
         * @param value
         */
        @Override
        protected void processRemovedLRU(final K key, final IndexedDiskElementDescriptor value)
        {
            addToRecycleBin(value);
            log.debug("{0}: Removing key: [{1}] from key store.", logCacheName, key);
            log.debug("{0}: Key store size: [{1}].", logCacheName, this.size());

            doOptimizeRealTime();
        }
    }
}
