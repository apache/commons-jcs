
package org.apache.jcs.utils.servlet.session;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.servlet.session.DistSession;
import org.apache.jcs.utils.servlet.session.ISessionConstants;

/**
 * Garbage collector for the DistSession objects.
 *
 * @author asmuts
 * @created January 15, 2002
 */
class DistSessionGC extends Thread implements ISessionConstants
{
    private final static Log log =
        LogFactory.getLog( DistSessionGC.class );

    private final static long IDLE_PERIOD = DFLT_INACTIVE_INTERVAL;
    private final Set sessIdSet;

    /**
     * Constructs with the given set of session id's.
     *
     * @param sessIdSet
     */
    DistSessionGC( Set sessIdSet )
    {
        this.sessIdSet = sessIdSet;
    }


    /**
     * Notifies the garbage collector that there is session available and kicks
     * off the garbage collection process.
     */
    void notifySession()
    {
        synchronized ( this )
        {
            notify();
        }
    }


    // Run forever.
    // Minimize the use of any synchronization in the process of garbage collection
    // for performance reason.
    /** Main processing method for the DistSessionGC object */
    public void run()
    {
        do
        {
            if ( sessIdSet.size() == 0 )
            {
                synchronized ( this )
                {
                    if ( sessIdSet.size() == 0 )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "entering into a wait state" );
                        }
                        // Garbage driven mode.
                        try
                        {
                            wait();
                            // wake up only if there is garbage.
                        }
                        catch ( InterruptedException ignore )
                        {
                        }
                    }
                }
            }
            // Time driven mode: sleep between each round of garbage collection.
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "sessIdSet.size()=" + sessIdSet.size() );
                    log.debug( "sleeping for " + IDLE_PERIOD );
                }
                Thread.currentThread().sleep( IDLE_PERIOD );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }
            long now = System.currentTimeMillis();
            // Take a snapshot of the hashtable.
            String[] sessIds = null;
            synchronized ( sessIdSet )
            {
                sessIds = ( String[] ) sessIdSet.toArray( new String[0] );
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "garbage collecting..." );
            }
            for ( int i = 0; i < sessIds.length; i++ )
            {
                String session_id = sessIds[i];
                DistSession sess = new DistSession();
                if ( !sess.init( session_id ) )
                {
                    continue;
                }
                long idleTime = System.currentTimeMillis() - sess.getLastAccessedTime();
                int max = sess.getMaxInactiveInterval();
                if ( idleTime >= max )
                {
                    sessIdSet.remove( session_id );
                    sess.invalidate();
                    continue;
                }
            }
            // end for loop.
        } while ( true );
    }
}

