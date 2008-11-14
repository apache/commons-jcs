package org.apache.jcs.auxiliary;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.config.OptionConverter;
import org.apache.jcs.utils.config.PropertySetter;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * Configuration util for auxiliary caches. I plan to move the auxiliary configuration from the
 * composite cache configurator here.
 */
public class AuxiliaryCacheConfigurator
{
    /** The logger. */
    private final static Log log = LogFactory.getLog( AuxiliaryCacheConfigurator.class );

    /** .attributes */
    public final static String ATTRIBUTE_PREFIX = ".attributes";

    /**
     * jcs.auxiliary.NAME.cacheeventlogger=CLASSNAME
     * <p>
     * jcs.auxiliary.NAME.cacheeventlogger.attributes.CUSTOMPROPERTY=VALUE
     */
    public final static String CACHE_EVENT_LOGGER_PREFIX = ".cacheeventlogger";

    /**
     * jcs.auxiliary.NAME.serializer=CLASSNAME
     * <p>
     * jcs.auxiliary.NAME.serializer.attributes.CUSTOMPROPERTY=VALUE
     */
    public final static String SERIALIZER_PREFIX = ".serializer";

    /**
     * Parses the event logger config, if there is any for the auxiliary.
     * <p>
     * @param props
     * @param auxPrefix - ex. AUXILIARY_PREFIX + auxName
     * @return cacheEventLogger
     */
    public static ICacheEventLogger parseCacheEventLogger( Properties props, String auxPrefix )
    {
        ICacheEventLogger cacheEventLogger = null;

        // auxFactory was not previously initialized.
        String eventLoggerClassName = auxPrefix + CACHE_EVENT_LOGGER_PREFIX;
        cacheEventLogger = (ICacheEventLogger) OptionConverter
            .instantiateByKey( props, eventLoggerClassName,
                               org.apache.jcs.engine.logging.behavior.ICacheEventLogger.class, null );
        if ( cacheEventLogger != null )
        {
            String cacheEventLoggerAttributePrefix = auxPrefix + CACHE_EVENT_LOGGER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( cacheEventLogger, props, cacheEventLoggerAttributePrefix + "." );
            if ( log.isInfoEnabled() )
            {
                log.info( "Using custom cache event logger [" + cacheEventLogger + "] for auxiliary [" + auxPrefix
                    + "]" );
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "No cache event logger defined for auxiliary [" + auxPrefix + "]" );
            }
        }
        return cacheEventLogger;
    }

    /**
     * Parses the element config, if there is any for the auxiliary.
     * <p>
     * @param props
     * @param auxPrefix - ex. AUXILIARY_PREFIX + auxName
     * @return cacheEventLogger
     */
    public static IElementSerializer parseElementSerializer( Properties props, String auxPrefix )
    {
        // TODO take in the entire prop key

        IElementSerializer elementSerializer = null;

        // auxFactory was not previously initialized.
        String elementSerializerClassName = auxPrefix + SERIALIZER_PREFIX;
        elementSerializer = (IElementSerializer) OptionConverter
            .instantiateByKey( props, elementSerializerClassName,
                               org.apache.jcs.engine.behavior.IElementSerializer.class, null );
        if ( elementSerializer != null )
        {
            String attributePrefix = auxPrefix + SERIALIZER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( elementSerializer, props, attributePrefix + "." );
            if ( log.isInfoEnabled() )
            {
                log.info( "Using custom element serializer [" + elementSerializer + "] for auxiliary [" + auxPrefix
                    + "]" );
            }
        }
        else
        {
            // use the default standard serializer
            elementSerializer = new StandardSerializer();
            if ( log.isInfoEnabled() )
            {
                log.info( "Using standard serializer [" + elementSerializer + "] for auxiliary [" + auxPrefix + "]" );
            }
        }
        return elementSerializer;
    }
}
