package org.apache.jcs.auxiliary;

import java.util.Properties;

import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.config.OptionConverter;
import org.apache.jcs.utils.config.PropertySetter;

/**
 * Configuration util for auxiliary caches. I plan to move the auxiliary configuration from the
 * composite cache configurator here.
 */
public class AuxiliaryCacheConfigurator
{
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
            .instantiateByKey( props, eventLoggerClassName, org.apache.jcs.engine.logging.behavior.ICacheEventLogger.class,
                               null );
        if ( cacheEventLogger != null )
        {
            String cacheEventLoggerAttributePrefix = auxPrefix + CACHE_EVENT_LOGGER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( cacheEventLogger, props, cacheEventLoggerAttributePrefix + "." );
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
            String cacheEventLoggerAttributePrefix = auxPrefix + SERIALIZER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( elementSerializer, props, cacheEventLoggerAttributePrefix + "." );
        }
        return elementSerializer;
    }
}
