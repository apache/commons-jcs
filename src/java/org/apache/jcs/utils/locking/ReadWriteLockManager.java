package org.apache.jcs.utils.locking;

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

