/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jcs.auxiliary.disk.file;
/*
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogFactory;

/**
 * This disk cache writes each item to a separate file. This is for regions with very few items,
 * perhaps big ones.
 * <p>
 * This is a fairly simple implementation. All the disk writing is handled right here. It's not
 * clear that anything more complicated is needed.
 */
public class FileDiskCache<K, V>
    extends AbstractDiskCache<K, V>
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( FileDiskCache.class );

    /** The name to prefix all log messages with. */
    private final String logCacheName;

    /** The config values. */
    private final FileDiskCacheAttributes diskFileCacheAttributes;

    /** The directory where the files are stored */
    private File directory;

    /**
     * Constructor for the DiskCache object.
     *
     * @param cacheAttributes
     */
    public FileDiskCache( final FileDiskCacheAttributes cacheAttributes )
    {
        this( cacheAttributes, null );
    }

    /**
     * Constructor for the DiskCache object. Will be marked alive if the directory cannot be
     * created.
     *
     * @param cattr
     * @param elementSerializer used if supplied, the super's super will not set a null
     */
    public FileDiskCache( final FileDiskCacheAttributes cattr, final IElementSerializer elementSerializer )
    {
        super( cattr );
        setElementSerializer( elementSerializer );
        this.diskFileCacheAttributes = cattr;
        this.logCacheName = "Region [" + getCacheName() + "] ";
        setAlive(initializeFileSystem( cattr ));
    }

    /**
     * Tries to create the root directory if it does not already exist.
     *
     * @param cattr
     * @return does the directory exist.
     */
    private boolean initializeFileSystem( final FileDiskCacheAttributes cattr )
    {
        // TODO, we might need to make this configurable
        this.setDirectory( new File( cattr.getDiskPath(), cattr.getCacheName() ) );
        final boolean createdDirectories = getDirectory().mkdirs();
        if ( log.isInfoEnabled() )
        {
            log.info( logCacheName + "Cache file root directory: " + getDirectory() );
            log.info( logCacheName + "Created root directory: " + createdDirectories );
        }

        // TODO consider throwing.
        final boolean exists = getDirectory().exists();
        if ( !exists )
        {
            log.error( "Could not initialize File Disk Cache.  The root directory does not exist." );
        }
        return exists;
    }

    /**
     * Creates the file for a key. Filenames and keys can be passed into this method. It must be
     * idempotent.
     * <p>
     * Protected for testing.
     *
     * @param key
     * @return the file for the key
     */
    protected <KK> File file( final KK key )
    {
        final StringBuilder fileNameBuffer = new StringBuilder();

        // add key as file name in a file system safe way
        final String keys = key.toString();
        final int l = keys.length();
        for ( int i = 0; i < l; i++ )
        {
            char c = keys.charAt( i );
            if ( !Character.isLetterOrDigit( c ) )
            {
                c = '_';
            }
            fileNameBuffer.append( c );
        }
        final String fileName = fileNameBuffer.toString();

        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Creating file for name: [" + fileName + "] based on key: [" + key + "]" );
        }

        return new File( getDirectory().getAbsolutePath(), fileName );
    }

    /**
     * Return the keys in this cache.
     *
     * @see org.apache.commons.jcs.auxiliary.disk.AbstractDiskCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @return dir.list().length
     */
    @Override
    public int getSize()
    {
        if ( getDirectory().exists() )
        {
            return getDirectory().list().length;
        }
        return 0;
    }

    /**
     * @return AuxiliaryCacheAttributes
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return diskFileCacheAttributes;
    }

    /**
     * @return String the path to the directory
     */
    @Override
    protected String getDiskLocation()
    {
        return getDirectory().getAbsolutePath();
    }

    /**
     * Sets alive to false.
     *
     * @throws IOException
     */
    @Override
    protected synchronized void processDispose()
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( getCacheName(), "none", ICacheEventLogger.DISPOSE_EVENT );
        try
        {
            if ( !isAlive() )
            {
                log.error( logCacheName + "Not alive and dispose was called, directgory: " + getDirectory() );
                return;
            }

            // Prevents any interaction with the cache while we're shutting down.
            setAlive(false);

            // TODO consider giving up the handle on the directory.
            if ( log.isInfoEnabled() )
            {
                log.info( logCacheName + "Shutdown complete." );
            }
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Looks for a file matching the key. If it exists, reads the file.
     *
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    protected ICacheElement<K, V> processGet( final K key )
        throws IOException
    {
        final File file = file( key );

        if ( !file.exists() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "File does not exist.  Returning null from Get." + file );
            }
            return null;
        }

        ICacheElement<K, V> element = null;

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );

            final long length = file.length();
            // Create the byte array to hold the data
            final byte[] bytes = new byte[(int) length];

            int offset = 0;
            int numRead = 0;
            while ( offset < bytes.length && ( numRead = fis.read( bytes, offset, bytes.length - offset ) ) >= 0 )
            {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if ( offset < bytes.length )
            {
                throw new IOException( "Could not completely read file " + file.getName() );
            }

            element = getElementSerializer().deSerialize( bytes, null );

            // test that the retrieved object has equal key
            if ( element != null && !key.equals( element.getKey() ) )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( logCacheName + "key: [" + key + "] point to cached object with key: [" + element.getKey()
                        + "]" );
                }
                element = null;
            }
        }
        catch ( IOException | ClassNotFoundException e )
        {
            log.error( logCacheName + "Failure getting element, key: [" + key + "]", e );
        }
        finally
        {
            silentClose( fis );
        }

        // If this is true and we have a max file size, the Least Recently Used file will be removed.
        if ( element != null && diskFileCacheAttributes.isTouchOnGet() )
        {
            touchWithRetry( file );
        }
        return element;
    }

    /**
     * @param pattern
     * @return Map
     * @throws IOException
     */
    @Override
    protected Map<K, ICacheElement<K, V>> processGetMatching( final String pattern )
        throws IOException
    {
        // TODO get a list of file and return those with matching keys.
        // the problem will be to handle the underscores.
        return null;
    }

    /**
     * Removes the file.
     *
     * @param key
     * @return true if the item was removed
     * @throws IOException
     */
    @Override
    protected boolean processRemove( final K key )
        throws IOException
    {
        return _processRemove(key);
    }

    /**
     * Removes the file.
     *
     * @param key
     * @return true if the item was removed
     * @throws IOException
     */
    private <T> boolean _processRemove( final T key )
        throws IOException
    {
        final File file = file( key );
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Removing file " + file );
        }
        return deleteWithRetry( file );
    }

    /**
     * Remove all the files in the directory.
     * <p>
     * Assumes that this is the only region in the directory. We could add a region prefix to the
     * files and only delete those, but the region should create a directory.
     *
     * @throws IOException
     */
    @Override
    protected void processRemoveAll()
        throws IOException
    {
        final String[] fileNames = getDirectory().list();
        for (final String fileName : fileNames) {
            _processRemove( fileName );
        }
    }

    /**
     * We create a temp file with the new contents, remove the old if it exists, and then rename the
     * temp.
     *
     * @param element
     * @throws IOException
     */
    @Override
    protected void processUpdate( final ICacheElement<K, V> element )
        throws IOException
    {
        removeIfLimitIsSetAndReached();

        final File file = file( element.getKey() );

        File tmp = null;
        OutputStream os = null;
        try
        {
            final byte[] bytes = getElementSerializer().serialize( element );

            tmp = File.createTempFile( "JCS_DiskFileCache", null, getDirectory() );

            final FileOutputStream fos = new FileOutputStream( tmp );
            os = new BufferedOutputStream( fos );

            if ( bytes != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( logCacheName + "Wrote " + bytes.length + " bytes to file " + tmp );
                }
                os.write( bytes );
                os.close();
            }
            deleteWithRetry( file );
            final boolean result = tmp.renameTo( file );
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Renamed to: " + file + " Result: " + result);
            }
        }
        catch ( final IOException e )
        {
            log.error( logCacheName + "Failure updating element, key: [" + element.getKey() + "]", e );
        }
        finally
        {
            silentClose( os );
            if ( tmp != null && tmp.exists() )
            {
                deleteWithRetry( tmp );
            }
        }
    }

    /**
     * If a limit has been set and we have reached it, remove the least recently modified file.
     * <p>
     * We will probably need to touch the files. If we touch, the LRM file will be based on age
     * (i.e. FIFO). If we touch, it will be based on access time (i.e. LRU).
     */
    private void removeIfLimitIsSetAndReached()
    {
        // TODO we might want to synchronize this block.
        if ( diskFileCacheAttributes.getMaxNumberOfFiles() > 0 && getSize() >= diskFileCacheAttributes.getMaxNumberOfFiles() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( logCacheName + "Max reached, removing least recently modified" );
            }

            long oldestLastModified = System.currentTimeMillis();
            File theLeastRecentlyModified = null;
            final String[] fileNames = getDirectory().list();
            for (final String fileName : fileNames) {
                final File file = file( fileName );
                final long lastModified = file.lastModified();
                if ( lastModified < oldestLastModified )
                {
                    oldestLastModified = lastModified;
                    theLeastRecentlyModified = file;
                }
            }
            if ( theLeastRecentlyModified != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( logCacheName + "Least recently modified: " + theLeastRecentlyModified );
                }
                deleteWithRetry( theLeastRecentlyModified );
            }
        }
    }

    /**
     * Tries to delete a file. If it fails, it tries several more times, pausing a few ms. each
     * time.
     *
     * @param file
     * @return true if the file does not exist or if it was removed
     */
    private boolean deleteWithRetry( final File file )
    {
        boolean success = file.delete();

        // TODO: The following should be identical to success == false, but it isn't
        if ( file.exists() )
        {
            final int maxRetries = diskFileCacheAttributes.getMaxRetriesOnDelete();
            for ( int i = 0; i < maxRetries && !success; i++ )
            {
                SleepUtil.sleepAtLeast( 5 );
                success = file.delete();
            }
        }
        else
        {
            success = true;
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "deleteWithRetry.  success= " + success + " file: " + file );
        }
        return success;
    }

    /**
     * Tries to set the last access time to now.
     *
     * @param file to touch
     * @return was it successful
     */
    private boolean touchWithRetry( final File file )
    {
        boolean success = file.setLastModified( System.currentTimeMillis() );
        if ( !success )
        {
            final int maxRetries = diskFileCacheAttributes.getMaxRetriesOnTouch();
            if ( file.exists() )
            {
                for ( int i = 0; i < maxRetries && !success; i++ )
                {
                    SleepUtil.sleepAtLeast( 5 );
                    success = file.delete();
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( logCacheName + "Last modified, success: " + success );
        }
        return success;
    }

    /**
     * Closes a stream and swallows errors.
     *
     * @param s the stream
     */
    private void silentClose( final InputStream s )
    {
        if ( s != null )
        {
            try
            {
                s.close();
            }
            catch ( final IOException e )
            {
                log.error( logCacheName + "Failure closing stream", e );
            }
        }
    }

    /**
     * Closes a stream and swallows errors.
     *
     * @param s the stream
     */
    private void silentClose( final OutputStream s )
    {
        if ( s != null )
        {
            try
            {
                s.close();
            }
            catch ( final IOException e )
            {
                log.error( logCacheName + "Failure closing stream", e );
            }
        }
    }

    /**
     * @param directory the directory to set
     */
    protected void setDirectory( final File directory )
    {
        this.directory = directory;
    }

    /**
     * @return the directory
     */
    protected File getDirectory()
    {
        return directory;
    }
}
