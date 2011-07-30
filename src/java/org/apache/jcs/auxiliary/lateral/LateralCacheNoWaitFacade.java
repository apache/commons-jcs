package org.apache.jcs.auxiliary.lateral;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AbstractAuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Used to provide access to multiple services under nowait protection. Composite factory should
 * construct LateralCacheNoWaitFacade to give to the composite cache out of caches it constructs
 * from the varies manager to lateral services. Perhaps the lateralcache factory should be able to
 * do this.
 */
public class LateralCacheNoWaitFacade
    extends AbstractAuxiliaryCache
{
    /** Don't change */
    private static final long serialVersionUID = -9047687810358008955L;

    /** The logger */
    private final static Log log = LogFactory.getLog( LateralCacheNoWaitFacade.class );

    /** The queuing facade to the client. */
    public LateralCacheNoWait[] noWaits;

    /** The region name */
    private final String cacheName;

    /** User configurable attributes. */
    private final ILateralCacheAttributes lateralCacheAttributes;

    /**
     * Constructs with the given lateral cache, and fires events to any listeners.
     * <p>
     * @param noWaits
     * @param cattr
     */
    public LateralCacheNoWaitFacade( LateralCacheNoWait[] noWaits, ILateralCacheAttributes cattr )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "CONSTRUCTING NO WAIT FACADE" );
        }
        this.noWaits = noWaits;
        this.cacheName = cattr.getCacheName();
        this.lateralCacheAttributes = cattr;
    }

    /**
     * Tells you if the no wait is in the list or not.
     * <p>
     * @param noWait
     * @return true if the noWait is in the list.
     */
    public boolean containsNoWait( LateralCacheNoWait noWait )
    {
        for ( int i = 0; i < noWaits.length; i++ )
        {
            // we know noWait isn't null
            if ( noWait.equals( noWaits[i] ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a no wait to the list if it isn't already in the list.
     * <p>
     * @param noWait
     * @return true if it wasn't already contained
     */
    public synchronized boolean addNoWait( LateralCacheNoWait noWait )
    {
        if ( noWait == null )
        {
            return false;
        }

        if ( containsNoWait( noWait ) )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "No Wait already contained, [" + noWait + "]" );
            }
            return false;
        }

        LateralCacheNoWait[] newArray = new LateralCacheNoWait[noWaits.length + 1];

        System.arraycopy( noWaits, 0, newArray, 0, noWaits.length );

        // set the last position to the new noWait
        newArray[noWaits.length] = noWait;

        noWaits = newArray;

        return true;
    }

    /**
     * Removes a no wait from the list if it is already there.
     * <p>
     * @param noWait
     * @return true if it was already in the array
     */
    public synchronized boolean removeNoWait( LateralCacheNoWait noWait )
    {
        if ( noWait == null )
        {
            return false;
        }

        int position = -1;
        for ( int i = 0; i < noWaits.length; i++ )
        {
            // we know noWait isn't null
            if ( noWait.equals( noWaits[i] ) )
            {
                position = i;
                break;
            }
        }

        if ( position == -1 )
        {
            return false;
        }

        LateralCacheNoWait[] newArray = new LateralCacheNoWait[noWaits.length - 1];

        System.arraycopy( noWaits, 0, newArray, 0, position );
        if ( noWaits.length != position )
        {
            System.arraycopy( noWaits, position + 1, newArray, position, noWaits.length - position - 1 );
        }
        noWaits = newArray;

        return true;
    }

    /**
     * @param ce
     * @throws IOException
     */
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
                noWaits[i].update( ce );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param key
     * @return ICacheElement
     */
    public ICacheElement get( Serializable key )
    {
        for ( int i = 0; i < noWaits.length; i++ )
        {
            try
            {
                Object obj = noWaits[i].get( key );

                if ( obj != null )
                {
                    // TODO: return after first success
                    // could do this simultaneously
                    // serious blocking risk here
                    return (ICacheElement) obj;
                }
            }
            catch ( Exception ex )
            {
                log.error( "Failed to get", ex );
            }
        }
        return null;
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    public Map<Serializable, ICacheElement> getMultiple(Set<Serializable> keys)
    {
        Map<Serializable, ICacheElement> elements = new HashMap<Serializable, ICacheElement>();

        if ( keys != null && !keys.isEmpty() )
        {
            for (Serializable key : keys)
            {
                ICacheElement element = get( key );

                if ( element != null )
                {
                    elements.put( key, element );
                }
            }
        }

        return elements;
    }

    /**
     * Synchronously reads from the lateral cache. Get a response from each! This will be slow.
     * Merge them.
     * <p>
     * @param pattern
     * @return ICacheElement
     */
    public Map<Serializable, ICacheElement> getMatching(String pattern)
    {
        Map<Serializable, ICacheElement> elements = new HashMap<Serializable, ICacheElement>();
        for ( int i = 0; i < noWaits.length; i++ )
        {
            try
            {
                elements.putAll( noWaits[i].getMatching( pattern ) );
            }
            catch ( Exception ex )
            {
                log.error( "Failed to get", ex );
            }
        }
        return elements;
    }

    /**
     * @param group
     * @return Set
     */
    public Set<Serializable> getGroupKeys( String group )
    {
        HashSet<Serializable> allKeys = new HashSet<Serializable>();
        for ( int i = 0; i < noWaits.length; i++ )
        {
            AuxiliaryCache aux = noWaits[i];
            if ( aux != null )
            {
                try
                {
                    allKeys.addAll( aux.getGroupKeys( group ) );
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
        return allKeys;
    }

    /**
     * Adds a remove request to the lateral cache.
     * <p>
     * @param key
     * @return always false.
     */
    public boolean remove( Serializable key )
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[i].remove( key );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
        return false;
    }

    /**
     * Adds a removeAll request to the lateral cache.
     */
    public void removeAll()
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[i].removeAll();
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
                noWaits[i].dispose();
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    /**
     * No lateral invocation.
     * @return The size value
     */
    public int getSize()
    {
        return 0;
        //cache.getSize();
    }

    /**
     * Gets the cacheType attribute of the LateralCacheNoWaitFacade object.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.LATERAL_CACHE;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWaitFacade object.
     * <p>
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
     * @return The status value
     */
    public int getStatus()
    {
        return 0;
        //q.isAlive() ? cache.getStatus() : cache.STATUS_ERROR;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.lateralCacheAttributes;
    }

    /**
     * @return "LateralCacheNoWaitFacade: " + cacheName;
     */
    @Override
    public String toString()
    {
        return "LateralCacheNoWaitFacade: " + cacheName;
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return "Lateral Cache No Wait";
    }

    /**
     * getStats
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * @return IStats
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Lateral Cache No Wait Facade" );

        ArrayList<IStatElement> elems = new ArrayList<IStatElement>();

        IStatElement se = null;

        if ( noWaits != null )
        {
            se = new StatElement();
            se.setName( "Number of No Waits" );
            se.setData( "" + noWaits.length );
            elems.add( se );

            for ( int i = 0; i < noWaits.length; i++ )
            {
                if ( noWaits[i] != null )
                {
                    // get the stats from the super too
                    // get as array, convert to list, add list to our outer list
                    IStats sStats = noWaits[i].getStatistics();
                    IStatElement[] sSEs = sStats.getStatElements();
                    List<IStatElement> sL = Arrays.asList( sSEs );
                    elems.addAll( sL );
                }
            }

        }

        // get an array and put them in the Stats object
        IStatElement[] ses = elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }
}
