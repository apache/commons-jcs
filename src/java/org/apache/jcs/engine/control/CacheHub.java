package org.apache.jcs.engine.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheHub;

/** Manages a composite cache. */
public class CacheHub
    implements ICacheHub, IRemoteCacheConstants, Serializable
{
    private final static Log log =
        LogFactory.getLog( CacheHub.class );

    /** Caches managed by this cache manager */
    protected Hashtable caches = new Hashtable();

    /** Internal system caches for this cache manager */
    protected Hashtable systemCaches = new Hashtable();

    /** Number of clients accessing this cache manager */
    private int clients;

    /** Default cache attributes for this cache manager */
    protected ICompositeCacheAttributes defaultCacheAttr =
        new CompositeCacheAttributes();

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
    protected static CacheHub instance;

    /**
     * Gets the CacheHub instance. For backward compatibility, if this creates
     * the instance it will attempt to configure it with the default
     * configuration. If you want to configure from your own source, use
     * {@link #getUnconfiguredInstance} and then call {@link #configure}
     */
    public static synchronized CacheHub getInstance()
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

    public static synchronized CacheHub getInstance( String propsFilename )
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
     * Get a CacheHub instance which is not configured.
     */
    public static synchronized CacheHub getUnconfiguredInstance()
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
     * Simple factory method, must override in subclasses so getInstance
     * creates / returns the correct object.
     */
    protected static CacheHub createInstance()
    {
        return new CacheHub();
    }

    /**
     * Configure with default properties file
     */
    public void configure()
    {
        configure( CacheConstants.DEFAULT_CONFIG );
    }

    /**
     * Configure from specific properties file
     *
     * @param propFile Path <u>within classpath</u> to load configuration from
     */
    public void configure( String propFile )
    {
        log.debug( "Creating cache manager from config file: " + propFile );

        Properties props = new Properties();

        InputStream is = getClass().getResourceAsStream( propFile );

        try
        {
            props.load( is );

            if ( log.isDebugEnabled() )
            {
                log.debug( "File contained " + props.size() + " properties" );
            }
        }
        catch ( IOException ex )
        {
            log.error( "Failed to load properties", ex );
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

        configure( props );
    }

    /**
     * Configure from properties object
     *
     * @param props
     */
    public void configure( Properties props )
    {
        // FIXME: need to do something for defaults
        // create a default entry in the propsfile
        // setDefaults( props );

        CompositeCacheConfigurator configurator =
            new CompositeCacheConfigurator( this );

        configurator.doConfigure( props );

        // FIXME: Supposedly neither of these can be thrown from the above code,
        //        are they safe to remove? Or should the go elsewhere?
        //
        // catch ( IOException ex )
        // {
        //     log.error( "Failed to create caches", ex );
        //     throw new IllegalStateException( ex.getMessage() );
        // }
        // catch ( NotBoundException ex )
        // {
        //     log.error( "Failed to create caches", ex );
        //     throw new IllegalStateException( ex.getMessage() );
        // }
    }

    /**
     * Gets the defaultCacheAttributes attribute of the CacheHub
     * object
     *
     * @return The defaultCacheAttributes value
     */
    public ICompositeCacheAttributes getDefaultCacheAttributes()
    {
        return this.defaultCacheAttr.copy();
    }

    /**
     * Sets the defaultCacheAttributes attribute of the CacheHub
     * object
     *
     * @param icca The new defaultCacheAttributes value
     */
    public void setDefaultCacheAttributes( ICompositeCacheAttributes icca )
    {
        this.defaultCacheAttr = icca;
    }

    /**
     * Sets the defaultElementAttributes attribute of the CacheHub
     * object
     *
     * @param iea The new defaultElementAttributes value
     */
    public void setDefaultElementAttributes( IElementAttributes iea )
    {
        this.defaultElementAttr = iea;
    }

    /**
     * Gets the defaultElementAttributes attribute of the CacheHub
     * object
     *
     * @return The defaultElementAttributes value
     */
    public IElementAttributes getDefaultElementAttributes()
    {
        return this.defaultElementAttr.copy();
    }

    /** Creates internal system cache */
    protected Cache createSystemCache( String cacheName,
                                       ICache[] auxCaches,
                                       ICompositeCacheAttributes cattr,
                                       IElementAttributes attr )
    {
        return new Cache( cacheName, auxCaches, cattr, attr );
    }

    /**
     * Factory method to create the actual Cache instance. Subclass can
     * override this method to create the specific cache.
     */
    protected Cache createCache( String cacheName,
                                 ICache[] auxCaches,
                                 ICompositeCacheAttributes cattr,
                                 IElementAttributes attr )
    {
        return new Cache( cacheName, auxCaches, cattr, attr );
    }

    /** Gets the cache attribute of the CacheHub object */
    public ICache getCache( String cacheName )
    {
        return getCache( cacheName, this.defaultCacheAttr.copy() );
    }

    /** Gets the cache attribute of the CacheHub object */
    public ICache getCache( String cacheName, ICompositeCacheAttributes cattr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, this.defaultElementAttr );
    }

    /** Gets the cache attribute of the CacheHub object */
    public ICache getCache( String cacheName,
                            ICompositeCacheAttributes cattr,
                            IElementAttributes attr )
    {
        cattr.setCacheName( cacheName );
        return getCache( cattr, this.defaultElementAttr );
    }

    /** Gets the cache attribute of the CacheHub object */
    public ICache getCache( ICompositeCacheAttributes cattr )
    {
        return getCache( cattr, this.defaultElementAttr );
    }

    /**
     * If the cache is created the CacheAttributes and the element Attributes
     * will be ignored. Currently there is no overiding once it is set up.
     * Overriding hte default elemental atributes will require cahnging the way
     * the atributes are assigned to elements. Get cache creates a cache with
     * defaults if none are specified. We might want to create separate method
     * for creating/getting. . .
     */
    public ICache getCache( ICompositeCacheAttributes cattr,
                            IElementAttributes attr )
    {
        ICache cache = ( ICache ) caches.get( cattr.getCacheName() );

        if ( cache == null )
        {
            synchronized ( caches )
            {
                cache = ( ICache ) caches.get( cattr.getCacheName() );
                if ( cache == null )
                {
                    cattr.setCacheName( cattr.getCacheName() );

                    CompositeCacheConfigurator configurator =
                        new CompositeCacheConfigurator( this );

                    cache = configurator.parseRegion( this.props,
                                                      cattr.getCacheName(),
                                                      this.defaultAuxValues,
                                                      cattr );

                    caches.put( cattr.getCacheName(), cache );
                }
            }
        }

        return cache;
    }

    /** */
    public void freeCache( String name )
    {
        freeCache( name, CacheConstants.LOCAL_INVOKATION );
    }

    /** */
    public void freeCache( String name, boolean fromRemote )
    {
        Cache cache = ( Cache ) caches.get( name );

        if ( cache != null )
        {
            cache.dispose( fromRemote );
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
        release( CacheConstants.LOCAL_INVOKATION );
    }

    /** */
    private void release( boolean fromRemote )
    {
        synchronized ( CacheHub.class )
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
                log.debug( "Last client called release. There are "
                           + caches.size() + " caches which will be disposed" );
            }

            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                Cache cache = ( Cache ) allCaches.nextElement();

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
        String[] list = new String[ caches.size() ];
        int i = 0;
        for ( Iterator itr = caches.keySet().iterator(); itr.hasNext(); )
        {
            list[ i++ ] = ( String ) itr.next();
        }
        return list;
    }

    /** */
    public int getCacheType()
    {
        return CACHE_HUB;
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
        return ( AuxiliaryCacheFactory ) auxFacs.get( name );
    }

    /** */
    void registryAttrPut( AuxiliaryCacheAttributes auxAttr )
    {
        auxAttrs.put( auxAttr.getName(), auxAttr );
    }

    /** */
    AuxiliaryCacheAttributes registryAttrGet( String name )
    {
        return ( AuxiliaryCacheAttributes ) auxAttrs.get( name );
    }
}

