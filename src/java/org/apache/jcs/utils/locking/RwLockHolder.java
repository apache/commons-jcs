package org.apache.jcs.utils.locking;


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


/**
 * Used to keep track of the total number of outstanding locks placed but not
 * yet released for a given resource.
 *
 */
class RwLockHolder
{
    // 10 seconds
    private final static long UNUSED_TIME = 10 * 1000;

    /** Contained ReadWriteLock */
    private final ReadWriteLock rwlock;

    /**
     * Number of locks that have been placed on the rwlock and not yet released.
     */
    int lcount = 1;

    /** Last timestamp when the lcount was zero. */
    long lastInactiveTime = -1;

    /**
     * Constructs with a Read/Write lock for a specific resource.
     *
     * @param rwlock
     */
    RwLockHolder( ReadWriteLock rwlock )
    {
        this.rwlock = rwlock;
    }

    /**
     * Returns true iff this object satisfies the condition of removing
     * RwLockHolder from the managing ReadWriteLockManager.
     */
    boolean removable( long now )
    {
        return lcount == 0
               && lastInactiveTime > 0
               && now - lastInactiveTime > UNUSED_TIME;
    }

    /** @see ReadWriteLock#readLock */
    public void readLock() throws InterruptedException
    {
        rwlock.readLock();
    }

    /** @see ReadWriteLock#writeLock */
    public void writeLock() throws InterruptedException
    {
        rwlock.writeLock();
    }

    /** @see ReadWriteLock#done */
    public void done()
    {
        rwlock.done();
    }

}

