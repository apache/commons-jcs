/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp;

import java.io.IOException;
import java.io.Serializable;

import java.util.Hashtable;

import org.apache.jcs.auxiliary.disk.jisp.behavior.IJISPCacheAttributes;
import org.apache.jcs.auxiliary.disk.jisp.behavior.IJISPCacheService;
import org.apache.jcs.auxiliary.disk.PurgatoryElement;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.CacheInfo;
import org.apache.jcs.engine.CacheConstants;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Store recent arivals in a temporary queue. Used to queue up update requests
 * to the underlying cache. These requests will be processed in their order of
 * arrival via the cache event queue processor. Note: There is a a problem
 * lurking in all queued distributed systems, where repopulzation of memory can
 * occur dispite a removal if the tow arrive in different order than issued. The
 * possible solution to never delete, but only invlidate. The cache can act ina
 * cvs like mode, enforcing versions of element, checking to see if a newer
 * version exists adding. So a remote put could amke sure a new version hasn't
 * arrived. Invalidaions would have version. We could avoid spooling these since
 * it will have been a while. This will be called garuanteed mode and can be
 * added to any listener.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class JISPCacheNoWaitBuffer implements ICache
{
    private final static Log log =
        LogFactory.getLog( JISPCacheNoWaitBuffer.class );

    /** Description of the Field */
    protected Hashtable purgatory = new Hashtable();

    private int purgHits = 0;

    private IJISPCacheAttributes cattr;
    private JISPCache cache;
    private ICacheEventQueue q;

    private String source_id = "org.apache.jcs.auxiliary.disk.jisp.JISPCacheNoWaitBuffer";


    /**
     * Gets the sourceId attribute of the JISPCacheNoWaitBuffer object
     *
     * @return The sourceId value
     */
    public Serializable getSourceId()
    {
        return this.source_id;
    }


    /**
     * Constructs with the given disk cache, and fires up an event queue for
     * aysnchronous processing.
     *
     * @param cattr
     */
    public JISPCacheNoWaitBuffer( IJISPCacheAttributes cattr )
    {
        cache = new JISPCache( this, cattr );
        this.cattr = cattr;
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), CacheInfo.listenerId, cache.getCacheName() );

        // need each no wait to handle each of its real updates and removes, since there may
        // be more than one per cache?  alternativve is to have the cache
        // perform updates using a different method that spcifies the listener
        //this.q = new CacheEventQueue(new CacheAdaptor(this), JISPCacheInfo.listenerId, cache.getCacheName());
        if ( cache.getStatus() == CacheConstants.STATUS_ERROR )
        {
            log.error( "destroying queue" );
            q.destroy();
        }
    }


    /** Adds a put request to the disk cache. */
    public void put( Serializable key, Serializable value )
        throws IOException
    {
        put( key, value, null );
    }


    /** Description of the Method */
    public void put( Serializable key, Serializable value, IElementAttributes attr )
        throws IOException
    {
        try
        {
            CacheElement ce = new CacheElement( cache.getCacheName(), key, value );
            ce.setElementAttributes( attr );
            update( ce );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
    }


    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        try
        {
            log.debug( "putting in purgatory" );

            PurgatoryElement pe = new PurgatoryElement( ce );
            pe.setSpoolable( true );
            q.addPutEvent( ( ICacheElement ) pe );

            //q.addPutEvent( ce );

            /*
             * / may be too slow
             * IDiskElement ide = new DiskElement( ce );
             * ide.setElementAttributes( ce.getElementAttributes() );
             * purgatory.put( ide.getKey(), ide );
             * /CacheElement ice = new CacheElement(ce.getCacheName(), ce.getKey(), ce.getVal() );
             * /ice.setElementAttributes( ce.getElementAttributes() );
             * ide.setIsSpoolable( true );
             * q.addPutEvent( ide );
             */
        }
        catch ( IOException ex )
        {
            log.error( ex );
            // should we destroy purgatory.  it will swell
            q.destroy();
        }
    }


    /** Synchronously reads from the disk cache. */
    public Serializable get( Serializable key )
    {
        return get( key, true );
    }


    /** Description of the Method */
    public Serializable get( Serializable key, boolean container )
    {
        //IDiskElement ide = (IDiskElement)purgatory.get( key );
        PurgatoryElement pe = ( PurgatoryElement ) purgatory.get( key );
        //if ( ide != null ) {
        if ( pe != null )
        {
            purgHits++;
            if ( log.isDebugEnabled() )
            {
                if ( purgHits % 100 == 0 )
                {
                    log.debug( "purgatory hits = " + purgHits );
                }
            }

            pe.setSpoolable( false );

            log.debug( "found in purgatory" );

            if ( container )
            {
                purgatory.remove( key );

                return pe.getCacheElement();
            }
            else
            {
                purgatory.remove( key );

                return ( Serializable ) pe.getCacheElement().getVal();
            }
        }
        try
        {
            return cache.get( key );
        }
        catch ( Exception ex )
        {
            q.destroy();
            // not sure we should do this.  What about purgatory?
            // must assume that we will not loose disk access
            // can make a repairer, but it complicates purgatory.
        }
        return null;
    }


    /** Adds a remove request to the disk cache. */
    public boolean remove( Serializable key )
    {
        purgatory.remove( key );
        try
        {
            q.addRemoveEvent( key );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
        return false;
    }


    /** Adds a removeAll request to the disk cache. */
    public void removeAll()
    {

        Hashtable temp = purgatory;
        purgatory = new Hashtable();
        temp = null;

        try
        {
            q.addRemoveAllEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
    }


    /** Adds a dispose request to the disk cache. */
    public void dispose()
    {
        cache.dispose();
        // may loose the end of the queue, need to be more graceful
        q.destroy();
        /*
         * try {
         * q.addDisposeEvent();
         * } catch(IOException ex) {
         * log.error(ex);
         * q.destroy();
         * }
         */
    }


    /**
     * No disk invokation.
     *
     * @return The stats value
     */
    public String getStats()
    {
        return cache.getStats();
    }


    /**
     * No disk invokation.
     *
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }


    /**
     * No disk invokation.
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return cache.getCacheType();
    }


    /**
     * Returns the asyn cache status. An error status indicates either the disk
     * connection is not available, or the asyn queue has been unexpectedly
     * destroyed. No disk invokation.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return q.isAlive() ? cache.getStatus() : CacheConstants.STATUS_ERROR;
    }


    /**
     * Gets the cacheName attribute of the JISPCacheNoWaitBuffer object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cache.getCacheName();
    }


    /**
     * NOT USED NOW Replaces the disk cache service handle with the given handle
     * and reset the event queue by starting up a new instance.
     */
    public void fixCache( IJISPCacheService disk )
    {
        //cache.fixCache(disk);
        resetEventQ();
        return;
    }


    /**
     * Resets the event q by first destroying the existing one and starting up
     * new one.
     */
    public void resetEventQ()
    {
        if ( q.isAlive() )
        {
            q.destroy();
        }
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), CacheInfo.listenerId, cache.getCacheName() );
    }


    /** Description of the Method */
    public String toString()
    {
        return "JISPCacheNoWaitBuffer: " + cache.toString();
    }
}
