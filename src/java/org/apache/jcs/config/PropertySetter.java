package org.apache.jcs.config;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.jcs.config.OptionConverter;
import org.apache.jcs.config.PropertySetterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is based on the log4j class org.apache.log4j.config.PropertySetter
 * that was made by Anders Kristensen
 *
 * @author asmuts
 * @created January 15, 2002
 */

/**
 * General purpose Object property setter. Clients repeatedly invokes {@link
 * #setProperty setProperty(name,value)} in order to invoke setters on the
 * Object specified in the constructor. This class relies on the JavaBeans
 * {@link Introspector} to analyze the given Object Class using reflection. <p>
 *
 * Usage: <pre>
 *PropertySetter ps = new PropertySetter(anObject);
 *ps.set("name", "Joe");
 *ps.set("age", "32");
 *ps.set("isMale", "true");
 *</pre> will cause the invocations anObject.setName("Joe"),
 * anObject.setAge(32), and setMale(true) if such methods exist with those
 * signatures. Otherwise an {@link IntrospectionException} are thrown.
 *
 * @author Anders Kristensen
 * @created January 15, 2002
 * @since 1.1
 */
public class PropertySetter
{
    private final static Log log =
        LogFactory.getLog( OptionConverter.class );

    /** Description of the Field */
    protected Object obj;
    /** Description of the Field */
    protected PropertyDescriptor[] props;

    /**
     * Create a new PropertySetter for the specified Object. This is done in
     * prepartion for invoking {@link #setProperty} one or more times.
     *
     * @param obj the object for which to set properties
     */
    public PropertySetter( Object obj )
    {
        this.obj = obj;
    }


    /**
     * Uses JavaBeans {@link Introspector} to computer setters of object to be
     * configured.
     */
    protected void introspect()
    {
        try
        {
            BeanInfo bi = Introspector.getBeanInfo( obj.getClass() );
            props = bi.getPropertyDescriptors();
        }
        catch ( IntrospectionException ex )
        {
            log.error( "Failed to introspect " + obj + ": " + ex.getMessage() );
            props = new PropertyDescriptor[0];
        }
    }


    /**
     * Set the properties of an object passed as a parameter in one go. The
     * <code>properties</code> are parsed relative to a <code>prefix</code>.
     *
     * @param obj The object to configure.
     * @param properties A java.util.Properties containing keys and values.
     * @param prefix Only keys having the specified prefix will be set.
     */
    public static void setProperties( Object obj, Properties properties, String prefix )
    {
        new PropertySetter( obj ).setProperties( properties, prefix );
    }


    /**
     * Set the properites for the object that match the <code>prefix</code>
     * passed as parameter.
     *
     * @param properties The new properties value
     * @param prefix The new properties value
     */
    public void setProperties( Properties properties, String prefix )
    {
        int len = prefix.length();

        for ( Enumeration e = properties.keys(); e.hasMoreElements();  )
        {
            String key = ( String ) e.nextElement();

            // handle only properties that start with the desired frefix.
            if ( key.startsWith( prefix ) )
            {

                // ignore key if it contains dots after the prefix
                if ( key.indexOf( '.', len + 1 ) > 0 )
                {
                    //System.err.println("----------Ignoring---["+key
                    //	     +"], prefix=["+prefix+"].");
                    continue;
                }

                String value = OptionConverter.findAndSubst( key, properties );
                key = key.substring( len );

                setProperty( key, value );
            }
        }

    }


    /**
     * Set a property on this PropertySetter's Object. If successful, this
     * method will invoke a setter method on the underlying Object. The setter
     * is the one for the specified property name and the value is determined
     * partly from the setter argument type and partly from the value specified
     * in the call to this method. <p>
     *
     * If the setter expects a String no conversion is necessary. If it expects
     * an int, then an attempt is made to convert 'value' to an int using new
     * Integer(value). If the setter expects a boolean, the conversion is by new
     * Boolean(value).
     *
     * @param name name of the property
     * @param value String value of the property
     */

    public void setProperty( String name, String value )
    {
        if ( value == null )
        {
            return;
        }

        name = Introspector.decapitalize( name );
        PropertyDescriptor prop = getPropertyDescriptor( name );

        //log.debug("---------Key: "+name+", type="+prop.getPropertyType());

        if ( prop == null )
        {
            log.warn( "No such property [" + name + "] in " +
                obj.getClass().getName() + "." );
        }
        else
        {
            try
            {
                setProperty( prop, name, value );
            }
            catch ( PropertySetterException ex )
            {
                log.warn( "Failed to set property " + name +
                    " to value \"" + value + "\". " + ex.getMessage() );
            }
        }
    }


    /**
     * Set the named property given a {@link PropertyDescriptor}.
     *
     * @param prop A PropertyDescriptor describing the characteristics of the
     *      property to set.
     * @param name The named of the property to set.
     * @param value The value of the property.
     */

    public void setProperty( PropertyDescriptor prop, String name, String value )
        throws PropertySetterException
    {
        Method setter = prop.getWriteMethod();
        if ( setter == null )
        {
            throw new PropertySetterException( "No setter for property" );
        }
        Class[] paramTypes = setter.getParameterTypes();
        if ( paramTypes.length != 1 )
        {
            throw new PropertySetterException( "#params for setter != 1" );
        }

        Object arg;
        try
        {
            arg = convertArg( value, paramTypes[0] );
        }
        catch ( Throwable t )
        {
            throw new PropertySetterException( "Conversion to type [" + paramTypes[0] +
                "] failed. Reason: " + t );
        }
        if ( arg == null )
        {
            throw new PropertySetterException(
                "Conversion to type [" + paramTypes[0] + "] failed." );
        }
        log.debug( "Setting property [" + name + "] to [" + arg + "]." );
        try
        {
            setter.invoke( obj, new Object[]{arg} );
        }
        catch ( Exception ex )
        {
            throw new PropertySetterException( ex );
        }
    }


    /**
     * Convert <code>val</code> a String parameter to an object of a given type.
     */
    protected Object convertArg( String val, Class type )
    {
        if ( val == null )
        {
            return null;
        }

        String v = val.trim();
        if ( String.class.isAssignableFrom( type ) )
        {
            return val;
        }
        else if ( Integer.TYPE.isAssignableFrom( type ) )
        {
            return new Integer( v );
        }
        else if ( Long.TYPE.isAssignableFrom( type ) )
        {
            return new Long( v );
        }
        else if ( Boolean.TYPE.isAssignableFrom( type ) )
        {
            if ( "true".equalsIgnoreCase( v ) )
            {
                return Boolean.TRUE;
            }
            else if ( "false".equalsIgnoreCase( v ) )
            {
                return Boolean.FALSE;
            }
        }
        return null;
    }


    /**
     * Gets the propertyDescriptor attribute of the PropertySetter object
     *
     * @return The propertyDescriptor value
     */
    protected PropertyDescriptor getPropertyDescriptor( String name )
    {
        if ( props == null )
        {
            introspect();
        }

        for ( int i = 0; i < props.length; i++ )
        {
            if ( name.equals( props[i].getName() ) )
            {
                return props[i];
            }
        }
        return null;
    }

}
