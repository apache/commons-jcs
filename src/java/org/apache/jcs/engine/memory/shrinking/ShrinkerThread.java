package org.apache.jcs.engine.memory.shrinking;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.memory.MemoryCache;

/**
 * A background memory shrinker. Memory problems and concurrent modification
 * exception caused by acting directly on an iterator of the underlying memory
 * cache should have been solved.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @version $Id$
 */
public class ShrinkerThread extends Thread
{
    private final static Log log = LogFactory.getLog( ShrinkerThread.class );

    /** The MemoryCache instance which this shrinker is watching */
    private final MemoryCache cache;

    /** The time to sleep between shrink runs */
    private final long shrinkerInterval;

    /** Maximum memory idle time for the whole cache */
    private final long maxMemoryIdleTime;

    /** Flag that indicates if the thread is still alive */
    boolean alive = true;

    /**
     * Constructor for the ShrinkerThread object.
     *
     * @param cache The MemoryCache which the new shrinker should watch.
     */
    public ShrinkerThread( MemoryCache cache )
    {
        super();

        this.cache = cache;

        this.shrinkerInterval =
            cache.getCacheAttributes().getShrinkerIntervalSeconds() * 1000;

        long maxMemoryIdleTimeSeconds =
            cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds();

        if ( maxMemoryIdleTimeSeconds == -1 )
        {
            this.maxMemoryIdleTime = -1;
        }
        else
        {
            this.maxMemoryIdleTime = maxMemoryIdleTimeSeconds * 1000;
        }
    }

    /**
     * Graceful shutdown after this round of processing.
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
                this.sleep( shrinkerInterval );
            }
            catch ( InterruptedException ie )
            {
                // Continue until killed ( alive == false )
            }
        }

        return;
    }

    /**
     * This method is called when the thread wakes up. Frist the method obtains
     * an array of keys for the cache region. It iterates through the keys and
     * tries to get the item from the cache without affecting the last access
     * or position of the item. The item is checked for expiration, the
     * expiration check has 3 parts:
     * <ol>
     *   <li>
     *     Has the cacheattributes.MaxMemoryIdleTimeSeconds defined for the
     *     region been exceeded? If so, the item should be move to disk.
     *   </li>
     *   <li>
     *     Has the item exceeded MaxLifeSeconds defined in the element
     *     attributes? If so, remove it.
     *   </li>
     *   <li>
     *     Has the item exceeded IdleTime defined in the element atributes?
     *     If so, remove it. If there are event listeners registered for
     *     the cache element, they will be called.
     *   </li>
     * </ol>
     *
     * @todo Change element event handling to use the queue, then move the
     *       queue to the region and access via the Cache.
     */
    protected void shrink()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Shrinking memory cache for: "
                       + this.cache.getCompositeCache().getCacheName() );
        }

        try
        {
            Object[] keys = cache.getKeyArray();
            int size = keys.length;

            Serializable key;
            ICacheElement cacheElement;
            IElementAttributes attributes;

            for ( int i = 0; i < size; i++ )
            {
                key = ( Serializable ) keys[ i ];
                cacheElement = cache.getQuiet( key );

                if ( cacheElement == null )
                {
                    continue;
                }

                attributes = cacheElement.getElementAttributes();

                boolean remove = false;

                long now = System.currentTimeMillis();

                // Useful, but overkill even for DEBUG since it is written for
                // every element in memory
                //
                // if ( log.isDebugEnabled() )
                // {
                //     log.debug( "IsEternal: " + attributes.getIsEternal() );
                //     log.debug( "MaxLifeSeconds: "
                //                + attributes.getMaxLifeSeconds() );
                //     log.debug( "CreateTime:" + attributes.getCreateTime() );
                // }

                // If the element is not eternal, check if it should be
                // removed and remove it if so.

                if ( !cacheElement.getElementAttributes().getIsEternal() )
                {
                    remove = checkForRemoval( cacheElement, now );

                    if ( remove )
                    {
                        cache.remove( cacheElement.getKey() );
                    }
                }

                // If the item is not removed, check is it has been idle
                // long enough to be spooled.

                if ( !remove && ( maxMemoryIdleTime != -1 ) )
                {
                    final long lastAccessTime = attributes.getLastAccessTime();

                    if ( lastAccessTime + maxMemoryIdleTime < now )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Exceeded memory idle time: "
                                       + cacheElement.getKey() );
                        }

                        // FIXME: Shouldn't we ensure that the element is
                        //        spooled before removing it from memory?

                        cache.remove( cacheElement.getKey() );

                        cache.waterfal( cacheElement );
                    }
                }

            }
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

    /**
     * Check if either lifetime or idletime has expired for the provided event,
     * and remove it from the cache if so.
     *
     * @param cacheElement Element to check for expiration
     * @param now Time to consider expirations relative to
     * @return true if the element should be removed, or false.
     * @throws IOException
     */
    private boolean checkForRemoval( ICacheElement cacheElement, long now )
        throws IOException
    {
        IElementAttributes attributes = cacheElement.getElementAttributes();

        final long maxLifeSeconds = attributes.getMaxLifeSeconds();
        final long createTime = attributes.getCreateTime();

        // Check if maxLifeSeconds has been exceeded
        if ( maxLifeSeconds != -1 && now - createTime > maxLifeSeconds * 1000 )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Exceeded maxLifeSeconds: " + cacheElement.getKey() );
            }

            handleElementEvents( cacheElement, IElementEventConstants
                .ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND );

            return true;
        }

        final long idleTime = attributes.getIdleTime();
        final long lastAccessTime = attributes.getLastAccessTime();

        // Check maxIdleTime has been exceeded
        if ( idleTime != -1 && now - lastAccessTime > idleTime * 1000 )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Exceeded maxIdleTime " + cacheElement.getKey() );
            }

            handleElementEvents( cacheElement, IElementEventConstants
                .ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND );

            return true;
        }

        return false;
    }

    /**
     * Handle any events registered for the given element of the given event
     * type.
     *
     * @param cacheElement Element to handle events for
     * @param eventType Type of event to handle
     * @throws IOException If an error occurs
     */
    private void handleElementEvents( ICacheElement cacheElement,
                                      int eventType )
        throws IOException
    {
        IElementAttributes attributes = cacheElement.getElementAttributes();

        ArrayList eventHandlers = attributes.getElementEventHandlers();

        if ( eventHandlers != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Handlers are registered, type: " + eventType );
            }

            IElementEvent event = new ElementEvent( cacheElement, eventType );

            Iterator handlerIter = eventHandlers.iterator();

            while ( handlerIter.hasNext() )
            {
                IElementEventHandler hand =
                    ( IElementEventHandler ) handlerIter.next();

                cache.getCompositeCache().addElementEvent( hand, event );
            }
        }
    }
}
