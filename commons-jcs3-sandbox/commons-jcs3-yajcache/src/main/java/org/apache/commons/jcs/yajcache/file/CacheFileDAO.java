package org.apache.commons.jcs.yajcache.file;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs.yajcache.lang.annotation.NonNullable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cache File Data Access Object.
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheFileDAO {
    inst;

    private final AtomicInteger countWriteIOException = new AtomicInteger();
    private final AtomicInteger countWriteCloseException = new AtomicInteger();
    private final AtomicInteger countReadIOException = new AtomicInteger();
    private final AtomicInteger countReadCloseException = new AtomicInteger();
    private final AtomicInteger countCorruptMinLength = new AtomicInteger();
    private final AtomicInteger countCorruptLength = new AtomicInteger();
    private final AtomicInteger countCorruptInvalid = new AtomicInteger();

    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Writes the specified cache item into the file system.
     *
     * @return true if successful; false otherwise.
     */
    public boolean writeCacheItem(
            @NonNullable final String cacheName, @NonNullable final CacheFileContentType type,
            @NonNullable final String key, @NonNullable final byte[] val)
    {
        final File file = CacheFileUtils.inst.getCacheFile(cacheName, key);
        RandomAccessFile raf = null;
        try {
            file.delete();
            file.createNewFile();
            raf = new RandomAccessFile(file, "rw");
            CacheFileContent.getInstance(type, val).write(raf);
            return true;
        } catch (final IOException ex) {
            countWriteIOException.incrementAndGet();
            log.error("", ex);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (final Exception ex) {
                    countWriteCloseException.incrementAndGet();
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
     * or null if it doesn't exist;
     * or CacheFileContent.CORRUPTED if file is corrupted.
     */
    public CacheFileContent readCacheItem(@NonNullable final String cacheName, @NonNullable final String key)
    {
        final File file = CacheFileUtils.inst.getCacheFile(cacheName, key);

        if (!file.exists()) {
            return null;
        }
        final long fileSize = file.length();

        if (fileSize <= CacheFileContent.MIN_FILE_LENGTH) {
            countCorruptMinLength.incrementAndGet();
            log.warn("Corrupted file which failed the minimum length condition for cacheName="
                    + cacheName + " key=" + key);
            return CacheFileContent.CORRUPTED;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            final CacheFileContent cfc = CacheFileContent.getInstance(raf);

            if (cfc.isValid()) {
                final int contentLength = (int)fileSize - CacheFileContent.MIN_FILE_LENGTH;

                if (contentLength != cfc.getContentLength()) {
                    countCorruptLength.incrementAndGet();
                    log.warn("Corrupted file with unexpected content length for cacheName="
                            + cacheName + " key=" + key);
                    return CacheFileContent.CORRUPTED;
                }
            }
            else
            {
                countCorruptInvalid.incrementAndGet();
                log.warn("Corrupted file for cacheName=" + cacheName
                        + " key=" + key);
                return CacheFileContent.CORRUPTED;
            }
            return cfc;
        } catch (final IOException | org.apache.commons.lang3.SerializationException ex) {
            countReadIOException.incrementAndGet();
            log.warn(ex.getClass().getName(), ex);
       } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (final Exception ex) {
                    countReadCloseException.incrementAndGet();
                    log.error("", ex);
                }
            }
        }
        return CacheFileContent.CORRUPTED;
    }
    /**
     * Removes the specified cache item from the file system.
     *
     * @return true if successful; false otherwise.
     */
    public boolean removeCacheItem(@NonNullable final String cacheName, @NonNullable final String key)
    {
        final File file = CacheFileUtils.inst.getCacheFile(cacheName, key);
        return file.delete();
    }

    @Override
    public @NonNullable String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
