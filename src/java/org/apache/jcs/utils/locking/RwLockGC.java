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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.locking.RwLockHolder;

/**
 * Used to enhance performance by delaying the removal of unreferenced
 * RwLockHolder objects managed by the ReadWriteLockManager.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RwLockGC extends Thread
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

    // Minimize the use of any synchronization in the process of garbage collection
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
                Thread.currentThread().sleep( IDLE_PERIOD );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }
            // The "clean" flag must be false here.
            // Simply presume we can collect all the garbage until proven otherwise.
            synchronized ( this )
            {
                clean = true;
            }
            long now = System.currentTimeMillis();
            // Take a snapshot of the hashtable.
            Map.Entry[] entries = ( Map.Entry[] ) ht.entrySet().toArray( new Map.Entry[0] );
//p("RwLockHolder garbage collecting...");
            for ( int i = 0; i < entries.length; i++ )
            {
                RwLockHolder holder = ( RwLockHolder ) entries[i].getValue();
                if ( holder.removable( now ) )
                {
                    Object key = entries[i].getKey();
                    synchronized ( ht )
                    {
                        holder = ( RwLockHolder ) ht.get( key );
                        // holder cannot possibly be null as this should be the only thread removing them.
                        if ( holder.removable( now ) )
                        {
                            ht.remove( key );
                            /*
                             * p("removing key=" + key + ", now=" + now + ", holder.lastInactiveTime="
                             * + holder.lastInactiveTime);
                             */
                        }
                    }
                }
            }
            // end for loop.
        } while ( true );
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

