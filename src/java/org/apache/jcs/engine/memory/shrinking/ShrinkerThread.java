package org.apache.jcs.engine.memory.shrinking;

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
 * @version $Id$
 */
public class ShrinkerThread
    implements Runnable
{
    private final static Log log = LogFactory.getLog( ShrinkerThread.class );

    /** The MemoryCache instance which this shrinker is watching */
    private final MemoryCache cache;

    /** Maximum memory idle time for the whole cache */
    private final long maxMemoryIdleTime;

    /** Maximum number of items to spool per run. Default is -1, or no limit. */
    private int maxSpoolPerRun;

    private boolean spoolLimit = false;

    /**
     * Constructor for the ShrinkerThread object.
     * 
     * @param cache
     *            The MemoryCache which the new shrinker should watch.
     */
    public ShrinkerThread( MemoryCache cache )
    {
        super();

        this.cache = cache;

        long maxMemoryIdleTimeSeconds = cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds();

        if ( maxMemoryIdleTimeSeconds < 0 )
        {
            this.maxMemoryIdleTime = -1;
        }
        else
        {
            this.maxMemoryIdleTime = maxMemoryIdleTimeSeconds * 1000;
        }

        this.maxSpoolPerRun = cache.getCacheAttributes().getMaxSpoolPerRun();
        if ( this.maxSpoolPerRun != -1 )
        {
            this.spoolLimit = true;
        }

    }

    /**
     * Main processing method for the ShrinkerThread object
     */
    public void run()
    {
        shrink();
    }

    /**
     * This method is called when the thread wakes up. Frist the method obtains
     * an array of keys for the cache region. It iterates through the keys and
     * tries to get the item from the cache without affecting the last access or
     * position of the item. The item is checked for expiration, the expiration
     * check has 3 parts:
     * <ol>
     * <li>Has the cacheattributes.MaxMemoryIdleTimeSeconds defined for the
     * region been exceeded? If so, the item should be move to disk.</li>
     * <li>Has the item exceeded MaxLifeSeconds defined in the element
     * attributes? If so, remove it.</li>
     * <li>Has the item exceeded IdleTime defined in the element atributes? If
     * so, remove it. If there are event listeners registered for the cache
     * element, they will be called.</li>
     * </ol>
     * 
     * @todo Change element event handling to use the queue, then move the queue
     *       to the region and access via the Cache.
     */
    protected void shrink()
    {
        if ( log.isDebugEnabled() )
        {
            if ( this.cache.getCompositeCache() != null )
            {
                log.debug( "Shrinking memory cache for: " + this.cache.getCompositeCache().getCacheName() );
            }
        }

        try
        {
            Object[] keys = cache.getKeyArray();
            int size = keys.length;
            if ( log.isDebugEnabled() )
            {
                log.debug( "Keys size: " + size );
            }

            Serializable key;
            ICacheElement cacheElement;
            IElementAttributes attributes;

            int spoolCount = 0;

            for ( int i = 0; i < size; i++ )
            {
                key = (Serializable) keys[i];
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
                // log.debug( "IsEternal: " + attributes.getIsEternal() );
                // log.debug( "MaxLifeSeconds: "
                // + attributes.getMaxLifeSeconds() );
                // log.debug( "CreateTime:" + attributes.getCreateTime() );
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
                    if ( !spoolLimit || ( spoolCount < this.maxSpoolPerRun ) )
                    {

                        final long lastAccessTime = attributes.getLastAccessTime();

                        if ( lastAccessTime + maxMemoryIdleTime < now )
                        {
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "Exceeded memory idle time: " + cacheElement.getKey() );
                            }

                            // Shouldn't we ensure that the element is
                            // spooled before removing it from memory?
                            // No the disk caches have a purgatory. If it fails
                            // to spool that does not affect the
                            // responsibilities of the memory cache.

                            spoolCount++;

                            cache.remove( cacheElement.getKey() );

                            cache.waterfal( cacheElement );

                            key = null;
                            cacheElement = null;
                        }
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "spoolCount = '" + spoolCount + "'; " + "maxSpoolPerRun = '" + maxSpoolPerRun
                                + "'" );
                        }

                        // stop processing if limit has been reached.
                        if ( spoolLimit && ( spoolCount >= this.maxSpoolPerRun ) )
                        {
                            keys = null;
                            return;
                        }
                    }
                }
            }

            keys = null;
        }
        catch ( Throwable t )
        {
            log.info( "Unexpected trouble in shrink cycle", t );

            // concurrent modifications should no longer be a problem
            // It is up to the IMemoryCache to return an array of keys

            // stop for now
            return;
        }

    }

    /**
     * Check if either lifetime or idletime has expired for the provided event,
     * and remove it from the cache if so.
     * 
     * @param cacheElement
     *            Element to check for expiration
     * @param now
     *            Time to consider expirations relative to
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

            handleElementEvents( cacheElement, IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND );

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

            handleElementEvents( cacheElement, IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND );

            return true;
        }

        return false;
    }

    /**
     * Handle any events registered for the given element of the given event
     * type.
     * 
     * @param cacheElement
     *            Element to handle events for
     * @param eventType
     *            Type of event to handle
     * @throws IOException
     *             If an error occurs
     */
    private void handleElementEvents( ICacheElement cacheElement, int eventType )
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
                IElementEventHandler hand = (IElementEventHandler) handlerIter.next();

                // extra safety
                // TODO we shouldn't be operating on a variable of another class.
                // we did this to get away from the singelton composite cache.
                // we will need to create an event manager and pass it around instead.
                if ( cache.getCompositeCache() != null )
                {
                    cache.getCompositeCache().addElementEvent( hand, event );                    
                }
            }
        }
    }
}
