
/*
 * Copyright 2005 The Apache Software Foundation.
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
import org.apache.jcs.yajcache.config.YajCacheConfig;
import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * Cache File Utilities.
 *
 * @author Hanson Char
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
    public boolean mkCacheDirs(@NonNullable String cacheName) {
        File dir = this.getCacheDir(cacheName);
        return dir.mkdirs();
    }
    /**
     * Removes the file directory for the specified cache,
     * including all files under the directory.
     */
    boolean rmCacheDir(@NonNullable String cacheName) {
        File dir = this.getCacheDir(cacheName);
        
        if (!dir.exists())
            return true;
        for (File f : dir.listFiles()) {
            f.delete();
        }
        return dir.delete();
    }
    /**
     * Returns the file directory for the specified cache.
     */
    @NonNullable File getCacheDir(@NonNullable String cacheName) {
        return new File(YajCacheConfig.inst.getCacheDir(), cacheName);
    }
    /**
     * Returns the cache file for the specified cache item.
     */
    @NonNullable File getCacheFile(
            @NonNullable String cacheName,
            @NonNullable String key) 
    {
        File dir = this.getCacheDir(cacheName);
        return new File(dir, key);
    }
}
