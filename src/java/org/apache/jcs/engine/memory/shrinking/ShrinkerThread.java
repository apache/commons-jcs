package org.apache.jcs.engine.memory.shrinking;

/*
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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;

/**
 * A background memory shrinker. Just started. <u>DON'T USE</u>
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created February 18, 2002
 * @version $Id:
 */
public class ShrinkerThread extends Thread
{

    private MemoryCache cache;
    boolean alive = true;

    private final static Log log =
        LogFactory.getLog( ShrinkerThread.class );

    /**
     * Constructor for the ShrinkerThread object. Should take an IMemoryCache
     *
     * @param cache
     */
    public ShrinkerThread( MemoryCache cache )
    {
        super();
        this.cache = cache;
    }

    /**
     * Description of the Method
     */
    public void kill()
    {
        alive = false;
    }

    /**
     * Main processing method for the ShrinkerThread object
     */
    public void run()
    {

        while ( alive )
        {

            shrink();

            try
            {
                this.sleep( cache.getCacheAttributes()
                            .getShrinkerIntervalSeconds() * 1000 );
            }
            catch ( InterruptedException ie )
            {
                return;
            }
        }
        return;
    }

    /**
     * Constructor for the shrink object
     */
    protected void shrink()
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "Shrinking" );
        }

        // not thread safe.  Copuld cause problems.  Only call remove directly
        // to the map.
        try
        {

            java.util.Iterator itr = cache.getIterator();

            while ( itr.hasNext() )
            {
                Map.Entry e = ( Map.Entry ) itr.next();
                MemoryElementDescriptor me = ( MemoryElementDescriptor ) e.getValue();

                long now = System.currentTimeMillis();

                // Memory idle, to disk shrinkage
                if ( cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds() != -1 )
                {
                    long deadAt = me.ce.getElementAttributes().getLastAccessTime() + ( cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds() * 1000 );
                    if ( ( deadAt - now ) < 0 )
                    {
                        if ( log.isInfoEnabled() )
                        {
                            log.info( "Exceeded memory idle time, Pushing item to disk -- " + me.ce.getKey() + " over by = " + String.valueOf( deadAt - now ) + " ms." );
                        }
                        itr.remove();
                        cache.waterfal( me );
                    }
                }
                else if ( !me.ce.getElementAttributes().getIsEternal() )
                {
                    // Exceeded maxLifeSeconds
                    if ( ( me.ce.getElementAttributes().getMaxLifeSeconds() != -1 ) && ( now - me.ce.getElementAttributes().getCreateTime() ) > ( me.ce.getElementAttributes().getMaxLifeSeconds() * 1000 ) )
                    {
                        if ( log.isInfoEnabled() )
                        {
                            log.info( "Exceeded maxLifeSeconds -- " + me.ce.getKey() );
                        }
                        itr.remove();
                        //cache.remove( me.ce.getKey() );
                    }
                    else

                    // Exceeded maxIdleTime, removal
                        if ( ( me.ce.getElementAttributes().getIdleTime() != -1 ) && ( now - me.ce.getElementAttributes().getLastAccessTime() ) > ( me.ce.getElementAttributes().getIdleTime() * 1000 ) )
                        {
                            if ( log.isInfoEnabled() )
                            {
                                log.info( "Exceeded maxIdleTime [ me.ce.getElementAttributes().getIdleTime() = " + me.ce.getElementAttributes().getIdleTime() + " ]-- " + me.ce.getKey() );
                            }
                            itr.remove();
                            //cache.remove( me.ce.getKey() );
                        }
                }

            }
        }
        catch ( Throwable t )
        {
            log.info( "Expected trouble in shrink cycle", t );
            // keep going?
            // concurrent modifications will be a serious problem here
            // there is no good way yo interate througha  map without locking it

            //stop for now
            return;
        }

    }

}
