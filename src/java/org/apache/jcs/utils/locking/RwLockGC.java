package org.apache.jcs.utils.locking;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.locking.RwLockHolder;

/**
 * Used to enhance performance by delaying the removal of unreferenced
 * RwLockHolder objects managed by the ReadWriteLockManager.
 *  
 */
public class RwLockGC
    extends Thread
{
    private final static Log log = LogFactory.getLog( RwLockGC.class );

    private final Hashtable ht;

    private final static long IDLE_PERIOD = 60 * 1000;

    // 60 seconds.

    private boolean clean = true;

    /**
     * Constructor for the RwLockGC object
     * 
     * @param ht
     */
    RwLockGC( Hashtable ht )
    {
        this.ht = ht;
    }

    /**
     * Notifies the garbage collection that there is garbage available, and
     * kicks off the garbage collection process.
     */
    void notifyGarbage()
    {
        dirty();
        synchronized ( this )
        {
            notify();
        }
    }

    // Run forever.

    // Minimize the use of any synchronization in the process of garbage
    // collection
    // for performance reason.
    /** Main processing method for the RwLockGC object */
    public void run()
    {
        do
        {
            if ( clean )
            {
                synchronized ( this )
                {
                    if ( clean )
                    {
                        //p("RwLockGC entering into a wait state");
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
                //p("RwLockGC sleeping for " + IDLE_PERIOD);
                sleep( IDLE_PERIOD );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }
            // The "clean" flag must be false here.
            // Simply presume we can collect all the garbage until proven
            // otherwise.
            synchronized ( this )
            {
                clean = true;
            }
            long now = System.currentTimeMillis();
            // Take a snapshot of the hashtable.
            Map.Entry[] entries = (Map.Entry[]) ht.entrySet().toArray( new Map.Entry[0] );
            //p("RwLockHolder garbage collecting...");
            for ( int i = 0; i < entries.length; i++ )
            {
                RwLockHolder holder = (RwLockHolder) entries[i].getValue();
                if ( holder.removable( now ) )
                {
                    Object key = entries[i].getKey();
                    synchronized ( ht )
                    {
                        holder = (RwLockHolder) ht.get( key );
                        // holder cannot possibly be null as this should be the
                        // only thread removing them.
                        if ( holder.removable( now ) )
                        {
                            ht.remove( key );
                            /*
                             * p("removing key=" + key + ", now=" + now + ",
                             * holder.lastInactiveTime=" +
                             * holder.lastInactiveTime);
                             */
                        }
                    }
                }
            }
            // end for loop.
        }
        while ( true );
    }

    /** Sets the "clean" flag to false in a critial section. */
    private void dirty()
    {
        if ( clean )
        {
            synchronized ( this )
            {
                clean = false;
            }
        }
    }
}
