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

package org.apache.jcs.yajcache.lang.ref;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * @author Hanson Char
 */
@CopyRightApache
public class KeyedRefCollector<K> implements Runnable {
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue q;
    private final @NonNullable ConcurrentMap<K, ? extends IKey<K>> synMap;
    private final AtomicInteger count = new AtomicInteger(0);

    public KeyedRefCollector(
            @NonNullable ReferenceQueue<?> q, 
            @NonNullable ConcurrentMap<K, ? extends IKey<K>> synMap)
    {
        this.q = q;
        this.synMap = synMap;
    }
    public void run() {
        Reference ref;
        
        while ((ref = this.q.poll()) != null) {
            IKey keyedRef = (IKey)ref;
            // remove unused lock;  may fail but that's fine.
            synMap.remove(keyedRef.getKey(), ref);
            // referent should have been cleared by GC.
            this.count.incrementAndGet();
        }        
    }
    public int getCount() {
        return this.count.intValue();
    }
}
