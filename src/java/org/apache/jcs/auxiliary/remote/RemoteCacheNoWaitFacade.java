package org.apache.jcs.auxiliary.remote;

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
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;

/**
 * Used to provide access to multiple services under nowait protection. factory
 * should construct NoWaitFacade to give to the composite cache out of caches it
 * constructs from the varies manager to lateral services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheNoWaitFacade implements AuxiliaryCache
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheNoWaitFacade.class );

    /** Description of the Field */
    public RemoteCacheNoWait[] noWaits;

    private String cacheName;

    // holds failover and cluster information
    RemoteCacheAttributes rca;

    /**
     * Gets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade
     * object
     *
     * @return The remoteCacheAttributes value
     */
    public RemoteCacheAttributes getRemoteCacheAttributes()
    {
        return rca;
    }

    /**
     * Sets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade
     * object
     *
     * @param rca The new remoteCacheAttributes value
     */
    public void setRemoteCacheAttributes( RemoteCacheAttributes rca )
    {
        this.rca = rca;
    }

    /**
     * Constructs with the given remote cache, and fires events to any
     * listeners.
     *
     * @param noWaits
     * @param rca
     */
    public RemoteCacheNoWaitFacade( RemoteCacheNoWait[] noWaits, RemoteCacheAttributes rca )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "CONSTRUCTING NO WAIT FACADE" );
        }
        this.noWaits = noWaits;
        this.rca = rca;
        this.cacheName = rca.getCacheName();
    }

    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "updating through cache facade, noWaits.length = " + noWaits.length );
        }
        int i = 0;
        try
        {
            for ( ; i < noWaits.length; i++ )
            {
                noWaits[ i ].update( ce );
                // an initial move into a zombie will lock this to primary
                // recovery.  will not discover other servers until primary reconnect
                // and subsequent error
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
            // can handle failover here?  Is it safe to try the others?
            // check to see it the noWait is now a zombie
            // if it is a zombie, then move to the next in the failover list
            // will need to keep them in order or a count
            failover( i );
            // should start a failover thread
            // should probably only failover if there is only one in the noWait list
            // should start a background thread to set the original as the primary
            // if we are in failover state
        }
    }

    /** Synchronously reads from the lateral cache. */
    public ICacheElement get( Serializable key )
    {
        for ( int i = 0; i < noWaits.length; i++ )
        {
            try
            {
                Object obj = noWaits[ i ].get( key );
                if ( obj != null )
                {
                    return ( ICacheElement ) obj;
                }
            }
            catch ( Exception ex )
            {
                log.debug( "Failed to get." );
            }
            return null;
        }
        return null;
    }

    /**
     * Gets the set of keys of objects currently in the group
     */
    public Set getGroupKeys(String group)
    {
        HashSet allKeys = new HashSet();
        for ( int i = 0; i < noWaits.length; i++ )
        {
            AuxiliaryCache aux = noWaits[i];
            if ( aux != null )
            {
                allKeys.addAll(aux.getGroupKeys(group));
            }
        }
        return allKeys;
    }


    /** Adds a remove request to the lateral cache. */
    public boolean remove( Serializable key )
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[ i ].remove( key );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
        return false;
    }

    /** Adds a removeAll request to the lateral cache. */
    public void removeAll()
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[ i ].removeAll();
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    /** Adds a dispose request to the lateral cache. */
    public void dispose()
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[ i ].dispose();
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    /**
     * No lateral invokation.
     *
     * @return The size value
     */
    public int getSize()
    {
        return 0;
        //cache.getSize();
    }

    /**
     * Gets the cacheType attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.REMOTE_CACHE;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return "";
        //cache.getCacheName();
    }


    // need to do something with this
    /**
     * Gets the status attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The status value
     */
    public int getStatus()
    {
        return 0;
        //q.isAlive() ? cache.getStatus() : cache.STATUS_ERROR;
    }

    /** Description of the Method */
    public String toString()
    {
        return "RemoteCacheNoWaitFacade: " + cacheName + ", rca = " + rca;
    }

    /** Description of the Method */
    protected void failover( int i )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "in failover for " + i );
        }
        //if ( noWaits.length == 1 ) {
        if ( rca.getRemoteType() == rca.LOCAL )
        {
            if ( noWaits[ i ].getStatus() == CacheConstants.STATUS_ERROR )
            {
                // start failover, primary recovery process
                RemoteCacheFailoverRunner runner = new RemoteCacheFailoverRunner( this );
                // If the returned monitor is null, it means it's already started elsewhere.
                if ( runner != null )
                {
                    runner.notifyError();
                    Thread t = new Thread( runner );
                    t.setDaemon( true );
                    t.start();
                }
            }
            else
            {
                log.info( "the noWait is not in error" );
            }
        }
    }

}
