package org.apache.jcs.auxiliary.disk.jisp;

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

import com.coyotegulch.jisp.KeyObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *  
 */
public class JISPKey
    extends KeyObject
{
    private final static Log log = LogFactory.getLog( JISPKey.class );

    /** Description of the Field */
    public Serializable m_key;

    /**
     * Constructor for the JISPKey object
     * 
     * @param key_value
     */
    public JISPKey( Serializable key_value )
    {
        m_key = key_value;

    }

    /** Constructor for the JISPKey object */
    public JISPKey()
    {
        m_key = "";
    }

    /** Description of the Method */
    public int compareTo( KeyObject key )
    {

        if ( key instanceof JISPKey )
        {
            //int orig = ( ( JISPKey ) key ).m_key.hashCode();
            int orig = m_key.hashCode();
            int test = key.hashCode();

            int cv = test - orig;
            if ( cv == 0 )
            {
                return KEY_EQUAL;
            }
            else if ( cv < 0 )
            {
                return KEY_LESS;
            }
            else
            {
                return KEY_MORE;
            }
        }
        return KEY_ERROR;
    }

    /** Description of the Method */
    public KeyObject makeNullKey()
    {
        JISPKey nullKey = new JISPKey();
        nullKey.m_key = "0xFFFFFFFF";
        return nullKey;
    }

    /** Description of the Method */
    public void writeExternal( ObjectOutput out )
        throws IOException
    {
        out.writeObject( m_key );
    }

    /** Description of the Method */
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        m_key = ( (Serializable) ( in.readObject() ) );
    }

    //    public int intValue()
    //    {
    //        return m_key;
    //    }

    /** Description of the Method */
    public String toString()
    {
        return m_key.toString();
    }

    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( ( obj != null ) && ( obj instanceof JISPKey ) )
        {
            int orig = m_key.hashCode();
            int test = ( (JISPKey) obj ).m_key.hashCode();

            return ( orig == test );
            //return ( m_key.equals( ( ( JISPKey ) obj ).m_key ) );
        }
        return false;
    }

    /** Description of the Method */
    public int hashCode()
    {
        int hash = m_key.hashCode();

        return hash;
        // new Integer(m_key).hashCode();
    }
}
