
package org.apache.jcs.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheUtils
{

    /** Constructor for the CacheUtils object */
    private CacheUtils() { }


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
            return ( Serializable ) ois.readObject();
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
