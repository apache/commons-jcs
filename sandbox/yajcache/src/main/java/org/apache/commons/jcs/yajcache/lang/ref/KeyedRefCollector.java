package org.apache.commons.jcs.yajcache.lang.ref;

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

import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keyed Reference garbage collector which removes stale
 * Keyed {@link Reference} entries
 * from the given {@link ConcurrentMap} using the embedded keys.
 * The stale Keyed References are put into the given {@link ReferenceQueue}
 * by the JVM garbage collector.
 *
 * @author Hanson Char
 */
@CopyRightApache
public class KeyedRefCollector<K> implements Runnable {
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue q;
    private final @NonNullable ConcurrentMap<K, ? extends IKey<K>> synMap;
    private final AtomicInteger count = new AtomicInteger(0);
    /**
     * Constructs with a given reference queue and concurrent map.
     */
    public KeyedRefCollector(
            @NonNullable ReferenceQueue<?> q,
            @NonNullable ConcurrentMap<K, ? extends IKey<K>> synMap)
    {
        this.q = q;
        this.synMap = synMap;
    }
    /**
     * Executes one cycle of stale entries removal.
     */
    public void run() {
        Reference ref;

        while ((ref = this.q.poll()) != null) {
            IKey keyedRef = (IKey)ref;
            // remove unused lock;  may fail but that's fine.
            synMap.remove(keyedRef.getKey(), ref);
            // referent should have been cleared by GC.
            if (debug)
                this.count.incrementAndGet();
        }
    }
    public int getCount() {
        return this.count.intValue();
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
                .append("count", this.getCount())
                .toString();
    }
}
