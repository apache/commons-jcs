package org.apache.jcs.utils.servlet.session;

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

