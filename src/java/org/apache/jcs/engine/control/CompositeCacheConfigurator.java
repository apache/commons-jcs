package org.apache.jcs.engine.control;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheFactory;

import org.apache.jcs.config.OptionConverter;
import org.apache.jcs.config.PropertySetter;

import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is based on the log4j class org.apache.log4j.PropertyConfigurator
 * which was made by: "Luke Blanshard" <Luke@quiq.com> "Mark DONSZELMANN"
 * <Mark.Donszelmann@cern.ch> "Anders Kristensen" <akristensen@dynamicsoft.com>
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CompositeCacheConfigurator
{
    private final static Log log =
        LogFactory.getLog( CompositeCacheConfigurator.class );

    final static String DEFAULT_REGION = "jcs.default";
    final static String REGION_PREFIX = "jcs.region.";
    final static String SYSTEM_REGION_PREFIX = "jcs.system.";
    final static String AUXILIARY_PREFIX = "jcs.auxiliary.";
    final static String ATTRIBUTE_PREFIX = ".attributes";
    final static String CACHE_ATTRIBUTE_PREFIX = ".cacheattributes";
    final static String ELEMENT_ATTRIBUTE_PREFIX = ".elementattributes";

    private CacheHub ccMgr;

    /**
     * Constructor for the CompositeCacheConfigurator object
     *
     * @param ccMgr
     */
    public CompositeCacheConfigurator( CacheHub ccMgr )
    {
        this.ccMgr = ccMgr;
    }

    /** Configure cached for file name. */
    public void doConfigure( String configFileName )
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
            log.error( "Could not read configuration file, ignored: " +
                configFileName, e );
            return;
        }

        // If we reach here, then the config file is alright.
        doConfigure( props );
    }

    /** Configure cache for properties object */
    public void doConfigure( Properties properties )
    {

        // store props for use by non configured caches
        ccMgr.props = properties;
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

    }

    /** Set the default aux list for new caches. */
    protected void setDefaultAuxValues( Properties props )
    {
        String value = OptionConverter.findAndSubst( DEFAULT_REGION, props );
        ccMgr.defaultAuxValues = value;

        log.info( "setting defaults to " + value );
    }

    /** Set the default CompositeCacheAttributes for new caches. */
    protected void setDefaultCompositeCacheAttributes( Properties props )
    {
        ICompositeCacheAttributes icca =
            parseCompositeCacheAttributes( props, "", this.DEFAULT_REGION );
        ccMgr.setDefaultCacheAttributes( icca );

        log.info( "setting defaultCompositeCacheAttributes to " + icca );
    }

    /** Set the default ElementAttributes for new caches. */
    protected void setDefaultElementAttributes( Properties props )
    {
        IElementAttributes iea =
            parseElementAttributes( props, "", this.DEFAULT_REGION );
        ccMgr.setDefaultElementAttributes( iea );

        log.info( "setting defaultElementAttributes to " + iea );
    }

    /**
     * Create caches used internally. System status gives them creation
     * priority.
     */
    protected void parseSystemRegions( Properties props )
    {
        Enumeration enum = props.propertyNames();
        while ( enum.hasMoreElements() )
        {
            String key = ( String ) enum.nextElement();
            if ( key.startsWith( SYSTEM_REGION_PREFIX )
                 && ( key.indexOf( "attributes" ) == -1 ) )
            {
                String regionName = key.substring( SYSTEM_REGION_PREFIX.length() );
                String value = OptionConverter.findAndSubst( key, props );
                ICache cache;
                synchronized ( regionName )
                {
                    cache = parseRegion( props, regionName, value, null, SYSTEM_REGION_PREFIX );
                }
                ccMgr.systemCaches.put( regionName, cache );
                // to be availiable for remote reference they need to be here as well
                ccMgr.caches.put( regionName, cache );
            }
        }
    }

    /** Parse region elements. */
    protected void parseRegions( Properties props )
    {
        Enumeration enum = props.propertyNames();
        while ( enum.hasMoreElements() )
        {
            String key = ( String ) enum.nextElement();
            if ( key.startsWith( REGION_PREFIX ) && ( key.indexOf( "attributes" ) == -1 ) )
            {
                String regionName = key.substring( REGION_PREFIX.length() );
                String value = OptionConverter.findAndSubst( key, props );
                ICache cache;
                synchronized ( regionName )
                {
                    cache = parseRegion( props, regionName, value );
                }
                ccMgr.caches.put( regionName, cache );
            }
        }
    }

    /** Create cache region. */
    protected ICache parseRegion( Properties props,
                                  String regName,
                                  String value )
    {
        return parseRegion( props, regName, value, null, REGION_PREFIX );
    }

    /** */
    protected ICache parseRegion( Properties props,
                                  String regName,
                                  String value,
                                  ICompositeCacheAttributes cca )
    {
        return parseRegion( props, regName, value, cca, REGION_PREFIX );
    }


    /** */
    protected ICache parseRegion( Properties props,
                                  String regName,
                                  String value,
                                  ICompositeCacheAttributes cca,
                                  String regionPrefix )
    {
        List auxList = new ArrayList();

        log.debug( "Parsing region name '" + regName + "', value '" + value + "'" );

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

        ICache auxCache;
        String auxName;
        while ( st.hasMoreTokens() )
        {
            auxName = st.nextToken().trim();
            if ( auxName == null || auxName.equals( "," ) )
            {
                continue;
            }
            log.debug( "Parsing auxiliary named \"" + auxName + "\"." );

            auxCache = parseAuxiliary( props, auxName, regName );
            if ( auxCache != null )
            {
                auxList.add( auxCache );
            }
        }

        ICache[] auxCaches = ( ICache[] ) auxList.toArray( new ICache[0] );

        // GET COMPOSITECACHEATTRIBUTES
        if ( cca == null )
        {
            cca = parseCompositeCacheAttributes( props, regName, regionPrefix );
        }

        IElementAttributes ea = parseElementAttributes( props, regName, regionPrefix );


        ICache cache = null;
        if ( regionPrefix.equals( SYSTEM_REGION_PREFIX ) )
        {
            //cache = ccMgr.createSystemCache( regName, auxCaches, cca, new ElementAttributes() );
            cache = ccMgr.createSystemCache( regName, auxCaches, cca, ea );
        }
        else
        {
            //cache = ccMgr.createCache( regName, auxCaches, cca, new ElementAttributes() );
            cache = ccMgr.createCache( regName, auxCaches, cca, ea );
        }
        return cache;
    }

    /** Get an compositecacheattributes for the listed region. */
    protected ICompositeCacheAttributes
        parseCompositeCacheAttributes( Properties props, String regName )
    {
        return parseCompositeCacheAttributes( props, regName, REGION_PREFIX );
    }


    /** */
    protected ICompositeCacheAttributes
        parseCompositeCacheAttributes( Properties props,
                                       String regName,
                                       String regionPrefix )
    {
        ICompositeCacheAttributes ccAttr;

        String attrName = regionPrefix + regName + CACHE_ATTRIBUTE_PREFIX;

        // auxFactory was not previously initialized.
        //String prefix = regionPrefix + regName + ATTRIBUTE_PREFIX;
        ccAttr = ( ICompositeCacheAttributes ) OptionConverter.instantiateByKey( props, attrName,
            org.apache.jcs.engine.behavior.ICompositeCacheAttributes.class,
            null );
        if ( ccAttr == null )
        {
            log.warn( "Could not instantiate ccAttr named '" + attrName +
                "', using defaults." );

            ICompositeCacheAttributes ccAttr2 = ccMgr.getDefaultCacheAttributes();
            ccAttr = ccAttr2.copy();
        }

        log.debug( "Parsing options for '" + attrName + "'" );

        PropertySetter.setProperties( ccAttr, props, attrName + "." );
        ccAttr.setCacheName( regName );

        log.debug( "End of parsing for \"" + attrName + "\"." );

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        ccAttr.setCacheName( regName );
        return ccAttr;
    }

    /** */
    protected IElementAttributes
        parseElementAttributes( Properties props,
                                       String regName,
                                       String regionPrefix )
    {
        IElementAttributes eAttr;

        String attrName = regionPrefix + regName + this.ELEMENT_ATTRIBUTE_PREFIX;

        // auxFactory was not previously initialized.
        //String prefix = regionPrefix + regName + ATTRIBUTE_PREFIX;
        eAttr = ( IElementAttributes ) OptionConverter.instantiateByKey( props, attrName,
            org.apache.jcs.engine.behavior.IElementAttributes.class,
            null );
        if ( eAttr == null )
        {
            log.warn( "Could not instantiate eAttr named '" + attrName +
                "', using defaults." );

            IElementAttributes eAttr2 = ccMgr.getDefaultElementAttributes();
            eAttr = eAttr2.copy();
        }

        log.debug( "Parsing options for '" + attrName + "'" );

        PropertySetter.setProperties( eAttr, props, attrName + "." );
        //eAttr.setCacheName( regName );

        log.debug( "End of parsing for \"" + attrName + "\"." );

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        //eAttr.setCacheName( regName );
        return eAttr;
    }


    /** Get an aux cache for the listed aux for a region. */
    protected ICache parseAuxiliary( Properties props,
                                     String auxName,
                                     String regName )
    {
        ICache auxCache;

        // GET FACTORY
        IAuxiliaryCacheFactory auxFac = ccMgr.registryFacGet( auxName );
        if ( auxFac == null )
        {
            // auxFactory was not previously initialized.
            String prefix = AUXILIARY_PREFIX + auxName;
            auxFac = ( IAuxiliaryCacheFactory ) OptionConverter.instantiateByKey( props, prefix,
                org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheFactory.class,
                null );
            if ( auxFac == null )
            {
                log.error( "Could not instantiate auxFactory named \"" + auxName + "\"." );
                return null;
            }
            auxFac.setName( auxName );
            ccMgr.registryFacPut( auxFac );
        }

        // GET ATTRIBUTES
        IAuxiliaryCacheAttributes auxAttr = ccMgr.registryAttrGet( auxName );
        String attrName = AUXILIARY_PREFIX + auxName + ATTRIBUTE_PREFIX;
        if ( auxAttr == null )
        {
            // auxFactory was not previously initialized.
            String prefix = AUXILIARY_PREFIX + auxName + ATTRIBUTE_PREFIX;
            auxAttr = ( IAuxiliaryCacheAttributes ) OptionConverter.instantiateByKey( props, prefix,
                org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes.class,
                null );
            if ( auxFac == null )
            {
                log.error( "Could not instantiate auxAttr named '" + attrName + "'" );
                return null;
            }
            auxAttr.setName( auxName );
            ccMgr.registryAttrPut( auxAttr );
        }

        auxAttr = auxAttr.copy();

        log.debug( "Parsing options for '" + attrName + "'" );
        PropertySetter.setProperties( auxAttr, props, attrName + "." );
        auxAttr.setCacheName( regName );

        log.debug( "End of parsing for '" + attrName + "'" );

        // GET CACHE FROM FACTORY WITH ATTRIBUTES
        auxAttr.setCacheName( regName );
        auxCache = auxFac.createCache( auxAttr );
        return auxCache;
    }
}
