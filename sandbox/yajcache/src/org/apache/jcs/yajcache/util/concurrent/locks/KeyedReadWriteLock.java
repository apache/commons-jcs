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

package org.apache.jcs.yajcache.util.concurrent.locks;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.jcs.yajcache.lang.ref.KeyedWeakReference;


/**
 * Factory for key specific ReadWriteLock.  
 * Unused locks are automatically garbage collected.
 *
 * @author Hanson Char
 */
public class KeyedReadWriteLock<K> implements IKeyedReadWriteLock<K> {
    private final ConcurrentMap<K, KeyedWeakReference<K,ReadWriteLock>> rwlMap = 
            new ConcurrentHashMap<K, KeyedWeakReference<K,ReadWriteLock>>();
    private final Class<? extends ReadWriteLock> rwlClass;
    private final ReferenceQueue<ReadWriteLock> refQ = 
            new ReferenceQueue<ReadWriteLock>();
    private final KeyedRefCollector<K> collector = 
            new KeyedRefCollector<K>(refQ, rwlMap);
    public KeyedReadWriteLock() {
        this.rwlClass = ReentrantReadWriteLock.class;
    }
    public KeyedReadWriteLock(Class<? extends ReadWriteLock> rwlClass) {
        this.rwlClass = rwlClass;
    }
    public Lock readLock(K key) {
        return this.readWriteLock(key).readLock();
    }
    public Lock writeLock(K key) {
        return this.readWriteLock(key).writeLock();
    }
    private ReadWriteLock readWriteLock(K key) {
        ReadWriteLock newLock = null;
        try {
            newLock = rwlClass.newInstance();
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(InstantiationException ex) {
            throw new RuntimeException(ex);
        }
        return this.getLock(key, newLock);
    }
    private ReadWriteLock getLock(final K key, ReadWriteLock newLock) 
    {
        this.collector.run();
        ReadWriteLock prev = null;
        KeyedWeakReference<K,ReadWriteLock> prevRef = this.rwlMap.get(key);
        
        if (prevRef != null) {
            // existing lock may exist
            prev = prevRef.get();
            
            if (prev != null) {
                // existing lock
                return prev;
            }
            // stale reference; doesn't matter if fail
            this.rwlMap.remove(key,  prevRef);
        }
        final KeyedWeakReference<K,ReadWriteLock> newLockRef = 
                new KeyedWeakReference<K,ReadWriteLock>(key, newLock, refQ);
        do {
            prevRef = this.rwlMap.putIfAbsent(key, newLockRef);

            if (prevRef == null) {
                // succesfully deposited the new lock.
                return newLock;
            }
            // existing lock may exist
            prev = prevRef.get();
            
            if (prev != null) {
                // exist lock
                return prev;
            }
            // stale reference; doesn't matter if fail
            this.rwlMap.remove(key,  prevRef);
        } while(true);
    }
//    public void removeUnusedLocks() {
//        this.collector.run();
//    }
}
