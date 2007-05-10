package org.apache.jcs.auxiliary.remote;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Used to provide access to multiple services under nowait protection. Factory should construct
 * NoWaitFacade to give to the composite cache out of caches it constructs from the varies manager
 * to lateral services.
 * <p>
 * Typically, we only connect to one remote server per facade.  We use a list of one RemoteCacheNoWait.
 */
public class RemoteCacheNoWaitFacade
    implements AuxiliaryCache
{
    private static final long serialVersionUID = -4529970797620747110L;

    private final static Log log = LogFactory.getLog( RemoteCacheNoWaitFacade.class );

    /** The connection to a remote server, or a zombie. */
    public RemoteCacheNoWait[] noWaits;

    private String cacheName;

    /** holds failover and cluster information */
    protected RemoteCacheAttributes remoteCacheAttributes;

    private ICompositeCacheManager cacheMgr;

    /**
     * Gets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade object
     * <p>
     * @return The remoteCacheAttributes value
     */
    public RemoteCacheAttributes getRemoteCacheAttributes()
    {
        return remoteCacheAttributes;
    }

    /**
     * Sets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade object.
     * <p>
     * @param rca The new remoteCacheAttributes value
     */
    public void setRemoteCacheAttributes( RemoteCacheAttributes rca )
    {
        this.remoteCacheAttributes = rca;
    }

    /**
     * Constructs with the given remote cache, and fires events to any listeners.
     * <p>
     * @param noWaits
     * @param rca
     * @param cacheMgr
     */
    public RemoteCacheNoWaitFacade( RemoteCacheNoWait[] noWaits, RemoteCacheAttributes rca,
                                   ICompositeCacheManager cacheMgr )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "CONSTRUCTING NO WAIT FACADE" );
        }
        this.noWaits = noWaits;
        this.remoteCacheAttributes = rca;
        this.cacheName = rca.getCacheName();
        this.cacheMgr = cacheMgr;
    }

    /**
     * Put an element in the cache.
     * <p>
     * @param ce
     * @throws IOException
     */
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
                noWaits[i].update( ce );
                // an initial move into a zombie will lock this to primary
                // recovery. will not discover other servers until primary
                // reconnect
                // and subsequent error
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
            // can handle failover here? Is it safe to try the others?
            // check to see it the noWait is now a zombie
            // if it is a zombie, then move to the next in the failover list
            // will need to keep them in order or a count
            failover( i );
            // should start a failover thread
            // should probably only failover if there is only one in the noWait
            // list
            // should start a background thread to set the original as the
            // primary
            // if we are in failover state
        }
    }

    /**
     * Synchronously reads from the remote cache.
     * <p>
     * @param key
     * @return Either an ICacheElement or null if it is not found.
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
                    return (ICacheElement) obj;
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
     * Gets the set of keys of objects currently in the group.
     * <p>
     * @param group
     * @return
     * @throws IOException
     */
    public Set getGroupKeys( String group )
        throws IOException
    {
        HashSet allKeys = new HashSet();
        for ( int i = 0; i < noWaits.length; i++ )
        {
            AuxiliaryCache aux = noWaits[i];
            if ( aux != null )
            {
                allKeys.addAll( aux.getGroupKeys( group ) );
            }
        }
        return allKeys;
    }

    /**
     * Adds a remove request to the remote cache.
     * <p>
     * @param key
     * @return wether or not it was removed, right now it return false.
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
            log.error( "Problem in dispose.", ex );
        }
    }

    /**
     * No lateral invocation.
     * @return The size value
     */
    public int getSize()
    {
        return 0;
        // cache.getSize();
    }

    /**
     * Gets the cacheType attribute of the RemoteCacheNoWaitFacade object.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.REMOTE_CACHE;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWaitFacade object.
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return remoteCacheAttributes.getCacheName();
    }

    /**
     * Gets the status attribute of the RemoteCacheNoWaitFacade object
     * <p>
     * Return ALIVE if any are alive.
     * <p>
     * @return The status value
     */
    public int getStatus()
    {
        for ( int i = 0; i < noWaits.length; i++ )
        {
            if ( noWaits[i].getStatus() == CacheConstants.STATUS_ALIVE )
            {
                return CacheConstants.STATUS_ALIVE;
            }
        }
        return 0;
    }

    /**
     * String form of some of the configuratin information for the remote cache.
     * <p>
     * @return Some info for logging.
     */
    public String toString()
    {
        return "RemoteCacheNoWaitFacade: " + cacheName + ", rca = " + remoteCacheAttributes;
    }

    /**
     * Begin the failover process if this is a local cache. Clustered remote caches do not failover.
     * <p>
     * @param i The no wait in error.
     */
    protected void failover( int i )
    {
        if ( log.isDebugEnabled() )
        {
            log.info( "in failover for " + i );
        }

        if ( remoteCacheAttributes.getRemoteType() == RemoteCacheAttributes.LOCAL )
        {
            if ( noWaits[i].getStatus() == CacheConstants.STATUS_ERROR )
            {
                // start failover, primary recovery process
                RemoteCacheFailoverRunner runner = new RemoteCacheFailoverRunner( this, cacheMgr );
                // If the returned monitor is null, it means it's already
                // started elsewhere.
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
                if ( log.isInfoEnabled() )
                {
                    log.info( "The noWait is not in error" );
                }
            }
        }
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.remoteCacheAttributes;
    }

    /**
     * getStats
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Remote Cache No Wait Facade" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        if ( noWaits != null )
        {
            se = new StatElement();
            se.setName( "Number of No Waits" );
            se.setData( "" + noWaits.length );
            elems.add( se );

            for ( int i = 0; i < noWaits.length; i++ )
            {
                // get the stats from the super too
                // get as array, convert to list, add list to our outer list
                IStats sStats = noWaits[i].getStatistics();
                IStatElement[] sSEs = sStats.getStatElements();
                List sL = Arrays.asList( sSEs );
                elems.addAll( sL );
            }
        }

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }
}
