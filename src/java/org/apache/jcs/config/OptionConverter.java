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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is based on the log4j class
 * org.apache.log4j.helpers.OptionConverter that was made by Ceki
 * G&uuml;lc&uuml; Simon Kitching; Avy Sharell (sharell@online.fr) Anders
 * Kristensen Matthieu Verbert (mve@zurich.ibm.com) A convenience class to
 * convert property values to specific types.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class OptionConverter
{
    private final static Log log =
        LogFactory.getLog( OptionConverter.class );

    static String DELIM_START = "${";
    static char DELIM_STOP = '}';
    static int DELIM_START_LEN = 2;
    static int DELIM_STOP_LEN = 1;

    static StringBuffer sbuf = new StringBuffer();

    /** OptionConverter is a static class. */
    private OptionConverter() { }


    /** Description of the Method */
    public static String[] concatanateArrays( String[] l, String[] r )
    {
        int len = l.length + r.length;
        String[] a = new String[len];

        System.arraycopy( l, 0, a, 0, l.length );
        System.arraycopy( r, 0, a, l.length, r.length );

        return a;
    }


    /** Description of the Method */
    public static String convertSpecialChars( String s )
    {
        char c;
        int len = s.length();
        StringBuffer sbuf = new StringBuffer( len );

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
            sbuf.append( c );
        }
        return sbuf.toString();
    }


    /**
     * Very similar to <code>System.getProperty</code> except that the {@link
     * SecurityException} is hidden.
     *
     * @param key The key to search for.
     * @param def The default value to return.
     * @return the string value of the system property, or the default value if
     *      there is no property with that key.
     * @since 1.1
     */

    public static String getSystemProperty( String key, String def )
    {
        try
        {
            return System.getProperty( key, def );
        }
        catch ( Throwable e )
        {
            // MS-Java throws com.ms.security.SecurityExceptionEx
            log.debug( "Was not allowed to read system property \"" + key + "\"." );
            return def;
        }
    }


    /** Description of the Method */
    public static Object instantiateByKey( Properties props, String key, Class superClass,
                                           Object defaultValue )
    {

        // Get the value of the property in string form
        String className = findAndSubst( key, props );
        if ( className == null )
        {
            log.warn( "Could not find value for key " + key );
            return defaultValue;
        }
        // Trim className to avoid trailing spaces that cause problems.
        return OptionConverter.instantiateByClassName( className.trim(), superClass,
            defaultValue );
    }


    /**
     * If <code>value</code> is "true", then <code>true</code> is returned. If
     * <code>value</code> is "false", then <code>true</code> is returned.
     * Otherwise, <code>default</code> is returned. <p>
     *
     * Case of value is unimportant.
     */

    public static boolean toBoolean( String value, boolean dEfault )
    {
        if ( value == null )
        {
            return dEfault;
        }
        String trimmedVal = value.trim();
        if ( "true".equalsIgnoreCase( trimmedVal ) )
        {
            return true;
        }
        if ( "false".equalsIgnoreCase( trimmedVal ) )
        {
            return false;
        }
        return dEfault;
    }


    /** Description of the Method */
    public static int toInt( String value, int dEfault )
    {
        if ( value != null )
        {
            String s = value.trim();
            try
            {
                return Integer.valueOf( s ).intValue();
            }
            catch ( NumberFormatException e )
            {
                log.error( "[" + s + "] is not in proper int form." );
                e.printStackTrace();
            }
        }
        return dEfault;
    }


    /** Description of the Method */
    public static long toFileSize( String value, long dEfault )
    {
        if ( value == null )
        {
            return dEfault;
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
        if ( s != null )
        {
            try
            {
                return Long.valueOf( s ).longValue() * multiplier;
            }
            catch ( NumberFormatException e )
            {
                log.error( "[" + s + "] is not in proper int form" );
                log.error( "[" + value + "] not in expected format", e );
            }
        }
        return dEfault;
    }


    /**
     * Find the value corresponding to <code>key</code> in <code>props</code>.
     * Then perform variable substitution on the found value.
     */

    public static String findAndSubst( String key, Properties props )
    {
        String value = props.getProperty( key );
        if ( value == null )
        {
            return null;
        }

        try
        {
            return substVars( value, props );
        }
        catch ( IllegalArgumentException e )
        {
            log.error( "Bad option value [" + value + "]", e );
            return value;
        }
    }


    /**
     * Instantiate an object given a class name. Check that the <code>className</code>
     * is a subclass of <code>superClass</code>. If that test fails or the
     * object could not be instantiated, then <code>defaultValue</code> is
     * returned.
     *
     * @param className The fully qualified class name of the object to
     *      instantiate.
     * @param superClass The class to which the new object should belong.
     * @param defaultValue The object to return in case of non-fulfillment
     */

    public static Object instantiateByClassName( String className, Class superClass,
                                                 Object defaultValue )
    {
        if ( className != null )
        {
            try
            {
                Class classObj = Class.forName( className );
                if ( !superClass.isAssignableFrom( classObj ) )
                {
                    log.error( "A \"" + className + "\" object is not assignable to a \"" +
                        superClass.getName() + "\" variable." );
                    return defaultValue;
                }
                return classObj.newInstance();
            }
            catch ( Exception e )
            {
                log.error( "Could not instantiate class [" + className + "]", e );
            }
        }
        return defaultValue;
    }

    /**
     * Perform variable substitution in string <code>val</code> from the values
     * of keys found in the system propeties. <p>
     *
     * The variable substitution delimeters are <b>${</b> and <b>}</b> . <p>
     *
     * For example, if the System properties contains "key=value", then the call
     * <pre>
     *String s = OptionConverter.substituteVars("Value of key is ${key}.");
     *</pre> will set the variable <code>s</code> to "Value of key is value.".
     * <p>
     *
     * If no value could be found for the specified key, then the <code>props</code>
     * parameter is searched, if the value could not be found there, then
     * substitution defaults to the empty string. <p>
     *
     * For example, if system propeties contains no value for the key
     * "inexistentKey", then the call <pre>
     *String s = OptionConverter.subsVars("Value of inexistentKey is [${inexistentKey}]");
     *</pre> will set <code>s</code> to "Value of inexistentKey is []" <p>
     *
     * An {@link java.lang.IllegalArgumentException} is thrown if <code>val</code>
     * contains a start delimeter "${" which is not balanced by a stop delimeter
     * "}". </p> <p>
     *
     * <b>Author</b> Avy Sharell</a> </p>
     *
     * @param val The string on which variable substitution is performed.
     * @throws IllegalArgumentException if <code>val</code> is malformed.
     */

    public static String substVars( String val, Properties props )
        throws
        IllegalArgumentException
    {
        sbuf.setLength( 0 );

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
                else
                {
                    sbuf.append( val.substring( i, val.length() ) );
                    return sbuf.toString();
                }
            }
            else
            {
                sbuf.append( val.substring( i, j ) );
                k = val.indexOf( DELIM_STOP, j );
                if ( k == -1 )
                {
                    throw new IllegalArgumentException( '"' + val +
                        "\" has no closing brace. Opening brace at position " + j
                         + '.' );
                }
                else
                {
                    j += DELIM_START_LEN;
                    String key = val.substring( j, k );
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
    }

}
// end class
