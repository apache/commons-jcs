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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.config.OptionConverter;
import org.apache.jcs.config.PropertySetter;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * This class configures JCS based on a properties object.
 * <p>
 * This class is based on the log4j class org.apache.log4j.PropertyConfigurator
 * which was made by: "Luke Blanshard" <Luke@quiq.com>"Mark DONSZELMANN"
 * <Mark.Donszelmann@cern.ch>"Anders Kristensen" <akristensen@dynamicsoft.com>
 *
 */
public class CompositeCacheConfigurator
{
    private final static Log log = LogFactory.getLog( CompositeCacheConfigurator.class );

    final static String DEFAULT_REGION = "jcs.default";

    final static String REGION_PREFIX = "jcs.region.";

    final static String SYSTEM_REGION_PREFIX = "jcs.system.";

    final static String AUXILIARY_PREFIX = "jcs.auxiliary.";

    final static String ATTRIBUTE_PREFIX = ".attributes";

    final static String CACHE_ATTRIBUTE_PREFIX = ".cacheattributes";

    final static String ELEMENT_ATTRIBUTE_PREFIX = ".elementattributes";

    private CompositeCacheManager compositeCacheManager;

    /**
     * Constructor for the CompositeCacheConfigurator object
     *
     * @param ccMgr
     */
    public CompositeCacheConfigurator( CompositeCacheManager ccMgr )
    {
        this.compositeCacheManager = ccMgr;
    }

    /**
     * Configure cached for file name.
     * <p>
     * This is only used for testing. The manager handles the translation of a
     * file into a properties object.
     *
     * @param configFileName
     */
    protected void doConfigure( String configFileName )
    {
        Properties props = new Properties();
        try
        {
            FileInputStream istream = new FileInputStream( configFileName );
            props.load( istream );
            istream.close();
        }
        catch ( IOException e )
        {
            log.error( "Could not read configuration file, ignored: " + configFileName, e );
            return;
        }

        // If we reach here, then the config file is alright.
        doConfigure( props );
    }

    /**
     * Configure cache for properties object.
     * <p>
     * This method proceeds in several steps:
     * <ul>
     * <li>Store props for use by non configured caches.
     * <li>Set default value list
     * <li>Set default cache attr
     * <li>Set default element attr
     * <li>Setup system caches to be used
     * <li>Setup preconfigured caches
     * </ul>
     *
     * @param properties
     */
    public void doConfigure( Properties properties )
    {
        long start = System.currentTimeMillis();

        // store props for use by non configured caches
        compositeCacheManager.props = properties;
        // set default value list
        setDefaultAuxValues( properties );
        // set default cache attr
        setDefaultCompositeCacheAttributes( properties );
        // set default element attr
        setDefaultElementAttributes( properties );

        // set up ssytem caches to be used by non system caches
        // need to make sure there is no circuarity of reference
        parseSystemRegions( properties );

        // setup preconfigured caches
        parseRegions( properties );

        long end = System.currentTimeMillis();
        if ( log.isInfoEnabled() )
        {
            log.info( "Finished configuration in " + ( end - start ) + " ms." );
        }

    }

    /**
     * Set the default aux list for new caches.
     *
     * @param props
     */
    protected void setDefaultAuxValues( Properties props )
    {
        String value = OptionConverter.findAndSubst( DEFAULT_REGION, props );
        compositeCacheManager.defaultAuxValues = value;

        if ( log.isInfoEnabled() )
        {
            log.info( "Setting default auxiliaries to " + value );
        }
    }

    /**
     * Set the default CompositeCacheAttributes for new caches.
     *
     * @param props
     */
    protected void setDefaultCompositeCacheAttributes( Properties props )
    {
        ICompositeCacheAttributes icca = parseCompositeCacheAttributes( props, "",
                                                                        CompositeCacheConfigurator.DEFAULT_REGION );
        compositeCacheManager.setDefaultCacheAttributes( icca );

        log.info( "setting defaultCompositeCacheAttributes to " + icca );
    }

    /**
     * Set the default ElementAttributes for new caches.
     *
     * @param props
     */
    protected void setDefaultElementAttributes( Properties props )
    {
        IElementAttributes iea = parseElementAttributes( props, "", CompositeCacheConfigurator.DEFAULT_REGION );
        compositeCacheManager.setDefaultElementAttributes( iea );

        log.info( "setting defaultElementAttributes to " + iea );
    }

    /**
     * Create caches used internally. System status gives them creation
     * priority.
     *
     * @param props
     */
    protected void parseSystemRegions( Properties props )
    {
        Enumeration en = props.propertyNames();
        while ( en.hasMoreElements() )
        {
            String key = (String) en.nextElement();
            if ( key.startsWith( SYSTEM_REGION_PREFIX ) && ( key.indexOf( "attributes" ) == -1 ) )
            {
                String regionName = key.substring( SYSTEM_REGION_PREFIX.length() );
                String value = OptionConverter.findAndSubst( key, props );
                ICache cache;
                synchronized ( regionName )
                {
                    cache = parseRegion( props, regionName, value, null, SYSTEM_REGION_PREFIX );
                }
                compositeCacheManager.systemCaches.put( regionName, cache );
                // to be availiable for remote reference they need to be here as
                // well
                compositeCacheManager.caches.put( regionName, cache );
            }
        }
    }

    /**
     * Parse region elements.
     *
     * @param props
     */
    protected void parseRegions( Properties props )
    {
        List regionNames = new ArrayList();

        Enumeration en = props.propertyNames();
        while ( en.hasMoreElements() )
        {
            String key = (String) en.nextElement();
            if ( key.startsWith( REGION_PREFIX ) && ( key.indexOf( "attributes" ) == -1 ) )
            {
                String regionName = key.substring( REGION_PREFIX.length() );

                regionNames.add( regionName );

                String value = OptionConverter.findAndSubst( key, props );
                ICache cache;
                synchronized ( regionName )
                {
                    cache = parseRegion( props, regionName, value );
                }
                compositeCacheManager.caches.put( regionName, cache );
            }
        }

        if ( log.isInfoEnabled() )
        {
            log.info( "Parsed regions " + regionNames );
        }

    }

    /**
     * Create cache region.
     *
     * @param props
     * @param regName
     * @param value
     * @return CompositeCache
     */
    protected CompositeCache parseRegion( Properties props, String regName, String value )
    {
        return parseRegion( props, regName, value, null, REGION_PREFIX );
    }

    /**
     * Get all the properties for a region and configure its cache.
     * <p>
     * This method tells the otehr parse method the name of the region prefix.
     *
     * @param props
     * @param regName
     * @param value
     * @param cca
     * @return CompositeCache
     */
    protected CompositeCache parseRegion( Properties props, String regName, String value, ICompositeCacheAttributes cca )
    {
        return parseRegion( props, regName, value, cca, REGION_PREFIX );
    }

    /**
     * Get all the properties for a region and configure its cache.
     *
     * @param props
     * @param regName
     * @param value
     * @param cca
     * @param regionPrefix
     * @return CompositeCache
     */
    protected CompositeCache parseRegion( Properties props, String regName, String value,
                                         ICompositeCacheAttributes cca, String regionPrefix )
    {
        // First, create or get the cache and element attributes, and create
        // the cache.

        if ( cca == null )
        {
            cca = parseCompositeCacheAttributes( props, regName, regionPrefix );
        }

        IElementAttributes ea = parseElementAttributes( props, regName, regionPrefix );

        CompositeCache cache = new CompositeCache( regName, cca, ea );

        // Next, create the auxiliaries for the new cache

        List auxList = new ArrayList();

        if ( log.isDebugEnabled() )
        {
            log.debug( "Parsing region name '" + regName + "', value '" + value + "'" );
        }

        // We must skip over ',' but not white space
        StringTokenizer st = new StringTokenizer( value, "," );

        // If value is not in the form ", appender.." or "", then we should set
        // the priority of the category.

        if ( !( value.startsWith( "," ) || value.equals( "" ) ) )
        {
            // just to be on the safe side...
            if ( !st.hasMoreTokens() )
            {
                return null;
            }
        }

        AuxiliaryCache auxCache;
        String auxName;
        while ( st.hasMoreTokens() )
        {
            auxName = st.nextToken().trim();
            if ( auxName == null || auxName.equals( "," ) )
            {
                continue;
            }
            log.debug( "Parsing auxiliary named \"" + auxName + "\"." );

            auxCache = parseAuxiliary( cache, props, auxName, regName );

            if ( auxCache != null )
            {
                auxList.add( auxCache );
            }
        }

        // Associate the auxiliaries with the cache

        cache.setAuxCaches( (AuxiliaryCache[]) auxList.toArray( new AuxiliaryCache[0] ) );

        // Return the new cache

        return cache;
    }

    /**
     * Get an compositecacheattributes for the listed region.
     *
     * @param props
     * @param regName
     * @return
     */
    protected ICompositeCacheAttributes parseCompositeCacheAttributes( Properties props, String regName )
    {
        return parseCompositeCacheAttributes( props, regName, REGION_PREFIX );
    }

    /**
     * Get the main attributes for a region.
     *
     * @param props
     * @param regName
     * @param regionPrefix
     * @return ICompositeCacheAttributes
     */
    protected ICompositeCacheAttributes parseCompositeCacheAttributes( Properties props, String regName,
                                                                      String regionPrefix )
    {
        ICompositeCacheAttributes ccAttr;

        String attrName = regionPrefix + regName + CACHE_ATTRIBUTE_PREFIX;

        // auxFactory was not previously initialized.
        // String prefix = regionPrefix + regName + ATTRIBUTE_PREFIX;
        ccAttr = (ICompositeCacheAttributes) OptionConverter
            .instantiateByKey( props, attrName, org.apache.jcs.engine.behavior.ICompositeCacheAttributes.class, null );

        if ( ccAttr == null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "No special CompositeCacheAttributes class defined for key [" + attrName + "], using default class." );
            }

            ICompositeCacheAttributes ccAttr2 = compositeCacheManager.getDefaultCacheAttributes();
            ccAttr = ccAttr2.copy();
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Parsing options for '" + attrName + "'" );
        }

        PropertySetter.setProperties( ccAttr, props, attrName + "." );
        ccAttr.setCacheName( regName );

        if ( log.isDebugEnabled() )
        {
            log.debug( "End of parsing for \"" + attrName + "\"." );
        }

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        ccAttr.setCacheName( regName );
        return ccAttr;
    }

    /**
     * Create the element attributes from the properties object for a cache
     * region.
     *
     * @param props
     * @param regName
     * @param regionPrefix
     * @return IElementAttributes
     */
    protected IElementAttributes parseElementAttributes( Properties props, String regName, String regionPrefix )
    {
        IElementAttributes eAttr;

        String attrName = regionPrefix + regName + CompositeCacheConfigurator.ELEMENT_ATTRIBUTE_PREFIX;

        // auxFactory was not previously initialized.
        // String prefix = regionPrefix + regName + ATTRIBUTE_PREFIX;
        eAttr = (IElementAttributes) OptionConverter
            .instantiateByKey( props, attrName, org.apache.jcs.engine.behavior.IElementAttributes.class, null );
        if ( eAttr == null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "No special ElementAttribute class defined for key [" + attrName + "], using default class." );
            }

            IElementAttributes eAttr2 = compositeCacheManager.getDefaultElementAttributes();
            eAttr = eAttr2.copy();
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Parsing options for '" + attrName + "'" );
        }

        PropertySetter.setProperties( eAttr, props, attrName + "." );
        // eAttr.setCacheName( regName );

        if ( log.isDebugEnabled() )
        {
            log.debug( "End of parsing for \"" + attrName + "\"." );
        }

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        // eAttr.setCacheName( regName );
        return eAttr;
    }

    /**
     * Get an aux cache for the listed aux for a region.
     *
     * @param cache
     *            the cache manager
     * @param props
     *            the configuration propeties
     * @param auxName
     *            the name of the auxiliary cache
     * @param regName
     *            the name of the region.
     * @return AuxiliaryCache
     */
    protected AuxiliaryCache parseAuxiliary( CompositeCache cache, Properties props, String auxName, String regName )
    {
        AuxiliaryCache auxCache;

        if ( log.isDebugEnabled() )
        {
            // cache isn't used.
            // TODO change method signature if is isn't needed.
            log.debug( "parseAuxiliary, Cache = " + cache );
        }

        // GET FACTORY
        AuxiliaryCacheFactory auxFac = compositeCacheManager.registryFacGet( auxName );
        if ( auxFac == null )
        {
            // auxFactory was not previously initialized.
            String prefix = AUXILIARY_PREFIX + auxName;
            auxFac = (AuxiliaryCacheFactory) OptionConverter
                .instantiateByKey( props, prefix, org.apache.jcs.auxiliary.AuxiliaryCacheFactory.class, null );
            if ( auxFac == null )
            {
                log.error( "Could not instantiate auxFactory named \"" + auxName + "\"." );
                return null;
            }

            auxFac.setName( auxName );

            compositeCacheManager.registryFacPut( auxFac );
        }

        // GET ATTRIBUTES
        AuxiliaryCacheAttributes auxAttr = compositeCacheManager.registryAttrGet( auxName );
        String attrName = AUXILIARY_PREFIX + auxName + ATTRIBUTE_PREFIX;
        if ( auxAttr == null )
        {
            // auxFactory was not previously initialized.
            String prefix = AUXILIARY_PREFIX + auxName + ATTRIBUTE_PREFIX;
            auxAttr = (AuxiliaryCacheAttributes) OptionConverter
                .instantiateByKey( props, prefix, org.apache.jcs.auxiliary.AuxiliaryCacheAttributes.class, null );
            if ( auxFac == null )
            {
                log.error( "Could not instantiate auxAttr named '" + attrName + "'" );
                return null;
            }
            auxAttr.setName( auxName );
            compositeCacheManager.registryAttrPut( auxAttr );
        }

        auxAttr = auxAttr.copy();

        if ( log.isDebugEnabled() )
        {
            log.debug( "Parsing options for '" + attrName + "'" );
        }

        PropertySetter.setProperties( auxAttr, props, attrName + "." );
        auxAttr.setCacheName( regName );

        if ( log.isDebugEnabled() )
        {
            log.debug( "End of parsing for '" + attrName + "'" );
        }

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        auxAttr.setCacheName( regName );
        // Consider putting the compositeCache back in the factory interface
        // since the manager may not know about it at this point.
        // need to make sure the maanger already has the cache
        // before the auxiliary is created.
        auxCache = auxFac.createCache( auxAttr, compositeCacheManager );
        return auxCache;
    }
}
