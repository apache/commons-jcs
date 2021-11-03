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

import java.util.Properties;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This class is based on the log4j class org.apache.log4j.helpers.OptionConverter that was made by
 * Ceki G&uuml;lc&uuml; Simon Kitching; Avy Sharell (sharell@online.fr) Anders Kristensen Matthieu
 * Verbert (mve@zurich.ibm.com) A convenience class to convert property values to specific types.
 */
public class OptionConverter
{
    /** The logger */
    private static final Log log = LogManager.getLog( OptionConverter.class );

    /** System property delimter */
    private static final String DELIM_START = "${";

    /** System property delimter */
    private static final char DELIM_STOP = '}';

    /** System property delimter start length */
    private static final int DELIM_START_LEN = 2;

    /** System property delimter end length */
    private static final int DELIM_STOP_LEN = 1;

    /** No instances please. */
    private OptionConverter()
    {
    }

    /**
     * Combines two arrays.
     * @param l
     * @param r
     * @return String[]
     */
    public static String[] concatanateArrays( final String[] l, final String[] r )
    {
        final int len = l.length + r.length;
        final String[] a = new String[len];

        System.arraycopy( l, 0, a, 0, l.length );
        System.arraycopy( r, 0, a, l.length, r.length );

        return a;
    }

    /**
     * Escapes special characters.
     *
     * @param s
     * @return String
     */
    public static String convertSpecialChars( final String s )
    {
        char c;
        final int len = s.length();
        final StringBuilder sb = new StringBuilder( len );

        int i = 0;
        while ( i < len )
        {
            c = s.charAt( i++ );
            if ( c == '\\' )
            {
                c = s.charAt( i++ );
                if ( c == 'n' )
                {
                    c = '\n';
                }
                else if ( c == 'r' )
                {
                    c = '\r';
                }
                else if ( c == 't' )
                {
                    c = '\t';
                }
                else if ( c == 'f' )
                {
                    c = '\f';
                }
                else if ( c == '\b' )
                {
                    c = '\b';
                }
                else if ( c == '\"' )
                {
                    c = '\"';
                }
                else if ( c == '\'' )
                {
                    c = '\'';
                }
                else if ( c == '\\' )
                {
                    c = '\\';
                }
            }
            sb.append( c );
        }
        return sb.toString();
    }

    /**
     * Very similar to <code>System.getProperty</code> except that the {@link SecurityException} is
     * hidden.
     * @param key The key to search for.
     * @param def The default value to return.
     * @return the string value of the system property, or the default value if there is no property
     *         with that key.
     * @since 1.1
     */

    public static String getSystemProperty( final String key, final String def )
    {
        try
        {
            return System.getProperty( key, def );
        }
        catch ( final Throwable e )
        {
            // MS-Java throws com.ms.security.SecurityExceptionEx
            log.debug( "Was not allowed to read system property \"{0}\".", key );
            return def;
        }
    }

    /**
     * Creates an object for the className value of the key.
     *
     * @param props
     * @param key
     * @param defaultValue
     * @return Object that was created
     */
    public static <T> T instantiateByKey( final Properties props, final String key, final T defaultValue )
    {

        // Get the value of the property in string form
        final String className = findAndSubst( key, props );
        if ( className == null )
        {
            log.trace( "Could not find value for key {0}", key );
            return defaultValue;
        }
        // Trim className to avoid trailing spaces that cause problems.
        return OptionConverter.instantiateByClassName( className.trim(), defaultValue );
    }

    /**
     * If <code>value</code> is "true", then <code>true</code> is returned. If <code>value</code> is
     * "false", then <code>true</code> is returned. Otherwise, <code>default</code> is returned.
     *
     * Case of value is unimportant.
     * @param value
     * @param defaultValue
     * @return Object
     */
    public static boolean toBoolean( final String value, final boolean defaultValue )
    {
        if ( value == null )
        {
            return defaultValue;
        }
        final String trimmedVal = value.trim();
        if ( "true".equalsIgnoreCase( trimmedVal ) )
        {
            return true;
        }
        if ( "false".equalsIgnoreCase( trimmedVal ) )
        {
            return false;
        }
        return defaultValue;
    }

    /**
     * Converts to int.
     *
     * @param value
     * @param defaultValue
     * @return int
     */
    public static int toInt( final String value, final int defaultValue )
    {
        if ( value != null )
        {
            final String s = value.trim();
            try
            {
                return Integer.parseInt(s);
            }
            catch ( final NumberFormatException e )
            {
                log.error( "[{0}] is not in proper int form.", s, e );
            }
        }
        return defaultValue;
    }

    /**
     * @param value
     * @param defaultValue
     * @return long
     */
    public static long toFileSize( final String value, final long defaultValue )
    {
        if ( value == null )
        {
            return defaultValue;
        }

        String s = value.trim().toUpperCase();
        long multiplier = 1;
        int index;

        if ( ( index = s.indexOf( "KB" ) ) != -1 )
        {
            multiplier = 1024;
            s = s.substring( 0, index );
        }
        else if ( ( index = s.indexOf( "MB" ) ) != -1 )
        {
            multiplier = 1024 * 1024;
            s = s.substring( 0, index );
        }
        else if ( ( index = s.indexOf( "GB" ) ) != -1 )
        {
            multiplier = 1024 * 1024 * 1024;
            s = s.substring( 0, index );
        }
        try
        {
            return Long.parseLong(s) * multiplier;
        }
        catch ( final NumberFormatException e )
        {
            log.error( "[{0}] is not in proper int form.", s);
            log.error( "[{0}] not in expected format", value, e );
        }
        return defaultValue;
    }

    /**
     * Find the value corresponding to <code>key</code> in <code>props</code>. Then perform variable
     * substitution on the found value.
     *
     * @param key
     * @param props
     * @return substituted string
     */

    public static String findAndSubst( final String key, final Properties props )
    {
        final String value = props.getProperty( key );
        if ( value == null )
        {
            return null;
        }

        try
        {
            return substVars( value, props );
        }
        catch ( final IllegalArgumentException e )
        {
            log.error( "Bad option value [{0}]", value, e );
            return value;
        }
    }

    /**
     * Instantiate an object given a class name. Check that the <code>className</code> is a subclass
     * of <code>superClass</code>. If that test fails or the object could not be instantiated, then
     * <code>defaultValue</code> is returned.
     *
     * @param className The fully qualified class name of the object to instantiate.
     * @param defaultValue The object to return in case of non-fulfillment
     * @return instantiated object
     */

    public static <T> T instantiateByClassName( final String className, final T defaultValue )
    {
        if ( className != null )
        {
            try
            {
                final Class<?> classObj = Class.forName( className );
                final Object o = classObj.getDeclaredConstructor().newInstance();

                try
                {
                    @SuppressWarnings("unchecked") // CCE catched
                    final
                    T t = (T) o;
                    return t;
                }
                catch (final ClassCastException e)
                {
                    log.error( "A \"{0}\" object is not assignable to the "
                            + "generic variable.", className );
                    return defaultValue;
                }
            }
            catch (final Exception e )
            {
                log.error( "Could not instantiate class [{0}]", className, e );
            }
        }
        return defaultValue;
    }

    /**
     * Perform variable substitution in string <code>val</code> from the values of keys found in the
     * system properties.
     *
     * The variable substitution delimiters are <b>${ </b> and <b>} </b>.
     *
     * For example, if the System properties contains "key=value", then the call
     *
     * <pre>
     * String s = OptionConverter.substituteVars( &quot;Value of key is ${key}.&quot; );
     * </pre>
     *
     * will set the variable <code>s</code> to "Value of key is value.".
     *
     * If no value could be found for the specified key, then the <code>props</code> parameter is
     * searched, if the value could not be found there, then substitution defaults to the empty
     * string.
     *
     * For example, if system properties contains no value for the key "inexistentKey", then the call
     *
     * <pre>
     * String s = OptionConverter.subsVars( &quot;Value of inexistentKey is [${inexistentKey}]&quot; );
     * </pre>
     *
     * will set <code>s</code> to "Value of inexistentKey is []"
     * <p>
     * An {@link java.lang.IllegalArgumentException}is thrown if <code>val</code> contains a start
     * delimiter "${" which is not balanced by a stop delimiter "}".
     * </p>
     * <p>
     * <b>Author </b> Avy Sharell
     * </p>
     * @param val The string on which variable substitution is performed.
     * @param props
     * @return String
     * @throws IllegalArgumentException if <code>val</code> is malformed.
     */

    public static String substVars( final String val, final Properties props )
        throws IllegalArgumentException
    {
        final StringBuilder sbuf = new StringBuilder();

        int i = 0;
        int j;
        int k;

        while ( true )
        {
            j = val.indexOf( DELIM_START, i );
            if ( j == -1 )
            {
                if ( i == 0 )
                {
                    return val;
                }
                sbuf.append( val.substring( i ) );
                return sbuf.toString();
            }
            sbuf.append(val, i, j);
            k = val.indexOf( DELIM_STOP, j );
            if ( k == -1 )
            {
                throw new IllegalArgumentException( '"' + val + "\" has no closing brace. Opening brace at position "
                    + j + '.' );
            }
            j += DELIM_START_LEN;
            final String key = val.substring( j, k );
            // first try in System properties
            String replacement = getSystemProperty( key, null );
            // then try props parameter
            if ( replacement == null && props != null )
            {
                replacement = props.getProperty( key );
            }

            if ( replacement != null )
            {
                sbuf.append( replacement );
            }
            i = k + DELIM_STOP_LEN;
        }
    }
}
