package org.apache.jcs.utils.data;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

