package org.apache.jcs.auxiliary.disk.block;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.LRUMapJCS;
import org.apache.jcs.utils.timing.ElapsedTimer;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This is responsible for storing the keys.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskKeyStore
{
    /** The logger */
    private static final Log log = LogFactory.getLog( BlockDiskKeyStore.class );

    /** Attributes governing the behavior of the block disk cache. */
    private BlockDiskCacheAttributes blockDiskCacheAttributes;

    /** The key to block map */
    private Map keyHash;

    /** The file where we persist the keys */
    private File keyFile;

    /** The name to prefix log messages with. */
    private final String logCacheName;

    /** Name of the file where we persist the keys */
    private String fileName;

    /** The maximum number of keys to store in memory */
    private int maxKeySize;

    /** we need this so we can communicate free blocks to the data store when keys fall off the LRU */
    private BlockDiskCache blockDiskCache;

    /** The root directory in which the keyFile lives */
    private File rootDirectory;

    /**
     * The background key persister, one for all regions.
     */
    private static ClockDaemon persistenceDaemon;

    /**
     * Set the configuration options.
     * <p>
     * @param cacheAttributes
     * @param blockDiskCache used for freeing
     * @throws Exception
     */
    public BlockDiskKeyStore( BlockDiskCacheAttributes cacheAttributes, BlockDiskCache blockDiskCache )
        throws Exception
    {
        this.blockDiskCacheAttributes = cacheAttributes;
        this.logCacheName = "Region [" + this.blockDiskCacheAttributes.getCacheName() + "] ";
        this.fileName = this.blockDiskCacheAttributes.getCacheName();
        this.maxKeySize = cacheAttributes.getMaxKeySize();
        this.blockDiskCache = blockDiskCache;

        String rootDirName = cacheAttributes.getDiskPath();
        this.rootDirectory = new File( rootDirName );
        this.rootDirectory.mkdirs();

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Cache file root directory [" + rootDirName + "]" );
        }

        this.keyFile = new File( rootDirectory, fileName + ".key" );

        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Key File [" + this.keyFile.getAbsolutePath() + "]" );
        }

        if ( keyFile.length() > 0 )
        {
            loadKeys();
            // TODO verify somehow
        }
        else
        {
            initKeyMap();
        }

        // add this region to the persistence thread.
        // TODO we might need to stagger this a bit.
        if ( this.blockDiskCacheAttributes.getKeyPersistenceIntervalSeconds() > 0 )
        {
            if ( persistenceDaemon == null )
            {
                persistenceDaemon = new ClockDaemon();
                persistenceDaemon.setThreadFactory( new MyThreadFactory() );
            }
            persistenceDaemon
                .executePeriodically( this.blockDiskCacheAttributes.getKeyPersistenceIntervalSeconds() * 1000,
                                      new Runnable()
                                      {
                                          public void run()
                                          {
                                              saveKeys();
                                          }
                                      }, false );
        }
    }

    /**
     * Saves key file to disk. This gets the LRUMap entry set and write the entries out one by one
     * after putting them in a wrapper.
     */
    protected void saveKeys()
    {
        try
        {
            ElapsedTimer timer = new ElapsedTimer();
            int numKeys = keyHash.size();
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Saving keys to [" + this.keyFile.getAbsolutePath() + "], key count ["
                    + numKeys + "]" );
            }

            keyFile.delete();

            keyFile = new File( rootDirectory, fileName + ".key" );
            FileOutputStream fos = new FileOutputStream( keyFile );
            BufferedOutputStream bos = new BufferedOutputStream( fos, 1024 );
            ObjectOutputStream oos = new ObjectOutputStream( bos );
            try
            {
                // don't need to synchronize, since the underlying collection makes a copy
                Iterator keyIt = keyHash.entrySet().iterator();
                while ( keyIt.hasNext() )
                {
                    Map.Entry entry = (Map.Entry) keyIt.next();
                    BlockDiskElementDescriptor descriptor = new BlockDiskElementDescriptor();
                    descriptor.setKey( (Serializable) entry.getKey() );
                    descriptor.setBlocks( (int[]) entry.getValue() );
                    // stream these out in the loop.
                    oos.writeObject( descriptor );
                }
            }
            finally
            {
                oos.flush();
                oos.close();
            }

            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Finished saving keys. It took " + timer.getElapsedTimeString() + " to store "
                    + numKeys + " keys.  Key file length [" + keyFile.length() + "]" );
            }
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Problem storing keys.", e );
        }
    }

    /**
     * Resets the file and creates a new key map.
     */
    protected void reset()
    {
        File keyFileTemp = new File( this.rootDirectory, fileName + ".key" );
        keyFileTemp.delete();

        keyFile = new File( this.rootDirectory, fileName + ".key" );

        initKeyMap();
    }

    /**
     * This is mainly used for testing. It leave the disk in tact, and just clears memory.
     */
    protected void clearMemoryMap()
    {
        this.keyHash.clear();
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
     * Loads the keys from the .key file. The keys are stored individually on disk. They are added
     * one by one to an LRUMap..
     * <p>
     * @throws InterruptedException
     */
    protected void loadKeys()
        throws InterruptedException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Loading keys for " + keyFile.toString() );
        }

        try
        {
            // create a key map to use.
            initKeyMap();

            HashMap keys = new HashMap();

            FileInputStream fis = new FileInputStream( keyFile );
            BufferedInputStream bis = new BufferedInputStream( fis );
            ObjectInputStream ois = new ObjectInputStream( bis );
            try
            {
                while ( true )
                {
                    BlockDiskElementDescriptor descriptor = (BlockDiskElementDescriptor) ois.readObject();
                    if ( descriptor != null )
                    {
                        keys.put( descriptor.getKey(), descriptor.getBlocks() );
                    }
                }
            }
            catch ( EOFException eof )
            {
                // nothing
            }
            finally
            {
                ois.close();
            }

            if ( !keys.isEmpty() )
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
        }
        catch ( Exception e )
        {
            log.error( logCacheName + "Problem loading keys for file " + fileName, e );
        }
    }

    /**
     * Gets the entry set.
     * <p>
     * @return entry set.
     */
    public Set entrySet()
    {
        return this.keyHash.entrySet();
    }

    /**
     * Gets the key set.
     * <p>
     * @return key set.
     */
    public Set keySet()
    {
        return this.keyHash.keySet();
    }

    /**
     * Gets the size of the key hash.
     * <p>
     * @return the number of keys.
     */
    public int size()
    {
        return this.keyHash.size();
    }

    /**
     * gets the object for the key.
     * <p>
     * @param key
     * @return Object
     */
    public int[] get( Object key )
    {
        return (int[]) this.keyHash.get( key );
    }

    /**
     * Puts a int[] in the keyStore.
     * <p>
     * @param key
     * @param value
     */
    public void put( Object key, int[] value )
    {
        this.keyHash.put( key, value );
    }

    /**
     * Remove by key.
     * <p>
     * @param key
     * @return BlockDiskElementDescriptor if it was present, else null
     */
    public int[] remove( Object key )
    {
        return (int[]) this.keyHash.remove( key );
    }

    /**
     * Class for recylcing and lru. This implments the LRU overflow callback, so we can mark the
     * blocks as free.
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
            blockDiskCache.freeBlocks( (int[]) value );
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Removing key: [" + key + "] from key store." );
                log.debug( logCacheName + "Key store size: [" + this.size() + "]." );
            }
        }
    }

    /**
     * Allows us to set the daemon status on the clockdaemon
     * @author aaronsm
     */
    class MyThreadFactory
        implements ThreadFactory
    {

        /**
         * Ensures that we create daemon threads.
         * <p>
         * (non-Javadoc)
         * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }
}
