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
 * @author Hanson Char
 */
public class KeyedReadWriteLock implements IKeyedReadWriteLock {
    private final ConcurrentMap<String, KeyedWeakReference<ReadWriteLock>> rwlMap = 
            new ConcurrentHashMap<String, KeyedWeakReference<ReadWriteLock>>();
    private final Class<? extends ReadWriteLock> rwlClass;
    private final ReferenceQueue<ReadWriteLock> refQ = 
            new ReferenceQueue<ReadWriteLock>();
    private final KeyedRefCollector collector = new KeyedRefCollector(refQ, rwlMap);
    public KeyedReadWriteLock() {
        this.rwlClass = ReentrantReadWriteLock.class;
    }
    public KeyedReadWriteLock(Class<? extends ReadWriteLock> rwlClass) {
        this.rwlClass = rwlClass;
    }
    public Lock readLock(String key) {
        return this.readWriteLock(key).readLock();
    }
    public Lock writeLock(String key) {
        return this.readWriteLock(key).writeLock();
    }
    private ReadWriteLock readWriteLock(String key) {
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
    private ReadWriteLock getLock(final String key, ReadWriteLock newLock) 
    {
        this.collector.run();
        final KeyedWeakReference<ReadWriteLock> newLockRef = 
                new KeyedWeakReference<ReadWriteLock>(key, newLock, refQ);
        KeyedWeakReference<ReadWriteLock> prevRef = 
                this.rwlMap.putIfAbsent(key, newLockRef);
        if (prevRef == null) {
            // succesfully deposited the new lock.
            return newLock;
        }
        ReadWriteLock prev = prevRef.get();
        
        for (; prev == null; prev=prevRef.get()) {
            // Unused lock is garbage collected.  So clean it up.
            rwlMap.remove(key, prevRef);  // remove may fail, but that's fine.
            prevRef = this.rwlMap.putIfAbsent(key, newLockRef);

            if (prevRef == null) {
                // succesfully deposited the new lock.
                return newLock;
            }
            // data race: someone else has just put in a lock.
        }
        // Return the lock deposited by another thread.
        return prev;
    }
//    public void removeUnusedLocks() {
//        this.collector.run();
//    }
}
