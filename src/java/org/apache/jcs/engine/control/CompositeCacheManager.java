package org.apache.jcs.engine.control;

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
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.stats.CacheStats;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.utils.threadpool.ThreadPoolManager;

/** Manages a composite cache. */
public class CompositeCacheManager
    implements IRemoteCacheConstants, Serializable, ICompositeCacheManager
{
    private final static Log log = LogFactory.getLog( CompositeCacheManager.class );

    /** Caches managed by this cache manager */
    protected Hashtable caches = new Hashtable();

    /** Internal system caches for this cache manager */
    protected Hashtable systemCaches = new Hashtable();

    /** Number of clients accessing this cache manager */
    private int clients;

    /** Default cache attributes for this cache manager */
    protected ICompositeCacheAttributes defaultCacheAttr = new CompositeCacheAttributes();

    /** Default elemeent attributes for this cache manager */
    protected IElementAttributes defaultElementAttr = new ElementAttributes();

    /** Used to keep track of configured auxiliaries */
    protected Hashtable auxFacs = new Hashtable( 11 );

    /** ??? */
    protected Hashtable auxAttrs = new Hashtable( 11 );

    /** Properties with which this manager was configured */
    protected Properties props;

    /** The default auxiliary caches to be used if not preconfigured */
    protected String defaultAuxValues;

    /** The Singleton Instance */
    protected static CompositeCacheManager instance;

    private static final String SYSTEM_PROPERTY_KEY_PREFIX = "jcs";

    private static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = true;

    /**
     * Gets the CacheHub instance. For backward compatibility, if this creates
     * the instance it will attempt to configure it with the default
     * configuration. If you want to configure from your own source, use
     * {@link #getUnconfiguredInstance}and then call {@link #configure}
     */
    public static synchronized CompositeCacheManager getInstance()
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

    public static synchronized CompositeCacheManager getInstance( String propsFilename )
    {
        if ( instance == null )
        {
            log.debug( "Instance is null, creating with default config" );

            instance = createInstance();

            instance.configure( propsFilename );
        }

        instance.incrementClients();

        return instance;
    }

    /**
     * Get a CacheHub instance which is not configured. If an instance already
     * exists, it will be returned.
     */
    public static synchronized CompositeCacheManager getUnconfiguredInstance()
    {
        if ( instance == null )
        {
            log.debug( "Instance is null, creating with provided config" );

            instance = createInstance();
        }

        instance.incrementClients();

        return instance;
    }

    /**
     * Simple factory method, must override in subclasses so getInstance creates /
     * returns the correct object.
     */
    protected static CompositeCacheManager createInstance()
    {
        return new CompositeCacheManager();
    }

    /**
     * Configure with default properties file
     */
    public void configure()
    {
        configure( CacheConstants.DEFAULT_CONFIG );
    }

    /**
     * Configure from specific properties file.
     * 
     * @param propFile
     *            Path <u>within classpath </u> to load configuration from
     */
    public void configure( String propFile )
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
                log.error( "Failed to load properties for name [" + propFile + "]", ex );
                throw new IllegalStateException( ex.getMessage() );
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch ( Exception ignore )
                {
                    // Ignored
                }
            }

        }
        else
        {
            log.error( "Failed to load properties for name [" + propFile + "]" );
            throw new IllegalStateException( "Failed to load properties for name [" + propFile + "]" );
        }

        configure( props );
    }

    /**
     * Configure from properties object.
     * <p>
     * This method will call confiure, instructing it to use ssytem properties
     * as a default.
     * 
     * @param props
     */
    public void configure( Properties props )
    {
        configure( props, DEFAULT_USE_SYSTEM_PROPERTIES );
    }

    /**
     * Configure from properties object, overriding with values from the system
     * properteis if instructed.
     * <p>
     * You can override a specif value by passing in a ssytem property:
     * <p>
     * For example, you could override this value in the cache.ccf file by
     * starting up your program with the argument:
     * -Djcs.auxiliary.LTCP.attributes.TcpListenerPort=1111
     * 
     * 
     * @param props
     * @param useSystemProperties --
     *            if true, values starting with jcs will be put into the props
     *            file prior to configuring the cache.
     */
    public void configure( Properties props, boolean useSystemProperties )
    {
        if ( props != null )
        {

            if ( useSystemProperties )
            {
                // override any setting with values from the system properties.
                Properties sysProps = System.getProperties();
                Set keys = sysProps.keySet();
                Iterator keyIt = keys.iterator();
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

            // set the props value and then configure the ThreadPoolManager
            ThreadPoolManager.setProps( props );
            ThreadPoolManager poolMgr = ThreadPoolManager.getInstance();

            // configure the cache
            CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( this );

            configurator.doConfigure( props );

            this.props = props;
        }
        else
        {
            log.error( "No properties found.  Please configure the cache correctly." );
        }
    }

    /**
     * Gets the defaultCacheAttributes attribute of the CacheHub object
     * 
     * @return The defaultCacheAttributes value
     */
    public ICompositeCacheAttributes getDefaultCacheAttributes()
    {
        return this.defaultCacheAttr.copy();
    }

    /**
     * Sets the defaultCacheAttributes attribute of the CacheHub object
     * 
     * @param icca
     *            The new defaultCacheAttributes value
     */
    public void setDefaultCacheAttributes( ICompositeCacheAttributes icca )
    {
        this.defaultCacheAttr = icca;
    }

    /**
     * Sets the defaultElementAttributes attribute of the CacheHub object
     * 
     * @param iea
     *            The new defaultElementAttributes value
     */
    public void setDefaultElementAttributes( IElementAttributes iea )
    {
        this.defaultElementAttr = iea;
    }

    /**
     * Gets the defaultElementAttributes attribute of the CacheHub object
     * 
     * @return The defaultElementAttributes value
     */
    public IElementAttributes getDefaultElementAttributes()
    {
        return this.defaultElementAttr.copy();
    }

    /** Gets the cache attribute of the CacheHub object */
    public CompositeCache getCache( String cacheName )
    {
        return getCache( cacheName, this.defaultCacheAttr.copy() );
    }

    /** Gets the cache attribute of the CacheHub object */
    public CompositeCache getCache( String cacheName, ICompositeCacheAttributes cattr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, this.defaultElementAttr );
    }

    /** Gets the cache attribute of the CacheHub object */
    public CompositeCache getCache( String cacheName, ICompositeCacheAttributes cattr, IElementAttributes attr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, this.defaultElementAttr );
    }

    /** Gets the cache attribute of the CacheHub object */
    public CompositeCache getCache( ICompositeCacheAttributes cattr )
    {
        return getCache( cattr, this.defaultElementAttr );
    }

    /**
     * If the cache is created the CacheAttributes and the element Attributes
     * will be ignored. Currently there is no overiding once it is set up.
     * <p>
     * Overriding hte default elemental atributes will require cahnging the way
     * the atributes are assigned to elements. Get cache creates a cache with
     * defaults if none are specified. We might want to create separate method
     * for creating/getting. . .
     */
    public CompositeCache getCache( ICompositeCacheAttributes cattr, IElementAttributes attr )
    {
        CompositeCache cache;

        synchronized ( caches )
        {
            cache = (CompositeCache) caches.get( cattr.getCacheName() );
            if ( cache == null )
            {
                cattr.setCacheName( cattr.getCacheName() );

                CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( this );

                cache = configurator.parseRegion( this.props, cattr.getCacheName(), this.defaultAuxValues, cattr );

                caches.put( cattr.getCacheName(), cache );
            }
        }

        return cache;
    }

    /** */
    public void freeCache( String name )
    {
        freeCache( name, false );
    }

    /** */
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

    /** */
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

            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                CompositeCache cache = (CompositeCache) allCaches.nextElement();

                if ( cache != null )
                {
                    cache.dispose( fromRemote );
                }
            }
        }
    }

    /** Returns a list of the current cache names. */
    public String[] getCacheNames()
    {
        String[] list = new String[caches.size()];
        int i = 0;
        for ( Iterator itr = caches.keySet().iterator(); itr.hasNext(); )
        {
            list[i++] = (String) itr.next();
        }
        return list;
    }

    /** */
    public int getCacheType()
    {
        return ICacheType.CACHE_HUB;
    }

    /** */
    public ICompositeCacheAttributes getDefaultCattr()
    {
        return this.defaultCacheAttr;
    }

    /** */
    void registryFacPut( AuxiliaryCacheFactory auxFac )
    {
        auxFacs.put( auxFac.getName(), auxFac );
    }

    /** */
    AuxiliaryCacheFactory registryFacGet( String name )
    {
        return (AuxiliaryCacheFactory) auxFacs.get( name );
    }

    /** */
    void registryAttrPut( AuxiliaryCacheAttributes auxAttr )
    {
        auxAttrs.put( auxAttr.getName(), auxAttr );
    }

    /** */
    AuxiliaryCacheAttributes registryAttrGet( String name )
    {
        return (AuxiliaryCacheAttributes) auxAttrs.get( name );
    }

    /**
     * Gets stats for debugging.
     * 
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * This returns data gathered for all region and all the auxiliaries they
     * currently uses.
     * 
     * @return
     */
    public ICacheStats[] getStatistics()
    {
        ArrayList cacheStats = new ArrayList();
        Enumeration allCaches = caches.elements();
        while ( allCaches.hasMoreElements() )
        {
            CompositeCache cache = (CompositeCache) allCaches.nextElement();
            if ( cache != null )
            {
                cacheStats.add( cache.getStatistics() );
            }
        }
        ICacheStats[] stats = (ICacheStats[]) cacheStats.toArray( new CacheStats[0] );
        return stats;
    }

}
