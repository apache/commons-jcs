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

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class coordinates concurrent calls to an object's get and set methods so
 * that calls to the object set methods do not interfere with each other or with
 * calls to the object's get methods. <br>
 * <br>
 * Only a single instance of this class should be created per specific resource
 * that requires Read/Write lock protection. <br>
 * <br>
 * The invariant required by this class is that the method <code>done</code>
 * must be called, and only be called, after a previous call to either the
 * method <code>readLock</code> or <code>writeLock</code>.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ReadWriteLock
{
    private final static Log log =
        LogFactory.getLog( ReadWriteLock.class );

    /** Number of threads waiting to read. */
    private int waitingForReadLock = 0;

    /** Number of threads reading. */
    private int outstandingReadLocks = 0;

    /** The thread that has the write lock or null. */
    private Thread writeLockedThread;

    /**
     * The number of (nested) write locks that have been requested from
     * writeLockedThread.
     */
    private int outstandingWriteLocks = 0;

    /**
     * Threads waiting to get a write lock are tracked in this ArrayList to
     * ensure that write locks are issued in the same order they are requested.
     */
    private ArrayList waitingForWriteLock = new ArrayList();


    /** Default constructor. */
    public ReadWriteLock() { }


    /**
     * Issue a read lock if there is no outstanding write lock or threads
     * waiting to get a write lock. Caller of this method must be careful to
     * avoid synchronizing the calling code so as to avoid deadlock.
     */
    public synchronized void readLock()
        throws InterruptedException
    {
        waitingForReadLock++;
        while ( writeLockedThread != null )
        {
            log.debug( "readLock wait" );
            wait(20);
            log.debug( "wake up from readLock wait" );
        }

        log.debug( "readLock acquired" );

        waitingForReadLock--;
        outstandingReadLocks++;
    }


    /**
     * Issue a write lock if there are no outstanding read or write locks.
     * Caller of this method must be careful to avoid synchronizing the calling
     * code so as to avoid deadlock.
     */
    public void writeLock()
        throws InterruptedException
    {
        Thread thisThread = Thread.currentThread();
        synchronized ( this )
        {
            if ( writeLockedThread == null && outstandingReadLocks == 0 )
            {
                writeLockedThread = Thread.currentThread();
                outstandingWriteLocks++;

                log.debug( "writeLock acquired without waiting" );

                return;
            }
            if ( writeLockedThread == thisThread )
            {
                // nested write locks from the same thread.
                outstandingWriteLocks++;
            }
            waitingForWriteLock.add( thisThread );
        }
        synchronized ( thisThread )
        {
            while ( thisThread != writeLockedThread )
            {
                log.debug( "writeLock wait" );
                // set this so if there is an error the app will not completely die!
                thisThread.wait( 2000 );
                log.debug( "wake up from writeLock wait" );
            }

            log.debug( "writeLock acquired" );
        }
        synchronized ( this )
        {
            int i = waitingForWriteLock.indexOf( thisThread );
            waitingForWriteLock.remove( i );
        }
    }


    /**
     * Threads call this method to relinquish a lock that they previously got
     * from this object.
     *
     * @throws IllegalStateException if called when there are no outstanding
     *      locks or there is a write lock issued to a different thread.
     */
    public synchronized void done()
    {
        if ( outstandingReadLocks > 0 )
        {
            outstandingReadLocks--;
            if ( outstandingReadLocks == 0 && waitingForWriteLock.size() > 0 )
            {
                writeLockedThread = ( Thread ) waitingForWriteLock.get( 0 );
                if ( log.isDebugEnabled() )
                {
                    log.debug( "readLock released and before notifying a write lock waiting thread "
                         + writeLockedThread );
                }
                synchronized ( writeLockedThread )
                {
                    writeLockedThread.notifyAll();
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "readLock released and after  notifying a write lock waiting thread "
                         + writeLockedThread );
                }
            }
            else if ( log.isDebugEnabled() )
            {
                log.debug( "readLock released without fuss" );
            }
            return;
        }

        if ( Thread.currentThread() == writeLockedThread )
        {

            //log.info( "outstandingWriteLocks= " + outstandingWriteLocks );
            if ( outstandingWriteLocks > 0 )
            {
                outstandingWriteLocks--;
            }
            else
            {
                log.warn( "extra lock release, writelocks are " + outstandingWriteLocks + "and done was called" );
            }

            if ( outstandingWriteLocks > 0 )
            {
                log.debug( "writeLock released for a nested writeLock request." );
                return;
            }
            if ( outstandingReadLocks == 0 && waitingForWriteLock.size() > 0 )
            {
                writeLockedThread = ( Thread ) waitingForWriteLock.get( 0 );
                if ( log.isDebugEnabled() )
                {
                    log.debug( "writeLock released and before notifying a write lock waiting thread "
                         + writeLockedThread );
                }
                synchronized ( writeLockedThread )
                {
                    writeLockedThread.notifyAll();
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "writeLock released and after notifying a write lock waiting thread "
                         + writeLockedThread );
                }
            }
            else
            {
                writeLockedThread = null;
                if ( waitingForReadLock > 0 )
                {
                    log.debug( "writeLock released, notified waiting readers" );

                    notifyAll();
                }
                else
                {
                    log.debug( "writeLock released, no readers waiting" );
                }
            }
            return;
        }

        throw new IllegalStateException( "Thread does not have lock" );
    }
}

