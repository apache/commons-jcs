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
public abstract class ReadWriteLockManager
{
    private final static Log log =
        LogFactory.getLog( ReadWriteLockManager.class );

    /**
     * Used to asynchronously remove unused RwLockHolder objects managed ty this
     * manager.
     */
    private static RwLockGC gc;

    /** Class name for debugging purposes. */
    private String clsname;

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


    /**
     * Release the read/write lock previously placed on the specified resource.
     */
    public final void done( String id )
    {
        Hashtable ht = getLocks();
        RwLockHolder holder = ( RwLockHolder ) ht.get( id );

        if ( holder == null )
        {

            log.debug( "Method done of " + getClass().getName() + " invoked without an outstanding lock; id=" + id );
            //System.exit(1);

            throw new IllegalStateException( "Method done of " + getClass().getName()
                 + " invoked without an outstanding lock; id=" + id );
        }
        holder.rwlock.done();
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

        //  p("-- holder.lcount=" + holder.lcount);
        if ( lcount == 0 )
        {
            holder.lastInactiveTime = System.currentTimeMillis();
            //p("notify: Gargage available");
            gc.notifyGarbage();
            return;
        }
        // lcount is negative! should never get here.
        /*
         * p("holder.lcount went down below zero (" + holder.lcount + ") for id=" + id);
         * System.exit(1);
         */
        throw new IllegalStateException( "holder.lcount went down below zero ("
             + holder.lcount + ") for id=" + id );
    }


    /** Places either a read or write lock on the specified resource. */
    private void lock( String id, boolean isWrite )
        throws InterruptedException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "about to get lock, isWrite=" + isWrite + " for id = " + id );
        }
        RwLockHolder holder;
        Hashtable ht = getLocks();
        if ( gc == null )
        {
            synchronized ( this )
            {
                if ( gc == null )
                {
                    gc = new RwLockGC( ht );
                    gc.setDaemon( true );
                    gc.start();
                }
            }
        }
        synchronized ( ht )
        {
            holder = ( RwLockHolder ) ht.get( id );
            if ( holder != null )
            {
                // Lock already exists.  So just use it.
                holder.lcount++;
                if ( log.isDebugEnabled() )
                {
                    log.debug( "++ holder.lcount=" + holder.lcount + ", isWrite=" + isWrite + " for id = " + id );
                }
            }
        }
        if ( holder == null )
        {
            // Lock does not exist.  So create a new one.
            RwLockHolder newHolder = new RwLockHolder( new ReadWriteLock() );
            if ( log.isDebugEnabled() )
            {
                log.debug( "holder is null, isWrite=" + isWrite );
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
                log.debug( ( isWrite ? "Write" : "Read" ) + " lock created for " + id );
            }
        }
        // Be careful not to put the following code into a synchronized block.
        // Otherwise, deadlock can easily happen as the writeLock() and readLock() may result
        // in the ReadWriteLock object being waited!
        if ( isWrite )
        {
            holder.rwlock.writeLock();
        }
        else
        {
            holder.rwlock.readLock();
        }
        return;
    }


    /**
     * Returns the lock table of all the resources managed by the subclass.
     *
     * @return The locks value
     */
    protected abstract Hashtable getLocks();


    /**
     * Subclass must always override this constructor to create and preserve a
     * singleton instance of the subclass.
     */
    protected ReadWriteLockManager()
    {
        clsname = getClass().getName();
        clsname = clsname.substring( clsname.lastIndexOf( '.' ) + 1 );
    }
}

