package org.apache.jcs.utils.servlet.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class DistSessionPoolManager
{
    private final static Log log =
        LogFactory.getLog( DistSessionPoolManager.class );

    DistSession[] pool;
    boolean[] inUse;


    /** Description of the Method */
    public static void main( String args[] )
    {
        DistSessionPoolManager hpm = new DistSessionPoolManager( 200 );

        int num = 1000000;

        long start = System.currentTimeMillis();
        for ( int i = 0; i < num; i++ )
        {
            DistSession ht = new DistSession();
        }
        long end = System.currentTimeMillis();
        System.out.println( "New DistSession creation took " + String.valueOf( end - start ) + " millis." );

        start = System.currentTimeMillis();
        for ( int i = 0; i < num; i++ )
        {
            DistSession ht = hpm.getDistSession();
            //ht.put( "tre", "ret" );
            hpm.returnDistSession( ht );
        }
        end = System.currentTimeMillis();
        System.out.println( "Pooled get and return of Hashtable took " + String.valueOf( end - start ) + " millis." );

    }


    /**
     * Constructor for the DistSessionPoolManager object
     *
     * @param initialPoolSize
     */
    public DistSessionPoolManager( int initialPoolSize )
    {
        log.info( "initialPoolSize = " + initialPoolSize );

        pool = new DistSession[initialPoolSize];
        inUse = new boolean[initialPoolSize];
        for ( int i = pool.length - 1; i >= 0; i-- )
        {
            pool[i] = new DistSession();
            inUse[i] = false;
        }
    }


    /**
     * Gets the distSession attribute of the DistSessionPoolManager object
     *
     * @return The distSession value
     */
    public synchronized DistSession getDistSession()
    {
        for ( int i = inUse.length - 1; i >= 0; i-- )
        {
            if ( !inUse[i] )
            {
                inUse[i] = true;
                return pool[i];
            }
        }

        //If we got here, then all the DistSessions are in use. We will increase the number in our
        //pool by 10.
        boolean[] old_inUse = inUse;
        inUse = new boolean[old_inUse.length + 10];
        System.arraycopy( old_inUse, 0, inUse, 0, old_inUse.length );

        DistSession[] old_pool = pool;
        pool = new DistSession[old_pool.length + 10];
        System.arraycopy( old_pool, 0, pool, 0, old_pool.length );

        for ( int i = old_pool.length; i < pool.length; i++ )
        {
            pool[i] = new DistSession();
            inUse[i] = false;
        }
        inUse[pool.length - 1] = true;
        return pool[pool.length - 1];
    }


    /** Description of the Method */
    public synchronized void returnDistSession( DistSession v )
    {
        for ( int i = inUse.length - 1; i >= 0; i-- )
        {
            if ( pool[i] == v )
            {
                inUse[i] = false;
                v.clean();
                return;
            }
        }
        log.warn( "DistSession was not obtained from the pool: " + v );
    }

}
// end class
