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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Generic ReadWriteLock Manager for various resources.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ReadWriteLockManager
{
    private final static Log log =
        LogFactory.getLog( ReadWriteLockManager.class );

    /**
     * Used to asynchronously remove unused RwLockHolder objects managed ty this
     * manager.
     */
    private static RwLockGC gc;

    /**
     * Hashtable of locks managed
     */
    private Hashtable locks = new Hashtable();

    /** Places a read lock on the specified resource. */
    public final void readLock( String id )
        throws InterruptedException
    {
        lock( id, false );
    }

    /** Places a write lock on the specified resource. */
    public final void writeLock( String id )
        throws InterruptedException
    {
        lock( id, true );
    }

     /** Places either a read or write lock on the specified resource. */
    private void lock( String id, boolean isWrite )
        throws InterruptedException
    {
        // For messages

        String lockType = isWrite ? "write" : "read";

        if ( log.isDebugEnabled() )
        {
            log.debug( "about to get " + lockType + " lock for id: " + id );
        }

        RwLockHolder holder;

        ensureGarbageCollectorCreated();

        Hashtable ht = getLocks();

        synchronized ( ht )
        {
            holder = ( RwLockHolder ) ht.get( id );

            if ( holder != null )
            {
                // Lock already exists.  So just use it.
                holder.lcount++;

                if ( log.isDebugEnabled() )
                {
                    log.debug( "Incrementing holder count to "
                               + holder.lcount + " on "
                               + lockType + " lock for id = " + id );
                }
            }
        }

        if ( holder == null )
        {
            // Lock does not exist.  So create a new one.

            RwLockHolder newHolder = new RwLockHolder( new ReadWriteLock() );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Creating new lock holder, lock type: " + lockType );
            }

            synchronized ( ht )
            {
                holder = ( RwLockHolder ) ht.put( id, newHolder );

                if ( holder != null )
                {
                    // Oops, the lock is already created by someone else concurrently.
                    // So we increment, put it back and discard the new lock we just created.
                    // We use this strategy to minimize the time spent in the synchronized block.
                    holder.lcount++;
                    ht.put( id, holder );
                }
            }
            if ( holder == null )
            {
                // no concurrency issue -- the new lock is now used.
                holder = newHolder;
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( lockType + " lock created for " + id );
            }
        }

        // Be careful not to put the following code into a synchronized block.
        // Otherwise, deadlock can easily happen as the writeLock() and
        // readLock() may result in the ReadWriteLock object being waited!

        if ( isWrite )
        {
            holder.writeLock();
        }
        else
        {
            holder.readLock();
        }
        return;
    }

    /** Ensures that the lock garbage collector has been created */
    private synchronized void ensureGarbageCollectorCreated()
    {
        if ( gc == null )
        {
            gc = new RwLockGC( getLocks() );
            gc.setDaemon( true );
            gc.start();
        }
    }

    /**
     * Release the read/write lock previously placed on the specified resource.
     */
    public final void done( String id )
    {
        Hashtable ht = getLocks();

        RwLockHolder holder = ( RwLockHolder ) ht.get( id );

        if ( holder == null )
        {
            String message =
                "done called without an outstanding lock for id: " + id;

            if ( log.isDebugEnabled() )
            {
                log.debug( message );
            }

            throw new IllegalStateException( message );
        }

        holder.done();

        if ( log.isDebugEnabled() )
        {
            log.debug( "lock done for id = " + id );
        }
        // Somehow if we don't synchronize while changing the count,
        // the count went down below zero!
        // Theoretically this should never happen, as a "done" is always preceeded
        // by either a read or write lock issued from the very same thread.
        // So for the moment, let's blame the JVM and make it work via synchronization,
        // until futher investigation.
        int lcount;
        // used to minimize the time spent in the synchronized block.
        synchronized ( ht )
        {
            lcount = --holder.lcount;
        }

        if ( lcount > 0 )
        {
            return;
        }
        else if ( lcount == 0 )
        {
            holder.lastInactiveTime = System.currentTimeMillis();

            gc.notifyGarbage();
            return;
        }
        else
        {
            throw new IllegalStateException(
                "holder.lcount went down below zero ("
                + holder.lcount + ") for id=" + id );
        }
    }

    /**
     * Returns the lock table of all the resources managed by the subclass.
     *
     * @return The locks value
     */
    protected Hashtable getLocks()
    {
        return locks;
    }
}

