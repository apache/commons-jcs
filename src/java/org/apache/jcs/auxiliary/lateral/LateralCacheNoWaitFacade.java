package org.apache.jcs.auxiliary.lateral;

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
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;

/**
 * Used to provide access to multiple services under nowait protection.
 * Composite factory should construct LateralCacheNoWaitFacade to give to the
 * composite cache out of caches it constructs from the varies manager to
 * lateral services. Perhaps the lateralcache factory should be able to do this.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheNoWaitFacade implements AuxiliaryCache
{
    private final static Log log =
        LogFactory.getLog( LateralCacheNoWaitFacade.class );

    /** Description of the Field */
    public LateralCacheNoWait[] noWaits;

    private String cacheName;

    /**
     * Constructs with the given lateral cache, and fires events to any
     * listeners.
     *
     * @param noWaits
     * @param cacheName
     */
    public LateralCacheNoWaitFacade( LateralCacheNoWait[] noWaits, String cacheName )
    {
        this.noWaits = noWaits;
        this.cacheName = cacheName;
    }

    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "updating through lateral cache facade, noWaits.length = " + noWaits.length );
        }
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[ i ].update( ce );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
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
                    // return after first success
                    // could do this simultaneously
                    // serious blocking risk here
                    return ( ICacheElement ) obj;
                }
            }
            catch ( Exception ex )
            {
                log.error( "Failed to get", ex );
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
     * Gets the cacheType attribute of the LateralCacheNoWaitFacade object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.LATERAL_CACHE;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWaitFacade object
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
     * Gets the status attribute of the LateralCacheNoWaitFacade object
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
        return "LateralCacheNoWaitFacade: " + cacheName;
    }
}
