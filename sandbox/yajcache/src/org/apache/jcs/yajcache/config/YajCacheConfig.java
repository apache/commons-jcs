
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

package org.apache.jcs.yajcache.config;

import java.io.File;

import org.apache.jcs.yajcache.annotate.*;

/**
 * @author Hanson Char
 */
@CopyRightApache
@TODO("Optional configuration via XML config file")
public enum YajCacheConfig {
    inst;
    
    /** Root directory for cache overflow to file. */
    private @NonNullable File cacheDir = new File("/tmp/yajcache");
    
    YajCacheConfig() {
        this.cacheDir.mkdirs();
    }

    public @NonNullable File getCacheDir() {
        return cacheDir;
    }
    void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
        this.cacheDir.mkdirs();
    }
}
