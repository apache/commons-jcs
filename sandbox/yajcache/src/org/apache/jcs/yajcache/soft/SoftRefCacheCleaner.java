
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
    
    private volatile int countTryKeyClean;
    private volatile int countRemovedByOthers;
    private volatile int countKeyCleaned;
    private volatile int countDataRace;
    private volatile int countDataRaceAndRemovedByOthers;
    private volatile int countBye;
    
    <V> void cleanupKey(@NonNullable Map<String, KeyedSoftReference<V>> map, @NonNullable String key) 
    {
        V val = null;
        // already garbage collected.  So try to clean up the key.
        if (debug)
            log.debug("Try to clean up the key");
        this.countTryKeyClean++;
        KeyedSoftReference<V> oldRef = map.remove(key);
        // If oldRef is null, the key has just been 
        // cleaned up by another thread.
        if (oldRef == null) {
            if (debug)
                log.debug("Key has just been removed by another thread.");
            this.countRemovedByOthers++;
            return;
        }
        // Check for race condition.
        V oldVal = oldRef.get();

        if (val == oldVal) {
            // not considered a race condition
            oldRef.clear();
            if (debug)
                log.debug("Key removed and Soft Reference cleared.");
            this.countKeyCleaned++;
            return;
        }
        // Race condition.
        do {
            if (debug)
                log.debug("Race condition occurred.  So put back the old stuff.");
            this.countDataRace++;
            // race condition occurred
            // put back the old stuff
            val = oldVal;
            oldRef = map.put(key, oldRef);

            if (oldRef == null) {
                // key has just been cleaned up by another thread.
                if (debug)
                    log.debug("Key has just been removed by another thread.");
                this.countDataRaceAndRemovedByOthers++;
                return;  
            }
            oldVal = oldRef.get();
        } while (oldVal != val);

        if (debug)
            log.debug("Bye.");
        this.countBye++;
        return;
    }

    public int getCountTryKeyClean() {
        return countTryKeyClean;
    }

    public int getCountRemovedByOthers() {
        return countRemovedByOthers;
    }

    public int getCountKeyCleaned() {
        return countKeyCleaned;
    }

    public int getCountDataRace() {
        return countDataRace;
    }
    public int getCountDataRaceAndRemovedByOthers() {
        return countDataRaceAndRemovedByOthers;
    }

    public int getCountBye() {
        return countBye;
    }
    
    @Override public @NonNullable String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
