package org.apache.jcs.engine;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Description of the Class
 *  
 */
public class CacheUtils
{

    /** Constructor for the CacheUtils object */
    private CacheUtils()
    {
    }

    /** Returns a deeply cloned object. */
    public static Serializable dup( Serializable obj )
        throws IOException
    {
        return deserialize( serialize( obj ) );
    }

    /**
     * Returns the serialized form of the given object in a byte array.
     */
    public static byte[] serialize( Serializable obj )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }

    /** Returns the object deserialized from the given byte array. */
    public static Serializable deserialize( byte[] buf )
        throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( buf );
        ObjectInputStream ois = new ObjectInputStream( bais );
        try
        {
            return (Serializable) ois.readObject();
        }
        catch ( ClassNotFoundException ex )
        {
            // impossible case.
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
        finally
        {
            ois.close();
        }
    }

}
// end class
