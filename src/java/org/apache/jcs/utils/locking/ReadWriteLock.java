
package org.apache.jcs.utils.locking;

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
            wait();
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
            outstandingWriteLocks--;
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

