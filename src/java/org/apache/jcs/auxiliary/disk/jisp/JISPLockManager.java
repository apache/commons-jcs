package org.apache.jcs.auxiliary.disk.jisp;

import java.util.Hashtable;

import org.apache.jcs.utils.locking.ReadWriteLockManager;

/**
 * Read/Write lock manager for Disk.
 *
 * @author asmuts
 * @created January 15, 2002
 */
class JISPLockManager extends ReadWriteLockManager
{

    /**
     * @TODO might need to have this lock for only one cache at a time might
     *      want to lock on a Diskcache instance
     */
    public final static String Disk = "Disk";
    private static JISPLockManager instance;

    private final Hashtable ht = new Hashtable();


    /** Constructor for the JISPLockManager object */
    private JISPLockManager() { }


    /**
     * Gets the instance attribute of the JISPLockManager class
     *
     * @return The instance value
     */
    static JISPLockManager getInstance()
    {
        if ( instance == null )
        {
            synchronized ( JISPLockManager.class )
            {
                if ( instance == null )
                {
                    instance = new JISPLockManager();
                }
            }
        }
        return instance;
    }


    /**
     * Gets the locks attribute of the JISPLockManager object
     *
     * @return The locks value
     */
    protected Hashtable getLocks()
    {
        return ht;
    }


    /** Description of the Method */
    void readLock()
    {
        try
        {
            readLock( Disk );
        }
        catch ( InterruptedException ex )
        {
            // should never happen.
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
    }


    /** Description of the Method */
    void writeLock()
    {
        try
        {
            writeLock( Disk );
        }
        catch ( InterruptedException ex )
        {
            // should never happen.
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
    }


    /** Description of the Method */
    void done()
    {
        done( Disk );
    }
}
