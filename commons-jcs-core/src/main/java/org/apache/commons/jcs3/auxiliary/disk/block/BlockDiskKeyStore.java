package org.apache.commons.jcs3.auxiliary.disk.block;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs3.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;
import org.apache.commons.jcs3.io.ObjectInputStreamClassLoaderAware;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.struct.AbstractLRUMap;
import org.apache.commons.jcs3.utils.struct.LRUMap;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;

/**
 * This is responsible for storing the keys.
 * <p>
 *
 * @author Aaron Smuts
 */
public class BlockDiskKeyStore<K>
{
    /**
     * Class for recycling and lru. This implements the LRU overflow callback,
     * so we can mark the blocks as free.
     */
    public class LRUMapCountLimited extends LRUMap<K, int[]>
    {
        /**
         * <code>tag</code> tells us which map we are working on.
         */
        public final static String TAG = "orig-lru-count";

        public LRUMapCountLimited(final int maxKeySize)
        {
            super(maxKeySize);
        }

        /**
         * This is called when the may key size is reached. The least recently
         * used item will be passed here. We will store the position and size of
         * the spot on disk in the recycle bin.
         * <p>
         *
         * @param key
         * @param value
         */
        @Override
        protected void processRemovedLRU(final K key, final int[] value)
        {
            blockDiskCache.freeBlocks(value);
            if (log.isDebugEnabled())
            {
                log.debug("{0}: Removing key: [{1}] from key store.", logCacheName, key);
                log.debug("{0}: Key store size: [{1}].", logCacheName, super.size());
            }
        }
    }

    /**
     * Class for recycling and lru. This implements the LRU size overflow
     * callback, so we can mark the blocks as free.
     */
    public class LRUMapSizeLimited extends AbstractLRUMap<K, int[]>
    {
        /**
         * <code>tag</code> tells us which map we are working on.
         */
        public final static String TAG = "orig-lru-size";

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
         * @param maxSize
         *            maximum cache size in kB
         */
        public LRUMapSizeLimited(final int maxSize)
        {
            this.maxSize = maxSize;
            this.contentSize = new AtomicInteger(0);
        }

        // keep the content size in kB, so 2^31 kB is reasonable value
        private void addLengthToCacheSize(final int[] value)
        {
            contentSize.addAndGet(value.length * blockSize / 1024 + 1);
        }

        /**
         * This is called when the may key size is reached. The least recently
         * used item will be passed here. We will store the position and size of
         * the spot on disk in the recycle bin.
         * <p>
         *
         * @param key
         * @param value
         */
        @Override
        protected void processRemovedLRU(final K key, final int[] value)
        {
            blockDiskCache.freeBlocks(value);
            if (log.isDebugEnabled())
            {
                log.debug("{0}: Removing key: [{1}] from key store.", logCacheName, key);
                log.debug("{0}: Key store size: [{1}].", logCacheName, super.size());
            }

            if (value != null)
            {
                subLengthFromCacheSize(value);
            }
        }

        @Override
        public int[] put(final K key, final int[] value)
        {
            int[] oldValue = null;

            try
            {
                oldValue = super.put(key, value);
            }
            finally
            {
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
        public int[] remove(final Object key)
        {
            int[] value = null;

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

        @Override
        protected boolean shouldRemove()
        {
            return maxSize > 0 && contentSize.get() > maxSize && this.size() > 1;
        }

        // keep the content size in kB, so 2^31 kB is reasonable value
        private void subLengthFromCacheSize(final int[] value)
        {
            contentSize.addAndGet(value.length * blockSize / -1024 - 1);
        }
    }

    /** The logger */
    private static final Log log = LogManager.getLog(BlockDiskKeyStore.class);

    /** Attributes governing the behavior of the block disk cache. */
    private final BlockDiskCacheAttributes blockDiskCacheAttributes;

    /** The key to block map */
    private Map<K, int[]> keyHash;

    /** The file where we persist the keys */
    private final File keyFile;

    /** The name to prefix log messages with. */
    protected final String logCacheName;

    /** Name of the file where we persist the keys */
    private final String fileName;

    /** The maximum number of keys to store in memory */
    private final int maxKeySize;

    /**
     * we need this so we can communicate free blocks to the data store when
     * keys fall off the LRU
     */
    protected final BlockDiskCache<K, ?> blockDiskCache;

    private DiskLimitType diskLimitType = DiskLimitType.COUNT;

    private final int blockSize;

    /**
     * Set the configuration options.
     * <p>
     *
     * @param cacheAttributes
     * @param blockDiskCache
     *            used for freeing
     */
    public BlockDiskKeyStore(final BlockDiskCacheAttributes cacheAttributes, final BlockDiskCache<K, ?> blockDiskCache)
    {
        this.blockDiskCacheAttributes = cacheAttributes;
        this.logCacheName = "Region [" + this.blockDiskCacheAttributes.getCacheName() + "] ";
        this.fileName = this.blockDiskCacheAttributes.getCacheName();
        this.maxKeySize = cacheAttributes.getMaxKeySize();
        this.blockDiskCache = blockDiskCache;
        this.diskLimitType = cacheAttributes.getDiskLimitType();
        this.blockSize = cacheAttributes.getBlockSizeBytes();

        final File rootDirectory = cacheAttributes.getDiskPath();

        log.info("{0}: Cache file root directory [{1}]", logCacheName, rootDirectory);

        this.keyFile = new File(rootDirectory, fileName + ".key");

        log.info("{0}: Key File [{1}]", logCacheName, this.keyFile.getAbsolutePath());

        if (keyFile.length() > 0)
        {
            loadKeys();
            if (!verify())
            {
                log.warn("{0}: Key File is invalid. Resetting file.", logCacheName);
                initKeyMap();
                reset();
            }
        }
        else
        {
            initKeyMap();
        }
    }

    /**
     * This is mainly used for testing. It leave the disk in tact, and just
     * clears memory.
     */
    protected void clearMemoryMap()
    {
        this.keyHash.clear();
    }

    /**
     * Gets the entry set.
     * <p>
     *
     * @return entry set.
     */
    public Set<Map.Entry<K, int[]>> entrySet()
    {
        return this.keyHash.entrySet();
    }

    /**
     * gets the object for the key.
     * <p>
     *
     * @param key
     * @return Object
     */
    public int[] get(final K key)
    {
        return this.keyHash.get(key);
    }

    /**
     * Create the map for keys that contain the index position on disk.
     */
    private void initKeyMap()
    {
        keyHash = null;
        if (maxKeySize >= 0)
        {
            if (this.diskLimitType == DiskLimitType.SIZE)
            {
                keyHash = new LRUMapSizeLimited(maxKeySize);
            }
            else
            {
                keyHash = new LRUMapCountLimited(maxKeySize);
            }
            log.info("{0}: Set maxKeySize to: \"{1}\"", logCacheName, maxKeySize);
        }
        else
        {
            // If no max size, use a plain map for memory and processing
            // efficiency.
            keyHash = new HashMap<>();
            // keyHash = Collections.synchronizedMap( new HashMap() );
            log.info("{0}: Set maxKeySize to unlimited", logCacheName);
        }
    }

    /**
     * Tests emptiness (size == 0).
     *
     * @return Whether or not this is empty.
     * @since 3.1
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Gets the key set.
     * <p>
     *
     * @return key set.
     */
    public Set<K> keySet()
    {
        return this.keyHash.keySet();
    }

    /**
     * Loads the keys from the .key file. The keys are stored individually on
     * disk. They are added one by one to an LRUMap..
     */
    protected void loadKeys()
    {
        log.info("{0}: Loading keys for {1}", () -> logCacheName, () -> keyFile.toString());

        try
        {
            // create a key map to use.
            initKeyMap();

            final HashMap<K, int[]> keys = new HashMap<>();

            synchronized (keyFile)
            {
                final InputStream fis = Files.newInputStream(keyFile.toPath());
                final BufferedInputStream bis = new BufferedInputStream(fis, 65536);

                try (ObjectInputStream ois = new ObjectInputStreamClassLoaderAware(bis, null))
                {
                    while (true)
                    {
                        @SuppressWarnings("unchecked")
                        final
                        // Need to cast from Object
                        BlockDiskElementDescriptor<K> descriptor = (BlockDiskElementDescriptor<K>) ois.readObject();
                        if (descriptor != null)
                        {
                            keys.put(descriptor.getKey(), descriptor.getBlocks());
                        }
                    }
                }
                catch (final EOFException eof)
                {
                    // nothing
                }
            }

            if (!keys.isEmpty())
            {
                keyHash.putAll(keys);

                log.debug("{0}: Found {1} in keys file.", logCacheName, keys.size());
                log.info("{0}: Loaded keys from [{1}], key count: {2}; up to {3} will be available.",
                        () -> logCacheName, () -> fileName, () -> keyHash.size(),
                        () -> maxKeySize);
            }
        }
        catch (final Exception e)
        {
            log.error("{0}: Problem loading keys for file {1}", logCacheName, fileName, e);
        }
    }

    /**
     * Puts a int[] in the keyStore.
     * <p>
     *
     * @param key
     * @param value
     */
    public void put(final K key, final int[] value)
    {
        this.keyHash.put(key, value);
    }

    /**
     * Remove by key.
     * <p>
     *
     * @param key
     * @return BlockDiskElementDescriptor if it was present, else null
     */
    public int[] remove(final K key)
    {
        return this.keyHash.remove(key);
    }

    /**
     * Resets the file and creates a new key map.
     */
    protected void reset()
    {
        synchronized (keyFile)
        {
            clearMemoryMap();
            saveKeys();
        }
    }

    /**
     * Saves key file to disk. This gets the LRUMap entry set and write the
     * entries out one by one after putting them in a wrapper.
     */
    protected void saveKeys()
    {
        try
        {
            final ElapsedTimer timer = new ElapsedTimer();
            final int numKeys = keyHash.size();
            log.info("{0}: Saving keys to [{1}], key count [{2}]", () -> logCacheName,
                    () -> this.keyFile.getAbsolutePath(), () -> numKeys);

            synchronized (keyFile)
            {
                final OutputStream fos = Files.newOutputStream(keyFile.toPath());
                final BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);

                try (ObjectOutputStream oos = new ObjectOutputStream(bos))
                {
                    if (!verify())
                    {
                        throw new IOException("Inconsistent key file");
                    }
                    // don't need to synchronize, since the underlying
                    // collection makes a copy
                    for (final Map.Entry<K, int[]> entry : keyHash.entrySet())
                    {
                        final BlockDiskElementDescriptor<K> descriptor = new BlockDiskElementDescriptor<>();
                        descriptor.setKey(entry.getKey());
                        descriptor.setBlocks(entry.getValue());
                        // stream these out in the loop.
                        oos.writeUnshared(descriptor);
                    }
                }
            }

            log.info("{0}: Finished saving keys. It took {1} to store {2} keys. Key file length [{3}]",
                    () -> logCacheName, () -> timer.getElapsedTimeString(), () -> numKeys,
                    () -> keyFile.length());
        }
        catch (final IOException e)
        {
            log.error("{0}: Problem storing keys.", logCacheName, e);
        }
    }

    /**
     * Gets the size of the key hash.
     * <p>
     *
     * @return the number of keys.
     */
    public int size()
    {
        return this.keyHash.size();
    }

    /**
     * Verify key store integrity
     *
     * @return true if key store is valid
     */
    private boolean verify()
    {
        final Map<Integer, Set<K>> blockAllocationMap = new TreeMap<>();
        for (final Entry<K, int[]> e : keyHash.entrySet())
        {
            for (final int block : e.getValue())
            {
                Set<K> keys = blockAllocationMap.get(block);
                if (keys == null)
                {
                    keys = new HashSet<>();
                    blockAllocationMap.put(block, keys);
                }
                else if (!log.isTraceEnabled())
                {
                    // keys are not null, and no debug - fail fast
                    return false;
                }
                keys.add(e.getKey());
            }
        }
        boolean ok = true;
        if (!log.isTraceEnabled()) {
            return ok;
        }
        for (final Entry<Integer, Set<K>> e : blockAllocationMap.entrySet())
        {
            log.trace("Block {0}: {1}", e.getKey(), e.getValue());
            if (e.getValue().size() > 1)
            {
                ok = false;
            }
        }
        return ok;
    }
}
