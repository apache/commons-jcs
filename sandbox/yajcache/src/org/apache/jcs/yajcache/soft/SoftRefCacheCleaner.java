
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.lang.ref.KeyedSoftReference;
/**
 *
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
enum SoftRefCacheCleaner {
    inst;
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    
    private AtomicInteger countTryKeyClean = new AtomicInteger(0);
    private AtomicInteger countRemovedByOthers = new AtomicInteger(0);
    private AtomicInteger countKeyCleaned = new AtomicInteger(0);
    private AtomicInteger countDataRace = new AtomicInteger(0);
    private AtomicInteger countDataRaceAndRemovedByOthers = new AtomicInteger(0);
    private AtomicInteger countBye = new AtomicInteger(0);
    
    <V> void cleanupKey(@NonNullable Map<String, 
            KeyedSoftReference<String,V>> map, @NonNullable String key) 
    {
        V val = null;
        // already garbage collected.  So try to clean up the key.
        if (debug)
            log.debug("Try to clean up the key");
        this.countTryKeyClean.incrementAndGet();
        KeyedSoftReference<String,V> oldRef = map.remove(key);
        // If oldRef is null, the key has just been 
        // cleaned up by another thread.
        if (oldRef == null) {
            if (debug)
                log.debug("Key has just been removed by another thread.");
            this.countRemovedByOthers.incrementAndGet();
            return;
        }
        // Check for race condition.
        V oldVal = oldRef.get();

        if (val == oldVal) {
            // not considered a race condition
            oldRef.clear();
            if (debug)
                log.debug("Key removed and Soft Reference cleared.");
            this.countKeyCleaned.incrementAndGet();
            return;
        }
        // Race condition.
        do {
            if (debug)
                log.debug("Race condition occurred.  So put back the old stuff.");
            this.countDataRace.incrementAndGet();
            // race condition occurred
            // put back the old stuff
            val = oldVal;
            oldRef = map.put(key, oldRef);

            if (oldRef == null) {
                // key has just been cleaned up by another thread.
                if (debug)
                    log.debug("Key has just been removed by another thread.");
                this.countDataRaceAndRemovedByOthers.incrementAndGet();
                return;  
            }
            oldVal = oldRef.get();
        } while (oldVal != val);

        if (debug)
            log.debug("Bye.");
        this.countBye.incrementAndGet();
        return;
    }

    public int getCountTryKeyClean() {
        return countTryKeyClean.intValue();
    }

    public int getCountRemovedByOthers() {
        return countRemovedByOthers.intValue();
    }

    public int getCountKeyCleaned() {
        return countKeyCleaned.intValue();
    }

    public int getCountDataRace() {
        return countDataRace.intValue();
    }
    public int getCountDataRaceAndRemovedByOthers() {
        return countDataRaceAndRemovedByOthers.intValue();
    }

    public int getCountBye() {
        return countBye.intValue();
    }
    
    @Override public @NonNullable String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
