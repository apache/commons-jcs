package org.apache.commons.jcs3.auxiliary;

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

import java.util.Properties;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.config.OptionConverter;
import org.apache.commons.jcs3.utils.config.PropertySetter;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * Configuration util for auxiliary caches. I plan to move the auxiliary configuration from the
 * composite cache configurator here.
 */
public class AuxiliaryCacheConfigurator
{
    /** The logger. */
    private static final Log log = LogManager.getLog( AuxiliaryCacheConfigurator.class );

    /** .attributes */
    public static final String ATTRIBUTE_PREFIX = ".attributes";

    /**
     * jcs.auxiliary.NAME.cacheeventlogger=CLASSNAME
     * <p>
     * jcs.auxiliary.NAME.cacheeventlogger.attributes.CUSTOMPROPERTY=VALUE
     */
    public static final String CACHE_EVENT_LOGGER_PREFIX = ".cacheeventlogger";

    /**
     * jcs.auxiliary.NAME.serializer=CLASSNAME
     * <p>
     * jcs.auxiliary.NAME.serializer.attributes.CUSTOMPROPERTY=VALUE
     */
    public static final String SERIALIZER_PREFIX = ".serializer";

    /**
     * Parses the event logger config, if there is any for the auxiliary.
     * <p>
     * @param props
     * @param auxPrefix - ex. AUXILIARY_PREFIX + auxName
     * @return cacheEventLogger
     */
    public static ICacheEventLogger parseCacheEventLogger( final Properties props, final String auxPrefix )
    {

        // auxFactory was not previously initialized.
        final String eventLoggerClassName = auxPrefix + CACHE_EVENT_LOGGER_PREFIX;
        final ICacheEventLogger cacheEventLogger = OptionConverter.instantiateByKey( props, eventLoggerClassName, null );
        if ( cacheEventLogger != null )
        {
            final String cacheEventLoggerAttributePrefix = auxPrefix + CACHE_EVENT_LOGGER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( cacheEventLogger, props, cacheEventLoggerAttributePrefix + "." );
            log.info( "Using custom cache event logger [{0}] for auxiliary [{1}]",
                    cacheEventLogger, auxPrefix );
        }
        else
        {
            log.info( "No cache event logger defined for auxiliary [{0}]", auxPrefix );
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
    public static IElementSerializer parseElementSerializer( final Properties props, final String auxPrefix )
    {
        // TODO take in the entire prop key
        // auxFactory was not previously initialized.
        final String elementSerializerClassName = auxPrefix + SERIALIZER_PREFIX;
        IElementSerializer elementSerializer = OptionConverter.instantiateByKey( props, elementSerializerClassName, null );
        if ( elementSerializer != null )
        {
            final String attributePrefix = auxPrefix + SERIALIZER_PREFIX + ATTRIBUTE_PREFIX;
            PropertySetter.setProperties( elementSerializer, props, attributePrefix + "." );
            log.info( "Using custom element serializer [{0}] for auxiliary [{1}]",
                    elementSerializer, auxPrefix );
        }
        else
        {
            // use the default standard serializer
            elementSerializer = new StandardSerializer();
            log.info( "Using standard serializer [{0}] for auxiliary [{1}]",
                    elementSerializer, auxPrefix );
        }
        return elementSerializer;
    }
}
