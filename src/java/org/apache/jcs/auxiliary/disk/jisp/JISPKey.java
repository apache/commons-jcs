package org.apache.jcs.auxiliary.disk.jisp;

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
 * @author asmuts
 * @created January 15, 2002
 */
public class JISPKey extends KeyObject
{
    private final static Log log =
        LogFactory.getLog( JISPKey.class );

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
            /*
             * for string
             * int cv = ((JISPKey)key).m_key.compareTo(m_key);
             * if ( cv == 0  )  {
             * return KEY_EQUAL;
             * } else if ( cv < 0  )  {
             * return KEY_LESS;
             * } else {
             * return KEY_MORE;
             * }
             */
        }
        else
        {
            return KEY_ERROR;
        }
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
        m_key = ( ( Serializable ) ( in.readObject() ) );
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
            int test = ( ( JISPKey ) obj ).m_key.hashCode();

            return ( orig == test );
            //return ( m_key.equals( ( ( JISPKey ) obj ).m_key ) );
        }
        else
        {
            return false;
        }
    }


    /** Description of the Method */
    public int hashCode()
    {
        int hash = m_key.hashCode();

        return hash;
        // new Integer(m_key).hashCode();
    }
}
