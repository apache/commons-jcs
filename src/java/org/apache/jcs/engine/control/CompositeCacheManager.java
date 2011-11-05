package org.apache.jcs.engine.control;

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
import java.io.InputStream;
import java.io.Serializable;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.IShutdownObservable;
import org.apache.jcs.engine.behavior.IShutdownObserver;
import org.apache.jcs.engine.stats.CacheStats;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.utils.threadpool.ThreadPoolManager;

/**
 * Manages a composite cache. This provides access to caches and is the primary way to shutdown the
 * caching system as a whole.
 * <p>
 * The composite cache manager is responsible for creating / configuring cache regions. It serves as
 * a factory for the ComositeCache class. The CompositeCache is the core of JCS, the hub for various
 * auxiliaries.
 * <p>
 * It is recommended that you use the JCS convenience class for all cache access.
 */
public class CompositeCacheManager
    implements IRemoteCacheConstants, Serializable, ICompositeCacheManager, IShutdownObservable
{
    /** Don't change */
    private static final long serialVersionUID = 7598584393134401756L;

    /** The logger */
    protected final static Log log = LogFactory.getLog( CompositeCacheManager.class );

    /** Caches managed by this cache manager */
    protected Hashtable<String, ICache> caches = new Hashtable<String, ICache>();

    /** Internal system caches for this cache manager */
    protected Hashtable<String, ICache> systemCaches = new Hashtable<String, ICache>();

    /** Number of clients accessing this cache manager */
    private int clients;

    /** Default cache attributes for this cache manager */
    protected ICompositeCacheAttributes defaultCacheAttr = new CompositeCacheAttributes();

    /** Default element attributes for this cache manager */
    protected IElementAttributes defaultElementAttr = new ElementAttributes();

    /** Used to keep track of configured auxiliaries */
    protected Hashtable<String, AuxiliaryCacheFactory> auxiliaryFactoryRegistry =
        new Hashtable<String, AuxiliaryCacheFactory>( 11 );

    /** Used to keep track of attributes for auxiliaries. */
    protected Hashtable<String, AuxiliaryCacheAttributes> auxiliaryAttributeRegistry =
        new Hashtable<String, AuxiliaryCacheAttributes>( 11 );

    /** Properties with which this manager was configured. This is exposed for other managers. */
    private Properties configurationProperties;

    /** The default auxiliary caches to be used if not preconfigured */
    protected String defaultAuxValues;

    /** The Singleton Instance */
    protected static CompositeCacheManager instance;

    /** The prefix of relevant system properties */
    private static final String SYSTEM_PROPERTY_KEY_PREFIX = "jcs";

    /** Should we use system property substitutions. */
    private static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = true;

    /** Once configured, you can force a recofiguration of sorts. */
    private static final boolean DEFAULT_FORCE_RECONFIGURATION = false;

    /** Those waiting for notification of a shutdown. */
    private final Set<IShutdownObserver> shutdownObservers = new HashSet<IShutdownObserver>();

    /** Indicates whether shutdown has been called. */
    private boolean isShutdown = false;

    /** Indicates whether configure has been called. */
    private boolean isConfigured = false;

    /**
     * Gets the CacheHub instance. For backward compatibility, if this creates the instance it will
     * attempt to configure it with the default configuration. If you want to configure from your
     * own source, use {@link #getUnconfiguredInstance}and then call {@link #configure}
     * <p>
     * @return CompositeCacheManager
     * @throws CacheException if the configuration cannot be loaded
     */
    public static synchronized CompositeCacheManager getInstance() throws CacheException
    {
        if ( instance == null )
        {
            log.debug( "Instance is null, creating with default config" );

            instance = createInstance();

            instance.configure();
        }

        instance.incrementClients();

        return instance;
    }

    /**
     * Initializes the cache manager using the props file for the given name.
     * <p>
     * @param propsFilename
     * @return CompositeCacheManager configured from the give propsFileName
     * @throws CacheException if the configuration cannot be loaded
     */
    public static synchronized CompositeCacheManager getInstance( String propsFilename ) throws CacheException
    {
        if ( instance == null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Instance is null, creating with default config [" + propsFilename + "]" );
            }

            instance = createInstance();

            instance.configure( propsFilename );
        }

        instance.incrementClients();

        return instance;
    }

    /**
     * Get a CacheHub instance which is not configured. If an instance already exists, it will be
     * returned.
     *<p>
     * @return CompositeCacheManager
     */
    public static synchronized CompositeCacheManager getUnconfiguredInstance()
    {
        if ( instance == null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Instance is null, returning unconfigured instance" );
            }

            instance = createInstance();
        }

        instance.incrementClients();

        return instance;
    }

    /**
     * Simple factory method, must override in subclasses so getInstance creates / returns the
     * correct object.
     * <p>
     * @return CompositeCacheManager
     */
    protected static CompositeCacheManager createInstance()
    {
        return new CompositeCacheManager();
    }

    /** Creates a shutdown hook */
    protected CompositeCacheManager()
    {
        ShutdownHook shutdownHook = new ShutdownHook();
        try
        {
            Runtime.getRuntime().addShutdownHook( shutdownHook );
        }
        catch ( AccessControlException e )
        {
            log.error( "Could not register shutdown hook.", e );
        }
    }

    /**
     * Configure with default properties file
     * @throws CacheException if the configuration cannot be loaded
     */
    public void configure() throws CacheException
    {
        configure( CacheConstants.DEFAULT_CONFIG );
    }

    /**
     * Configure from specific properties file.
     * <p>
     * @param propFile Path <u>within classpath </u> to load configuration from
     * @throws CacheException if the configuration cannot be loaded
     */
    public void configure( String propFile ) throws CacheException
    {
        log.info( "Creating cache manager from config file: " + propFile );

        Properties props = new Properties();

        InputStream is = getClass().getResourceAsStream( propFile );

        if ( is != null )
        {
            try
            {
                props.load( is );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "File [" + propFile + "] contained " + props.size() + " properties" );
                }
            }
            catch ( IOException ex )
            {
                throw new CacheException("Failed to load properties for name [" + propFile + "]", ex);
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch ( IOException ignore )
                {
                    // Ignored
                }
            }
        }
        else
        {
            throw new CacheException( "Failed to read configuration file [" + propFile + "]" );
        }

        configure( props );
    }

    /**
     * Configure from properties object.
     * <p>
     * This method will call configure, instructing it to use system properties as a default.
     * @param props
     */
    public void configure( Properties props )
    {
        configure( props, DEFAULT_USE_SYSTEM_PROPERTIES );
    }

    /**
     * Configure from properties object, overriding with values from the system properties if
     * instructed.
     * <p>
     * You can override a specific value by passing in a system property:
     * <p>
     * For example, you could override this value in the cache.ccf file by starting up your program
     * with the argument: -Djcs.auxiliary.LTCP.attributes.TcpListenerPort=1111
     * <p>
     * @param props
     * @param useSystemProperties -- if true, values starting with jcs will be put into the props
     *            file prior to configuring the cache.
     */
    public void configure( Properties props, boolean useSystemProperties )
    {
        configure( props, useSystemProperties, DEFAULT_FORCE_RECONFIGURATION );
    }

    /**
     * Configure from properties object, overriding with values from the system properties if
     * instructed.
     * <p>
     * You can override a specific value by passing in a system property:
     * <p>
     * For example, you could override this value in the cache.ccf file by starting up your program
     * with the argument: -Djcs.auxiliary.LTCP.attributes.TcpListenerPort=1111
     * <p>
     * @param props
     * @param useSystemProperties -- if true, values starting with jcs will be put into the props
     *            file prior to configuring the cache.
     * @param forceReconfiguration - if the manager is already configured, we will try again. This
     *            may not work properly.
     */
    public synchronized void configure( Properties props, boolean useSystemProperties, boolean forceReconfiguration )
    {
        if ( props == null )
        {
            log.error( "No properties found.  Please configure the cache correctly." );
            return;
        }

        if ( isConfigured )
        {
            if ( !forceReconfiguration )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Configure called after the manager has been configured.  "
                        + "Force reconfiguration is false.  Doing nothing" );
                }
                return;
            }
            else
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Configure called after the manager has been configured.  "
                        + "Force reconfiguration is true.  Reconfiguring as best we can." );
                }
            }
        }
        if ( useSystemProperties )
        {
            overrideWithSystemProperties( props );
        }
        doConfigure( props );
    }

    /**
     * Any property values will be replaced with system property values that match the key.
     * <p>
     * TODO move to a utility.
     * <p>
     * @param props
     */
    private static void overrideWithSystemProperties( Properties props )
    {
        // override any setting with values from the system properties.
        Properties sysProps = System.getProperties();
        Set<Object> keys = sysProps.keySet();
        Iterator<Object> keyIt = keys.iterator();
        while ( keyIt.hasNext() )
        {
            String key = (String) keyIt.next();
            if ( key.startsWith( SYSTEM_PROPERTY_KEY_PREFIX ) )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Using system property [[" + key + "] [" + sysProps.getProperty( key ) + "]]" );
                }
                props.put( key, sysProps.getProperty( key ) );
            }
        }
    }

    /**
     * Configure the cache using the supplied properties.
     * <p>
     * @param props assumed not null
     */
    private void doConfigure( Properties props )
    {
        // We will expose this for managers that need raw properties.
        this.configurationProperties = props;

        // set the props value and then configure the ThreadPoolManager
        ThreadPoolManager.setProps( props );
        ThreadPoolManager poolMgr = ThreadPoolManager.getInstance();
        if ( log.isDebugEnabled() )
        {
            log.debug( "ThreadPoolManager = " + poolMgr );
        }

        // configure the cache
        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( this );

        configurator.doConfigure( props );

        isConfigured = true;
    }

    /**
     * Gets the defaultCacheAttributes attribute of the CacheHub object
     * <p>
     * @return The defaultCacheAttributes value
     */
    public ICompositeCacheAttributes getDefaultCacheAttributes()
    {
        return this.defaultCacheAttr.copy();
    }

    /**
     * Sets the defaultCacheAttributes attribute of the CacheHub object
     * <p>
     * @param icca The new defaultCacheAttributes value
     */
    public void setDefaultCacheAttributes( ICompositeCacheAttributes icca )
    {
        this.defaultCacheAttr = icca;
    }

    /**
     * Sets the defaultElementAttributes attribute of the CacheHub object
     * <p>
     * @param iea The new defaultElementAttributes value
     */
    public void setDefaultElementAttributes( IElementAttributes iea )
    {
        this.defaultElementAttr = iea;
    }

    /**
     * Gets the defaultElementAttributes attribute of the CacheHub object
     * <p>
     * @return The defaultElementAttributes value
     */
    public IElementAttributes getDefaultElementAttributes()
    {
        return this.defaultElementAttr.copy();
    }

    /**
     * Gets the cache attribute of the CacheHub object
     * <p>
     * @param cacheName
     * @return CompositeCache -- the cache region controller
     */
    public CompositeCache getCache( String cacheName )
    {
        return getCache( cacheName, this.defaultCacheAttr.copy() );
    }

    /**
     * Gets the cache attribute of the CacheHub object
     * <p>
     * @param cacheName
     * @param cattr
     * @return CompositeCache
     */
    public CompositeCache getCache( String cacheName, ICompositeCacheAttributes cattr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, this.defaultElementAttr );
    }

    /**
     * Gets the cache attribute of the CacheHub object
     * <p>
     * @param cacheName
     * @param cattr
     * @param attr
     * @return CompositeCache
     */
    public CompositeCache getCache( String cacheName, ICompositeCacheAttributes cattr, IElementAttributes attr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, attr );
    }

    /**
     * Gets the cache attribute of the CacheHub object
     * <p>
     * @param cattr
     * @return CompositeCache
     */
    public CompositeCache getCache( ICompositeCacheAttributes cattr )
    {
        return getCache( cattr, this.defaultElementAttr );
    }

    /**
     * If the cache has already been created, then the CacheAttributes and the element Attributes
     * will be ignored. Currently there is no overiding the CacheAttributes once it is set up. You
     * can change the default ElementAttributes for a region later.
     * <p>
     * Overriding the default elemental attributes will require changing the way the attributes are
     * assigned to elements. Get cache creates a cache with defaults if none are specified. We might
     * want to create separate method for creating/getting. . .
     * <p>
     * @param cattr
     * @param attr
     * @return CompositeCache
     */
    public CompositeCache getCache( ICompositeCacheAttributes cattr, IElementAttributes attr )
    {
        CompositeCache cache;

        if ( log.isDebugEnabled() )
        {
            log.debug( "attr = " + attr );
        }

        synchronized ( caches )
        {
            cache = (CompositeCache) caches.get( cattr.getCacheName() );
            if ( cache == null )
            {
                cattr.setCacheName( cattr.getCacheName() );

                CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( this );

                cache = configurator.parseRegion( this.getConfigurationProperties(), cattr.getCacheName(),
                                                  this.defaultAuxValues, cattr );

                caches.put( cattr.getCacheName(), cache );
            }
        }

        return cache;
    }

    /**
     * @param name
     */
    public void freeCache( String name )
    {
        freeCache( name, false );
    }

    /**
     * @param name
     * @param fromRemote
     */
    public void freeCache( String name, boolean fromRemote )
    {
        CompositeCache cache = (CompositeCache) caches.remove( name );

        if ( cache != null )
        {
            cache.dispose( fromRemote );
        }
    }

    /**
     * Calls freeCache on all regions
     */
    public void shutDown()
    {
        isShutdown = true;

        // notify any observers
        synchronized ( shutdownObservers )
        {
            // We don't need to worry about locking the set.
            // since this is a shutdown command, nor do we need
            // to queue these up.
            for (IShutdownObserver observer : shutdownObservers)
            {
                observer.shutdown();
            }
        }

        // do the traditional shutdown of the regions.
        String[] names = getCacheNames();
        int len = names.length;
        for ( int i = 0; i < len; i++ )
        {
            String name = names[i];
            freeCache( name );
        }
    }

    /** */
    protected void incrementClients()
    {
        clients++;
    }

    /** */
    public void release()
    {
        release( false );
    }

    /**
     * @param fromRemote
     */
    private void release( boolean fromRemote )
    {
        synchronized ( CompositeCacheManager.class )
        {
            // Wait until called by the last client
            if ( --clients > 0 )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Release called, but " + clients + " remain" );
                    return;
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Last client called release. There are " + caches.size() + " caches which will be disposed" );
            }

            for (ICache c : caches.values() )
            {
                CompositeCache cache = (CompositeCache) c;

                if ( cache != null )
                {
                    cache.dispose( fromRemote );
                }
            }
        }
    }

    /**
     * Returns a list of the current cache names.
     * @return String[]
     */
    public String[] getCacheNames()
    {
        String[] list = new String[caches.size()];
        int i = 0;
        for (String key : caches.keySet())
        {
            list[i++] = key;
        }
        return list;
    }

    /**
     * @return ICacheType.CACHE_HUB
     */
    public int getCacheType()
    {
        return ICacheType.CACHE_HUB;
    }

    /**
     * @return ICompositeCacheAttributes
     */
    public ICompositeCacheAttributes getDefaultCattr()
    {
        return this.defaultCacheAttr;
    }

    /**
     * @param auxFac
     */
    void registryFacPut( AuxiliaryCacheFactory auxFac )
    {
        auxiliaryFactoryRegistry.put( auxFac.getName(), auxFac );
    }

    /**
     * @param name
     * @return AuxiliaryCacheFactory
     */
    AuxiliaryCacheFactory registryFacGet( String name )
    {
        return auxiliaryFactoryRegistry.get( name );
    }

    /**
     * @param auxAttr
     */
    void registryAttrPut( AuxiliaryCacheAttributes auxAttr )
    {
        auxiliaryAttributeRegistry.put( auxAttr.getName(), auxAttr );
    }

    /**
     * @param name
     * @return AuxiliaryCacheAttributes
     */
    AuxiliaryCacheAttributes registryAttrGet( String name )
    {
        return auxiliaryAttributeRegistry.get( name );
    }

    /**
     * Gets stats for debugging. This calls gets statistics and then puts all the results in a
     * string. This returns data for all regions.
     * <p>
     * @return String
     */
    public String getStats()
    {
        ICacheStats[] stats = getStatistics();
        if ( stats == null )
        {
            return "NONE";
        }

        // force the array elements into a string.
        StringBuffer buf = new StringBuffer();
        int statsLen = stats.length;
        for ( int i = 0; i < statsLen; i++ )
        {
            buf.append( "\n---------------------------\n" );
            buf.append( stats[i] );
        }
        return buf.toString();
    }

    /**
     * This returns data gathered for all regions and all the auxiliaries they currently uses.
     * <p>
     * @return ICacheStats[]
     */
    public ICacheStats[] getStatistics()
    {
        ArrayList<ICacheStats> cacheStats = new ArrayList<ICacheStats>();
        for (ICache c :  caches.values())
        {
            CompositeCache cache = (CompositeCache) c;
            if ( cache != null )
            {
                cacheStats.add( cache.getStatistics() );
            }
        }
        ICacheStats[] stats = cacheStats.toArray( new CacheStats[0] );
        return stats;
    }

    /**
     * Perhaps the composite cache itself should be the observable object. It doesn't make much of a
     * difference. There are some problems with region by region shutdown. Some auxiliaries are
     * global. They will need to track when every region has shutdown before doing things like
     * closing the socket with a lateral.
     * <p>
     * @param observer
     */
    public void registerShutdownObserver( IShutdownObserver observer )
    {
        // synchronized to take care of iteration safety
        // during shutdown.
        synchronized ( shutdownObservers )
        {
            // the set will take care of duplication protection
            shutdownObservers.add( observer );
        }
    }

    /**
     * @param observer
     */
    public void deregisterShutdownObserver( IShutdownObserver observer )
    {
        synchronized ( shutdownObservers )
        {
            shutdownObservers.remove( observer );
        }
    }

    /**
     * This is exposed so other manager can get access to the props.
     * <p>
     * @param props
     */
    public void setConfigurationProperties( Properties props )
    {
        this.configurationProperties = props;
    }

    /**
     * This is exposed so other manager can get access to the props.
     * <p>
     * @return the configurationProperties
     */
    public Properties getConfigurationProperties()
    {
        return configurationProperties;
    }

    /**
     * @return the isShutdown
     */
    public boolean isShutdown()
    {
        return isShutdown;
    }

    /**
     * @return the isConfigured
     */
    public boolean isConfigured()
    {
        return isConfigured;
    }

    /**
     * Called on shutdown. This gives use a chance to store the keys and to optimize even if the
     * cache manager's shutdown method was not called manually.
     */
    class ShutdownHook
        extends Thread
    {
        /**
         * This will persist the keys on shutdown.
         * <p>
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            if ( !isShutdown() )
            {
                log.info( "Shutdown hook activated.  Shutdown was not called.  Shutting down JCS." );
                shutDown();
            }
        }
    }
}
