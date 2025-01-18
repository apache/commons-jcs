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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.yajcache.config.YajCacheConfig;
import org.apache.commons.jcs.yajcache.lang.annotation.*;

import java.io.File;

/**
 * Cache File Utilities.
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheFileUtils {
    inst;

    /**
     * Creates the directory for the specified cache,
     * including any necessary but nonexistent parent directories.
     * Note that if this operation fails it may have succeeded in
     * creating some of the necessary parent directories.
     *
     * @return true if succesfull; false otherwise.
     */
    public boolean mkCacheDirs(@NonNullable final String cacheName) {
        final File dir = getCacheDir(cacheName);
        return dir.mkdirs();
    }
    /**
     * Removes the file directory for the specified cache,
     * including all files under the directory.
     */
    public boolean rmCacheDir(@NonNullable final String cacheName) {
        final File dir = getCacheDir(cacheName);

        if (!dir.exists()) {
            return true;
        }
        for (final File f : dir.listFiles()) {
            f.delete();
        }
        return dir.delete();
    }
    public boolean isCacheDirEmpty(@NonNullable final String cacheName) {
        final File dir = getCacheDir(cacheName);

        if (!dir.exists()) {
            return true;
        }
        final String[] list = dir.list();
        return list == null || list.length == 0;
    }
    public int getCacheDirSize(@NonNullable final String cacheName) {
        final File dir = getCacheDir(cacheName);

        if (!dir.exists()) {
            return 0;
        }
        final String[] list = dir.list();
        return list == null ? 0 : list.length;
    }
    public String[] getCacheDirList(@NonNullable final String cacheName)
    {
        final File dir = getCacheDir(cacheName);

        if (!dir.exists()) {
            return null;
        }
        return dir.list();
    }
    /**
     * Returns the file directory for the specified cache.
     */
    @NonNullable File getCacheDir(@NonNullable final String cacheName) {
        return new File(YajCacheConfig.inst.getCacheDir(), cacheName);
    }
    /**
     * Returns the cache file for the specified cache item.
     */
    @NonNullable File getCacheFile(
            @NonNullable final String cacheName,
            @NonNullable final String key)
    {
        final File dir = getCacheDir(cacheName);
        return new File(dir, key);
    }
}
