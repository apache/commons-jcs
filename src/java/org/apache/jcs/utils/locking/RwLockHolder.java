package org.apache.jcs.utils.locking;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/**
 * Used to keep track of the total number of outstanding locks placed but not
 * yet released for a given resource.
 *
 * @author asmuts
 * @created January 15, 2002
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

