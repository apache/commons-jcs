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
