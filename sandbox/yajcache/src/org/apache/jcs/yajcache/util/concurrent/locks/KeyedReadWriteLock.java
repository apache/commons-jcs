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

package org.apache.jcs.yajcache.util.concurrent.locks;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.jcs.yajcache.lang.ref.KeyedWeakReference;

import org.apache.jcs.yajcache.lang.annotation.*;


/**
 * Factory for key specific ReadWriteLock.  
 * Unused locks are automatically garbage collected.
 *
 * @author Hanson Char
 */
@CopyRightApache
public class KeyedReadWriteLock<K> implements IKeyedReadWriteLock<K> {
    private static final boolean debug = true;
    
    private final @NonNullable ConcurrentMap<K, KeyedWeakReference<K,ReadWriteLock>> rwlMap = 
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
    public KeyedReadWriteLock(Class<? extends ReadWriteLock> rwlClass) {
        this.rwlClass = rwlClass;
    }
    public Lock readLock(K key) {
        if (debug)
            this.countReadLock.incrementAndGet();
        return this.readWriteLock(key).readLock();
    }
    public Lock writeLock(K key) {
        if (debug)
            this.countWriteLock.incrementAndGet();
        return this.readWriteLock(key).writeLock();
    }
    private ReadWriteLock readWriteLock(K key) {
        this.collector.run();
        KeyedWeakReference<K,ReadWriteLock> prevRef = this.rwlMap.get(key);
        
        if (prevRef != null) {
            // existing lock may exist
            if (debug)
                this.countKeyHit.incrementAndGet();
            ReadWriteLock prev = prevRef.get();
            
            if (prev != null) {
                // existing lock
                if (debug)
                    this.countLockExist.incrementAndGet();
                return prev;
            }
            if (debug)
                this.countLockRefEmpty.incrementAndGet();
            // stale reference; doesn't matter if fail
            this.rwlMap.remove(key,  prevRef);
        }
        // Instantiate a new RW lock.
        ReadWriteLock newLock = null;
        try {
            newLock = rwlClass.newInstance();
            if (debug)
                this.countRWLockCreate.incrementAndGet();
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(InstantiationException ex) {
            throw new RuntimeException(ex);
        }
        return this.getLock(key, newLock);
    }
    private ReadWriteLock getLock(final K key, ReadWriteLock newLock) 
    {
        final KeyedWeakReference<K,ReadWriteLock> newLockRef = 
                new KeyedWeakReference<K,ReadWriteLock>(key, newLock, refQ);
        ReadWriteLock prev = null;
        KeyedWeakReference<K,ReadWriteLock> prevRef = null;
        do {
            prevRef = this.rwlMap.putIfAbsent(key, newLockRef);

            if (prevRef == null) {
                // succesfully deposited the new lock.
                if (debug)
                    this.countLockNew.incrementAndGet();
                return newLock;
            }
            // existing lock may exist
            prev = prevRef.get();
            
            if (prev != null) {
                // exist lock
                if (debug)
                    this.countLockExist.incrementAndGet();
                return prev;
            }
            // stale reference; doesn't matter if fail
            if (debug)
                this.countLockRefEmpty.incrementAndGet();
            this.rwlMap.remove(key,  prevRef);
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
