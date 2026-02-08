package org.apache.commons.jcs4.utils.config;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.jcs4.log.Log;

/**
 * Generic builder for record types annotated with @Configurable.
 * Automatically parses Properties and creates record instances.
 */
public class ConfigurationBuilder<T>
{
    private static final Log log = Log.getLog(ConfigurationBuilder.class);

    private final Class<T> recordClass;
    private final Map<String, Object> values = new HashMap<>();

    private ConfigurationBuilder(Class<T> recordClass, Object defaultRecord)
    {
        if (!recordClass.isRecord())
        {
            throw new IllegalArgumentException(recordClass.getName() + " is not a record");
        }
        this.recordClass = recordClass;

        if (defaultRecord != null)
        {
            initializeDefaults(defaultRecord);
        }
    }

    /**
     * Create a new builder for the given record class and an optional default record.
     */
    public static <T> ConfigurationBuilder<T> create(Class<T> recordClass)
    {
        return new ConfigurationBuilder<>(recordClass, null);
    }

    /**
     * Create a new builder for the given record class and an optional default record.
     */
    public static <T> ConfigurationBuilder<T> create(Class<T> recordClass, Object defaultRecord)
    {
        return new ConfigurationBuilder<>(recordClass, defaultRecord);
    }

    /**
     * Initialize default values from default record.
     *
     * @param defaultRecord a record object containing defaults
     */
    private void initializeDefaults(Object defaultRecord)
    {
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components)
        {
            try
            {
                Object value = component.getAccessor().invoke(defaultRecord);
                values.put(component.getName(), value);
            }
            catch (ReflectiveOperationException e)
            {
                log.error("Error setting defaults for {0}, field {1}",
                        recordClass, component.getName(), e);
            }
        }
    }

    /**
     * Load properties from a Properties object with a given prefix.
     *
     * @param props the Properties
     * @param prefix the Prefix
     */
    public ConfigurationBuilder<T> fromProperties(Properties props, String prefix)
    {
        if (props == null || props.isEmpty())
        {
            return this;
        }

        RecordComponent[] components = recordClass.getRecordComponents();

        for (RecordComponent component : components)
        {
            // Capitalize first letter of component name
            String name = Pattern.compile("^.").matcher(component.getName()).replaceFirst(
                    m -> m.group().toUpperCase(Locale.ENGLISH));

            String fullKey = String.join(".", prefix, name);
            String value = OptionConverter.findAndSubst(fullKey, props);

            if (value != null)
            {
                Object parsed = parseValue(value, component.getType());
                if (parsed != null)
                {
                    values.put(component.getName(), parsed);
                    log.debug("Loaded property {0}: {1}", fullKey, parsed);
                }
            }
        }

        return this;
    }

    /**
     * Build the record instance, throwing if any required properties are missing.
     */
    public T build()
    {
        return createInstance();
    }

    /**
     * Create the record instance using the canonical constructor.
     */
    private T createInstance()
    {
        try
        {
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            Object[] params = new Object[components.length];

            for (int i = 0; i < components.length; i++)
            {
                paramTypes[i] = components[i].getType();
                params[i] = values.getOrDefault(components[i].getName(),
                        // Use null-safe defaults for primitives
                        getDefaultForType(components[i].getType()));
            }

            Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
            T instance = constructor.newInstance(params);
            log.debug("Created instance of {0}", recordClass.getSimpleName());
            return instance;

        }
        catch (Exception e)
        {
            throw new RuntimeException(
                "Failed to create instance of " + recordClass.getName(), e
            );
        }
    }

    /**
     * Parse a string value to the appropriate type.
     */
    private Object parseValue(String value, Class<?> type)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }

        String v = value.trim();
        if ( String.class.isAssignableFrom( type ) )
        {
            return v;
        }
        if ( Integer.TYPE.isAssignableFrom( type ) )
        {
            return Integer.valueOf( v );
        }
        if ( Long.TYPE.isAssignableFrom( type ) )
        {
            return Long.valueOf( v );
        }
        if ( Boolean.TYPE.isAssignableFrom( type ) )
        {
            if (Boolean.parseBoolean(v))
            {
                return Boolean.TRUE;
            }
            else
            {
                return Boolean.FALSE;
            }
        }
        else if( type.isEnum() )
        {
            @SuppressWarnings({ "unchecked" })
            final Enum<?> valueOf = Enum.valueOf(type.asSubclass(Enum.class), v);
            return valueOf;
        }
        else if ( File.class.isAssignableFrom( type ) )
        {
            return new File( v );
        }

        log.warn("Unknown type for conversion: {0} and value {1}", type, value);
        return null;
    }

    /**
     * Get default value for a primitive type.
     */
    private Object getDefaultForType(Class<?> type)
    {
        if (type == boolean.class)
        {
            return false;
        }
        if (type == int.class)
        {
            return 0;
        }
        if (type == long.class)
        {
            return 0L;
        }
        if (type == double.class)
        {
            return 0.0d;
        }
        if (type == float.class)
        {
            return 0.0f;
        }
        return null;
    }
}
