
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

package org.apache.jcs.yajcache.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.yajcache.annotate.*;
import org.apache.commons.lang.builder.*;


/**
 * Cache File Data Access Object.
 *
 * @author Hanson Char
 */
@CopyRightApache
public enum CacheFileDAO {
    inst;

    private volatile int countWriteIOException;
    private volatile int countWriteCloseException;
    private volatile int countReadIOException;
    private volatile int countReadCloseException;
    private volatile int countCorruptMinLength;
    private volatile int countCorruptLength;
    private volatile int countCorruptInvalid;
    
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Writes the specified cache item into the file system.
     *
     * @return true if successful; false otherwise.
     */
    public boolean writeCacheItem(
            @NonNullable String cacheName, @NonNullable CacheFileContentType type, 
            @NonNullable String key, @NonNullable byte[] val)
    {
        File file = CacheFileUtils.inst.getCacheFile(cacheName, key);
        RandomAccessFile raf = null;
        try {
            file.delete();
            file.createNewFile();
            raf = new RandomAccessFile(file, "rw");
            CacheFileContent.getInstance(type, val).write(raf);
            return true;
        } catch(IOException ex) {
            countWriteIOException++;
            log.error("", ex);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch(Exception ex) {
                    countWriteCloseException++;
                    log.error("", ex);
                }
            }
        }
        return false;
    }
    /**
     * Reads the byte array of a specified cache item from the file system.
     * 
     * @return the byte array of a specified cache item from the file system;
     * or null if it can't.
     */
    public byte[] readCacheItem(@NonNullable String cacheName, @NonNullable String key) 
    {
        File file = CacheFileUtils.inst.getCacheFile(cacheName, key);
        
        if (!file.exists())
            return null;
        final long fileSize = file.length();
        
        if (fileSize <= CacheFileContent.MIN_FILE_LENGTH) {
            countCorruptMinLength++;
            log.warn("Corrupted file which failed the minimum length condition for cacheName=" 
                    + cacheName + " key=" + key);
            return null;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            CacheFileContent cfc = CacheFileContent.getInstance(raf);
            
            if (cfc.isValid()) {
                final int contentLength = (int)fileSize - CacheFileContent.MIN_FILE_LENGTH;
                
                if (contentLength != cfc.getContentLength()) {
                    countCorruptLength++;
                    log.warn("Corrupted file with unexpected content length for cacheName=" 
                            + cacheName + " key=" + key);
                    return null;
                }
            }
            else
            {
                countCorruptInvalid++;
                log.warn("Corrupted file for cacheName=" + cacheName 
                        + " key=" + key);
                return null;
            }
            return cfc.getContent();
        } catch(IOException ex) {
            countReadIOException++;
            log.warn("", ex);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch(Exception ex) {
                    countReadCloseException++;
                    log.error("", ex);
                }
            }
        }
        return null;
    }
    /**
     * Removes the specified cache item from the file system.
     *
     * @return true if successful; false otherwise.
     */
    public boolean removeCacheItem(@NonNullable String cacheName, @NonNullable String key)
    {
        File file = CacheFileUtils.inst.getCacheFile(cacheName, key);
        return file.delete();
    }

    @Override 
    public @NonNullable String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
