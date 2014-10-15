package org.apache.commons.jcs.yajcache.util.concurrent.locks;

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
import org.apache.commons.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.commons.jcs.yajcache.lang.ref.KeyedWeakReference;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Factory for key specific ReadWriteLock.
 * Unused locks are automatically garbage collected.
 *
 * @author Hanson Char
 */
@CopyRightApache
public class KeyedReadWriteLock<K> implements IKeyedReadWriteLock<K> {
    private static final boolean debug = true;

    private final @NonNullable
            ConcurrentMap<K, KeyedWeakReference<K,ReadWriteLock>> rwlMap =
            new ConcurrentHashMap<K, KeyedWeakReference<K,ReadWriteLock>>();
    private final @NonNullable Class<? extends ReadWriteLock> rwlClass;
    private final @NonNullable ReferenceQueue<ReadWriteLock> refQ =
            new ReferenceQueue<ReadWriteLock>();
    private final @NonNullable KeyedRefCollector<K> collector =
            new KeyedRefCollector<K>(refQ, rwlMap);

    private final AtomicInteger countRWLockCreate = new AtomicInteger(0);
    private final AtomicInteger countReadLock = new AtomicInteger(0);
    private final AtomicInteger countWriteLock = new AtomicInteger(0);

    private final AtomicInteger countKeyHit = new AtomicInteger(0);
    private final AtomicInteger countLockRefEmpty = new AtomicInteger(0);
    private final AtomicInteger countLockNew = new AtomicInteger(0);
    private final AtomicInteger countLockExist = new AtomicInteger(0);

    public KeyedReadWriteLock() {
        this.rwlClass = ReentrantReadWriteLock.class;
    }
    public KeyedReadWriteLock(
            @NonNullable Class<? extends ReadWriteLock> rwlClass)
    {
        this.rwlClass = rwlClass;
    }
    public @NonNullable Lock readLock(@NonNullable final K key) {
        if (debug)
            this.countReadLock.incrementAndGet();
        return this.readWriteLock(key).readLock();
    }
    public @NonNullable Lock writeLock(@NonNullable final K key) {
        if (debug)
            this.countWriteLock.incrementAndGet();
        return this.readWriteLock(key).writeLock();
    }
    private @NonNullable ReadWriteLock readWriteLock(@NonNullable final K key) {
        this.collector.run();
        ReadWriteLock ret = this.getExistingLock(key);
        return ret == null ? this.tryNewLock(key) : ret;
    }
    /**
     * Returns an existing RWLock for the specified key, or null if not found.
     */
    private ReadWriteLock getExistingLock(@NonNullable final K key) {
        KeyedWeakReference<K,ReadWriteLock> ref = this.rwlMap.get(key);
        return ref == null ? null : this.toLock(ref);
    }
    /**
     * Returns the RWLock of the given RWLock weak reference, cleaning up
     * stale reference as necessary should the RWLock be garbage collected.
     */
    private ReadWriteLock toLock(
            @NonNullable KeyedWeakReference<K,ReadWriteLock> ref)
    {
        // existing lock may exist
        if (debug)
            this.countKeyHit.incrementAndGet();
        ReadWriteLock prev = ref.get();

        if (prev != null) {
            // existing lock
            if (debug)
                this.countLockExist.incrementAndGet();
            return prev;
        }
        if (debug)
            this.countLockRefEmpty.incrementAndGet();
        // stale reference; doesn't matter if fail
        this.rwlMap.remove(ref.getKey(),  ref);
        return null;
    }
    /** Instantiate a new RW lock. */
    private @NonNullable ReadWriteLock createRWLock() {
        if (debug)
            this.countRWLockCreate.incrementAndGet();
        try {
            return rwlClass.newInstance();
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Returns either a new lock created by the executing thread;
     * or an existing lock created by another thread due to data race.
     */
    private @NonNullable ReadWriteLock tryNewLock(@NonNullable final K key)
    {
        final ReadWriteLock newLock = this.createRWLock();
        final KeyedWeakReference<K,ReadWriteLock> newLockRef =
                new KeyedWeakReference<K,ReadWriteLock>(key, newLock, refQ);
        do {
            KeyedWeakReference<K,ReadWriteLock> ref =
                    this.rwlMap.putIfAbsent(key, newLockRef);
            if (ref == null) {
                // successfully deposited the new lock.
                if (debug)
                    this.countLockNew.incrementAndGet();
                return newLock;
            }
            ReadWriteLock rwl = this.toLock(ref);

            if (rwl != null) {
                return rwl;
            }
        } while(true);
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("\n").append("countReadLock", this.countReadLock)
            .append("\n").append("countWriteLock", this.countWriteLock)
            .append("\n").append("countRWLockCreate", this.countRWLockCreate)
            .append("\n").append("countKeyHit", this.countKeyHit)
            .append("\n").append("countLockExist", this.countLockExist)
            .append("\n").append("countLockNew", this.countLockNew)
            .append("\n").append("countLockRefEmpty", this.countLockRefEmpty)
            .append("\n").append("collector", this.collector)
            .toString();
    }
}
