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
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;

import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 *  A background memory shrinker. Memory problems and concurrent modification
 *  exception caused by acting directly on an iterator of the underlying memory
 *  cache should have been solved.
 *
 *@author     <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 *@created    February 18, 2002
 *@version    $Id:
 */
public class ShrinkerThread extends Thread
{

    private MemoryCache cache;
    boolean alive = true;

    private final static Log log =
        LogFactory.getLog( ShrinkerThread.class );

    /**
     *  Constructor for the ShrinkerThread object. Should take an IMemoryCache
     *
     *@param  cache
     */
    public ShrinkerThread( MemoryCache cache )
    {
        super();
        this.cache = cache;
    }

    /**
     *  Graceful shutdown after this round of processing.
     */
    public void kill()
    {
        alive = false;
    }

    /**
     *  Main processing method for the ShrinkerThread object
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
     *  This method is called when the thread wakes up. A. The method obtains an
     *  array of keys for the cache region. B. It iterates through the keys and
     *  tries to get the item from the cache without affecting the last access
     *  or position of the item. C. Then the item is checked for expiration.
     *  This expiration check has 3 parts: 1. Has the
     *  cacheattributes.MaxMemoryIdleTimeSeconds defined for the region been
     *  exceeded? If so, the item should be move to disk. 2. Has the item
     *  exceeded MaxLifeSeconds defined in the element atributes? If so, remove
     *  it. 3. Has the item exceeded IdleTime defined in the element atributes?
     *  If so, remove it. If there are event listeners registered for the cache
     *  element, they will be called.
     */
    protected void shrink()
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "Shrinking" );
        }

        try
        {

            Object[] keys = cache.getKeyArray();
            int size = keys.length;
            for ( int i = 0; i < size; i++ )
            {

                Serializable key = ( Serializable ) keys[i];
                ICacheElement ce = cache.getQuiet( key );

                if ( ce != null )
                {

                    long now = System.currentTimeMillis();

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "now = " + now );
                        log.debug( "!ce.getElementAttributes().getIsEternal() = " + !ce.getElementAttributes().getIsEternal() );
                        log.debug( "ce.getElementAttributes().getMaxLifeSeconds() = " + ce.getElementAttributes().getMaxLifeSeconds() );
                        log.debug( "now - ce.getElementAttributes().getCreateTime() = " + String.valueOf( now - ce.getElementAttributes().getCreateTime() ) );
                        log.debug( "ce.getElementAttributes().getMaxLifeSeconds() * 1000 = " + ce.getElementAttributes().getMaxLifeSeconds() * 1000 );
                    }

                    // Memory idle, to disk shrinkage
                    if ( cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds() != -1 )
                    {
                        long deadAt = ce.getElementAttributes().getLastAccessTime() + ( cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds() * 1000 );
                        if ( ( deadAt - now ) < 0 )
                        {
                            if ( log.isInfoEnabled() )
                            {
                                log.info( "Exceeded memory idle time, Pushing item to disk -- " + ce.getKey() + " over by = " + String.valueOf( deadAt - now ) + " ms." );
                            }

                            cache.remove( ce.getKey() );

                            cache.waterfal( ce );
                        }
                    }

                    ////////////////////////////////////////////////
                    if ( !ce.getElementAttributes().getIsEternal() )
                    {
                        // Exceeded maxLifeSeconds
                        if ( ( ce.getElementAttributes().getMaxLifeSeconds() != -1 ) && ( now - ce.getElementAttributes().getCreateTime() ) > ( ce.getElementAttributes().getMaxLifeSeconds() * 1000 ) )
                        {
                            if ( log.isInfoEnabled() )
                            {
                                log.info( "Exceeded maxLifeSeconds -- " + ce.getKey() );
                            }

                            // handle event, might move to a new method
                            ArrayList eventHandlers = ce.getElementAttributes().getElementEventHandlers();
                            if ( eventHandlers != null )
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Handlers are registered.  Event -- ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND" );
                                }
                                IElementEvent event = new ElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND );
                                Iterator hIt = eventHandlers.iterator();
                                while ( hIt.hasNext() )
                                {
                                    IElementEventHandler hand = ( IElementEventHandler ) hIt.next();
                                    hand.handleElementEvent( event );
                                }
                            }

                            cache.remove( ce.getKey() );
                        }
                        else
                        // Exceeded maxIdleTime, removal
                            if ( ( ce.getElementAttributes().getIdleTime() != -1 ) && ( now - ce.getElementAttributes().getLastAccessTime() ) > ( ce.getElementAttributes().getIdleTime() * 1000 ) )
                        {
                            if ( log.isInfoEnabled() )
                            {
                                log.info( "Exceeded maxIdleTime [ ce.getElementAttributes().getIdleTime() = " + ce.getElementAttributes().getIdleTime() + " ]-- " + ce.getKey() );
                            }

                            // handle event, might move to a new method
                            ArrayList eventHandlers = ce.getElementAttributes().getElementEventHandlers();
                            if ( eventHandlers != null )
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Handlers are registered.  Event -- ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND" );
                                }
                                IElementEvent event = new ElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND );
                                Iterator hIt = eventHandlers.iterator();
                                while ( hIt.hasNext() )
                                {
                                    IElementEventHandler hand = ( IElementEventHandler ) hIt.next();
                                    hand.handleElementEvent( event );
                                }
                            }

                            cache.remove( ce.getKey() );
                        }

                    }// end if not eternal

                }// end if ce != null

            }// end for

        }
        catch ( Throwable t )
        {
            log.info( "Unexpected trouble in shrink cycle", t );

            // concurrent modifications should no longer be a problem
            // It is up to the IMemoryCache to return an array of keys

            //stop for now
            return;
        }

    }

}
