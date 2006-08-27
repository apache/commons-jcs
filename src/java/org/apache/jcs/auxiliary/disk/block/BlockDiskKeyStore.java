package org.apache.jcs.auxiliary.disk.block;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache License, Version
 * 2.0 (the "License") you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.LRUMapJCS;
import org.apache.jcs.utils.timing.ElapsedTimer;

/**
 * This is responsible for storing the keys.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskKeyStore
{
    private static final Log log = LogFactory.getLog( BlockDiskCache.class );

    private BlockDiskCacheAttributes blockDiskCacheAttributes;

    private Map keyHash;

    private File keyFile;

    private final String logCacheName;

    private String fileName;

    private int maxKeySize;

    private BlockDiskCache blockDiskCache;

    private File rootDirectory;

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
            log.info( logCacheName + "Cache file root directory: " + rootDirName );
        }

        this.keyFile = new File( rootDirectory, fileName + ".key" );

        if ( keyFile.length() > 0 )
        {
            loadKeys();
            // TODO verify somehow
        }
        else
        {
            initKeyMap();
        }
    }

    /**
     * Saves key file to disk. This converts the LRUMap to a HashMap for deserialzation.
     */
    protected void saveKeys()
    {
        try
        {
            ElapsedTimer timer = new ElapsedTimer();
            int numKeys = keyHash.size();
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Saving keys to: " + fileName + ", key count: " + numKeys );
            }

            keyFile.delete();

            HashMap keys = new HashMap();
            keys.putAll( keyHash );

            if ( keys.size() > 0 )
            {
                FileOutputStream fos = new FileOutputStream( keyFile );
                BufferedOutputStream bos = new BufferedOutputStream( fos );
                ObjectOutputStream oos = new ObjectOutputStream( bos );
                try
                {
                    oos.writeObject( keys );
                    oos.flush();
                }
                finally
                {
                    oos.close();
                }
            }

            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Finished saving keys. It took " + timer.getElapsedTimeString() + " to store "
                    + numKeys + " keys." );
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
     * Loads the keys from the .key file. The keys are stored in a HashMap on disk. This is
     * converted into a LRUMap.
     * <p>
     * @throws InterruptedException
     */
    protected void loadKeys()
        throws InterruptedException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Loading keys for " + keyFile.toString() );
        }

        try
        {
            // create a key map to use.
            initKeyMap();

            HashMap keys = null;

            FileInputStream fis = new FileInputStream( keyFile );
            BufferedInputStream bis = new BufferedInputStream( fis );
            ObjectInputStream ois = new ObjectInputStream( bis );
            try
            {
                keys = (HashMap) ois.readObject();// 0, keyFile.length() );
            }
            finally
            {
                ois.close();
            }

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
     * gets the object fot he key.
     * <p>
     * @param key
     * @return Object
     */
    public BlockDiskElementDescriptor get( Object key )
    {
        return (BlockDiskElementDescriptor) this.keyHash.get( key );
    }

    /**
     * Puts a BlockDiskElementDescriptor in the keyStore.
     * <p>
     * @param key
     * @param value
     */
    public void put( Object key, BlockDiskElementDescriptor value )
    {
        this.keyHash.put( key, value );
    }

    /**
     * Remove by key.
     * <p>
     * @param key
     * @return BlockDiskElementDescriptor if it was present, else null
     */
    public BlockDiskElementDescriptor remove( Object key )
    {
        return (BlockDiskElementDescriptor) this.keyHash.remove( key );
    }

    /**
     * Class for recylcing and lru. This implments the LRU overflow callback, so we can mark the
     * blocks as free.
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
         */
        protected void processRemovedLRU( Object key, Object value )
        {
            blockDiskCache.freeBlocks( ( (BlockDiskElementDescriptor) value ).getBlocks() );
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Removing key: [" + key + "] from key store." );
                log.debug( logCacheName + "Key store size: [" + this.size() + "]." );
            }
        }
    }
}
