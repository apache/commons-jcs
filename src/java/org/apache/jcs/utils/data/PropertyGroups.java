package org.apache.jcs.utils.data;

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * represent a file from the classpath, such as
 * C:\JRun\jsm-default\classes\tst.properties which we load via
 * load("\tst.properties") as a group of Properties in a hashtable; thus
 * alpha_zip=1111 beta_zip=2222 gamma_zip=3333 alpha_zap=uggle wurple=wing
 * beta_zap=wuggle zurple=zing becomes a PropertyGroups with Enumeration
 * propertyKeys()=[alpha,beta,gamma] Enumeration simpleKeys()=[wurple,zurple]
 * Properties getProperties("alpha") = {zip=1111,zap=uggle} String
 * getProperty("wurple")=wing. String getProperty("alpha","bibble")=bibble It is
 * an error to define a key both as a group name and a property: alpha=stringval
 * would be an error; it would conflict with alpha_zip or alpha_zap. it is not
 * an error to ask for a property whose name is a group name, but the answer is
 * null.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class PropertyGroups extends Hashtable
{

    private Properties props = null;
    private String fileName = null;
    int simpleKeys;
    int compoundKeys;


    /** Constructor for the PropertyGroups object */
    public PropertyGroups()
    {
        props = new Properties();
        simpleKeys = 0;
        compoundKeys = 0;
    }


    /**
     * Constructor for the PropertyGroups object
     *
     * @param name
     * @exception Exception
     */
    public PropertyGroups( String name )
        throws Exception
    {
        this();
        load( name );
    }


    /** Description of the Method */
    public void load( String name )
        throws Exception
    {
        fileName = name;
        load();
    }


    /** Description of the Method */
    public void load()
        throws Exception
    {
        java.io.InputStream is = getClass().getResourceAsStream( fileName );
        if ( null == is )
        {
            throw
                new Exception( "PropertyGroups.load: can't get resource " + fileName );
        }
        props.load( is );
        is.close();
        Enumeration keys = props.keys();
        while ( keys.hasMoreElements() )
        {
            String key = ( String ) keys.nextElement();
            int sloc;
            if ( 0 > ( sloc = key.indexOf( '_' ) ) )
            {
                simpleKeys++;
                put( key, props.get( key ) );
            }
            else
            {
                String key1 = key.substring( 0, sloc );
                String key2 = key.substring( 1 + sloc );
                Properties subprops = ( Properties ) get( key1 );
                if ( null == subprops )
                {
                    compoundKeys++;
                    put( key1, subprops = new Properties() );
                }
                subprops.put( key2, props.get( key ) );
            }
        }
    }


    /**
     * Gets the property attribute of the PropertyGroups object
     *
     * @return The property value
     */
    public String getProperty( String key )
    {
        Object ob = get( key );
        if ( ob instanceof String )
        {
            return ( String ) ob;
        }
        return null;
    }


    /**
     * Gets the property attribute of the PropertyGroups object
     *
     * @return The property value
     */
    public String getProperty( String key, String dflt )
    {
        if ( null == key )
        {
            return dflt;
        }
        String p = getProperty( key );
        return p == null ? dflt : p;
    }


    /**
     * Gets the properties attribute of the PropertyGroups object
     *
     * @return The properties value
     */
    public Properties getProperties( String key )
    {
        if ( null == key )
        {
            return null;
        }
        Object ob = get( key );
        if ( ob instanceof Properties )
        {
            return ( Properties ) ob;
        }
        return null;
    }


    /** Description of the Method */
    public Enumeration propertyKeys()
    {
        return new PropertyKeysEnum();
    }


    /** Description of the Method */
    public Enumeration simpleKeys()
    {
        return new SimpleKeysEnum();
    }


    /** Description of the Method */
    private void keyVal( StringBuffer sB, Object key )
    {
        String k = ( String ) key;
        sB.append( k );
        sB.append( "=" );
        sB.append( get( key ).toString() );
    }


    /** Description of the Method */
    public synchronized String toString()
    {
        StringBuffer sB = new StringBuffer();
        Enumeration pk = propertyKeys();
        if ( pk.hasMoreElements() )
        {
            keyVal( sB, pk.nextElement() );
        }
        while ( pk.hasMoreElements() )
        {
            sB.append( ", " );
            keyVal( sB, pk.nextElement() );
        }
        Enumeration sk = simpleKeys();
        if ( sk.hasMoreElements() )
        {
            keyVal( sB, sk.nextElement() );
        }
        while ( sk.hasMoreElements() )
        {
            sB.append( ", " );
            keyVal( sB, sk.nextElement() );
        }
        return sB.toString();
    }


    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
     */
    class PropertyKeysEnum implements Enumeration
    {
        int howMany;
        Enumeration baseEnum;


        /** Constructor for the PropertyKeysEnum object */
        public PropertyKeysEnum()
        {
            howMany = compoundKeys;
            baseEnum = keys();
        }


        /** Description of the Method */
        public boolean hasMoreElements()
        {
            return howMany > 0;
        }


        /** Description of the Method */
        public Object nextElement()
        {
            Object ob;
            while ( baseEnum.hasMoreElements() )
            {
                Object k = baseEnum.nextElement();
                Object v = get( k );
                if ( v instanceof Properties )
                {
                    howMany--;
                    return k;
                }
            }
            howMany = 0;
            return null;
        }

    }
    // end of PropertyKeysEnum inner class

    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
     */
    class SimpleKeysEnum implements Enumeration
    {
        int howMany;
        Enumeration baseEnum;


        /** Constructor for the SimpleKeysEnum object */
        public SimpleKeysEnum()
        {
            howMany = simpleKeys;
            baseEnum = keys();
        }


        /** Description of the Method */
        public boolean hasMoreElements()
        {
            return howMany > 0;
        }


        /** Description of the Method */
        public Object nextElement()
        {
            Object ob;
            while ( baseEnum.hasMoreElements() )
            {
                Object k = baseEnum.nextElement();
                Object v = get( k );
                if ( v instanceof String )
                {
                    howMany--;
                    return k;
                }
            }
            howMany = 0;
            return null;
        }

    }
    // end of SimpleKeysEnum inner class

}
// end PropertyGroups class

