package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

/**
 * Defines the behavior for cache element serializers. This layer of abstraction
 * allows us to plug in different serialization mechanisms, such as XStream.
 * 
 * @author Aaron Smuts
 * 
 */
public interface IElementSerializer
{

    /**
     * Turns an object into a byte array.
     * 
     * @param obj
     * @return
     * @throws IOException
     */
    public abstract byte[] serialize( Serializable obj )
        throws IOException;

    /**
     * Turns a byte array into an object.
     * 
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException thrown if we don't know the object.
     */
    public abstract Object deSerialize( byte[] bytes )
        throws IOException, ClassNotFoundException;

}
