package org.apache.commons.jcs3.utils.config;

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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This class is based on the log4j class org.apache.log4j.config.PropertySetter that was made by
 * Anders Kristensen
 * <p>
 * General purpose Object property setter. Clients repeatedly invokes {@link #setProperty
 * setProperty(name,value)} in order to invoke setters on the Object specified in the constructor.
 * This class relies on the JavaBeans {@link Introspector}to analyze the given Object Class using
 * reflection.
 * <p>
 * Usage:
 *
 * <pre>
 * PropertySetter ps = new PropertySetter( anObject );
 * ps.set( &quot;name&quot;, &quot;Joe&quot; );
 * ps.set( &quot;age&quot;, &quot;32&quot; );
 * ps.set( &quot;isMale&quot;, &quot;true&quot; );
 * </pre>
 *
 * will cause the invocations anObject.setName("Joe"), anObject.setAge(32), and setMale(true) if
 * such methods exist with those signatures. Otherwise an {@link IntrospectionException}are thrown.
 */
public class PropertySetter
{
    /** Logger */
    private static final Log log = LogManager.getLog( PropertySetter.class );

    /** Description of the Field */
    private final Object obj;

    /** Description of the Field */
    private PropertyDescriptor[] props;

    /**
     * Create a new PropertySetter for the specified Object. This is done in preparation for invoking
     * {@link #setProperty}one or more times.
     * @param obj the object for which to set properties
     */
    public PropertySetter( final Object obj )
    {
        this.obj = obj;
    }

    /**
     * Uses JavaBeans {@link Introspector}to compute setters of object to be configured.
     */
    protected void introspect()
    {
        try
        {
            final BeanInfo bi = Introspector.getBeanInfo( obj.getClass() );
            props = bi.getPropertyDescriptors();
        }
        catch ( final IntrospectionException ex )
        {
            log.error( "Failed to introspect {0}", obj, ex );
            props = new PropertyDescriptor[0];
        }
    }

    /**
     * Set the properties of an object passed as a parameter in one go. The <code>properties</code>
     * are parsed relative to a <code>prefix</code>.
     * <p>
     * @param obj The object to configure.
     * @param properties A java.util.Properties containing keys and values.
     * @param prefix Only keys having the specified prefix will be set.
     */
    public static void setProperties( final Object obj, final Properties properties, final String prefix )
    {
        new PropertySetter( obj ).setProperties( properties, prefix );
    }

    /**
     * Set the properties for the object that match the <code>prefix</code> passed as parameter.
     * <p>
     * @param properties The new properties value
     * @param prefix The new properties value
     */
    public void setProperties( final Properties properties, final String prefix )
    {
        final int len = prefix.length();

        for (final String key : properties.stringPropertyNames())
        {
            // handle only properties that start with the desired prefix.
            if ( key.startsWith( prefix ) )
            {
                // ignore key if it contains dots after the prefix
                if ( key.indexOf( '.', len + 1 ) > 0 )
                {
                    continue;
                }

                final String value = OptionConverter.findAndSubst( key, properties );

                setProperty( key.substring( len ), value );
            }
        }

    }

    /**
     * Set a property on this PropertySetter's Object. If successful, this method will invoke a
     * setter method on the underlying Object. The setter is the one for the specified property name
     * and the value is determined partly from the setter argument type and partly from the value
     * specified in the call to this method.
     * <p>
     * If the setter expects a String no conversion is necessary. If it expects an int, then an
     * attempt is made to convert 'value' to an int using Integer.valueOf(value). If the setter expects
     * a boolean, the conversion is by Boolean.valueOf(value).
     * @param name name of the property
     * @param value String value of the property
     */

    public void setProperty( String name, final String value )
    {
        if ( value == null )
        {
            return;
        }

        name = Introspector.decapitalize( name );
        final PropertyDescriptor prop = getPropertyDescriptor( name );

        //log.debug("---------Key: "+name+", type="+prop.getPropertyType());

        if ( prop == null )
        {
            log.warn( "No such property [{0}] in {1}.", name, obj.getClass().getName() );
        }
        else
        {
            try
            {
                setProperty( prop, name, value );
            }
            catch ( final PropertySetterException ex )
            {
                log.warn( "Failed to set property {0} to value \"{1}\".", name, value, ex );
            }
        }
    }

    /**
     * Set the named property given a {@link PropertyDescriptor}.
     * @param prop A PropertyDescriptor describing the characteristics of the property to set.
     * @param name The named of the property to set.
     * @param value The value of the property.
     * @throws PropertySetterException
     */

    public void setProperty( final PropertyDescriptor prop, final String name, final String value )
        throws PropertySetterException
    {
        final Method setter = prop.getWriteMethod();
        if ( setter == null )
        {
            throw new PropertySetterException( "No setter for property" );
        }
        final Class<?>[] paramTypes = setter.getParameterTypes();
        if ( paramTypes.length != 1 )
        {
            throw new PropertySetterException( "#params for setter != 1" );
        }

        final Object arg;
        try
        {
            arg = convertArg( value, paramTypes[0] );
        }
        catch ( final Throwable t )
        {
            throw new PropertySetterException( "Conversion to type [" + paramTypes[0] + "] failed. Reason: " + t );
        }
        if ( arg == null )
        {
            throw new PropertySetterException( "Conversion to type [" + paramTypes[0] + "] failed." );
        }
        log.debug( "Setting property [{0}] to [{1}].", name, arg );
        try
        {
            setter.invoke( obj, arg );
        }
        catch ( final Exception ex )
        {
            throw new PropertySetterException( ex );
        }
    }

    /**
     * Convert <code>val</code> a String parameter to an object of a given type.
     * @param val
     * @param type
     * @return Object
     */
    protected Object convertArg( final String val, final Class<?> type )
    {
        if ( val == null )
        {
            return null;
        }

        final String v = val.trim();
        if ( String.class.isAssignableFrom( type ) )
        {
            return val;
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
            if ( "true".equalsIgnoreCase( v ) )
            {
                return Boolean.TRUE;
            }
            if ( "false".equalsIgnoreCase( v ) )
            {
                return Boolean.FALSE;
            }
        }
        else if( type.isEnum() )
        {
            return Enum.valueOf(type.asSubclass(Enum.class), v );
        }
        else if ( File.class.isAssignableFrom( type ) )
        {
            return new File( v );
        }
        return null;
    }

    /**
     * Gets the propertyDescriptor attribute of the PropertySetter object
     * @param name
     * @return The propertyDescriptor value
     */
    protected PropertyDescriptor getPropertyDescriptor( final String name )
    {
        if ( props == null )
        {
            introspect();
        }

        for (final PropertyDescriptor prop : props) {
            if ( name.equals( prop.getName() ) )
            {
                return prop;
            }
        }
        return null;
    }
}
