package org.apache.jcs.engine.control.group;

import java.util.Hashtable;

import org.apache.jcs.utils.locking.ReadWriteLockManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The ReadWriteLock Manager for distributed group list management.
 *
 * @author asmuts
 * @created January 15, 2002
 */
class GroupRWLockManager extends ReadWriteLockManager
{
    private final static Log log =
        LogFactory.getLog( GroupRWLockManager.class );

    /** Description of the Field */
    private static GroupRWLockManager instance;
    private final Hashtable ht = new Hashtable();

    /**
     * Returns the lock table of all the resources managed by this manager.
     *
     * @return The locks value
     */
    protected Hashtable getLocks()
    {
        return ht;
    }


    /** Constructor for the GroupRWLockManager object */
    private GroupRWLockManager() { }


    /**
     * Gets the instance attribute of the GroupRWLockManager class
     *
     * @return The instance value
     */
    static GroupRWLockManager getInstance()
    {
        if ( instance == null )
        {
            synchronized ( GroupRWLockManager.class )
            {
                if ( instance == null )
                {
                    instance = new GroupRWLockManager();
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "   >> GroupRWLockManager instanciated." );
                    }
                }
            }
        }
        return instance;
    }
}
