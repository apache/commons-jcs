
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
package org.apache.jcs.yajcache.soft;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.yajcache.annotate.*;
/**
 * Collects and clears stale cache entries implemented using Soft References.
 *
 * @author Hanson Char
 */
@CopyRightApache
class KeyedSoftRefCollector<V> implements Runnable {
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private volatile int count;
    private final @NonNullable ReferenceQueue<V> q;
    private final @NonNullable Map<String, KeyedSoftRef<V>> map;
    /** Creates a new instance of ReferenceQProcessor */
    KeyedSoftRefCollector(
            @NonNullable ReferenceQueue<V> q, @NonNullable Map<String, KeyedSoftRef<V>> map) 
    {
        this.q = q;
        this.map = map;
    }
    /**
     * Removes stale entries from the cache map collected by GC.
     * Thread safetyness provided by ReferenceQueue.
     */
//    @SuppressWarnings("unchecked")
    public void run() {
        Reference<? extends V> r;
        
        while ((r = this.q.poll()) != null) {
            KeyedSoftRef ksr = (KeyedSoftRef)r;
            String key = ksr.getKey();
            if (debug)
                log.debug("Remove stale entry with key=" + key);
            SoftRefCacheCleaner.inst.cleanupKey(map, key);
            // referent should have been cleared.  Defensively clear it again.
            ksr.clear();
            count++;
        }        
    }
    public int getCount() {
        return count;
    }
}
