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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import com.coyotegulch.jisp.KeyObject;

/**
 * Description of the Class
 *  
 */
public class JISPKey
    extends KeyObject
{
    /** The key for jisp */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.coyotegulch.jisp.KeyObject#compareTo(com.coyotegulch.jisp.KeyObject)
     */
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

    /**
     * Description of the Method
     * 
     * @return
     */
    public KeyObject makeNullKey()
    {
        JISPKey nullKey = new JISPKey();
        nullKey.m_key = "0xFFFFFFFF";
        return nullKey;
    }

    /**
     * Description of the Method
     * 
     * @param out
     * @throws IOException
     */
    public void writeExternal( ObjectOutput out )
        throws IOException
    {
        out.writeObject( m_key );
    }

    /**
     * Description of the Method
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        m_key = ( (Serializable) ( in.readObject() ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return m_key.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int hash = m_key.hashCode();

        return hash;
        // new Integer(m_key).hashCode();
    }
}
