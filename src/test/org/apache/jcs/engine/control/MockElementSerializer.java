package org.apache.jcs.engine.control;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.utils.serialization.StandardSerializer;

/** For mocking. */
public class MockElementSerializer
    implements IElementSerializer
{
    /** test property */
    private String testProperty;
    
    private StandardSerializer serializer = new StandardSerializer();    
    
    /**
     * @param bytes 
     * @return Object
     * @throws IOException 
     * @throws ClassNotFoundException 
     * 
     */
    public Object deSerialize( byte[] bytes )
        throws IOException, ClassNotFoundException
    {
        return serializer.deSerialize( bytes );
    }

    /**
     * @param obj 
     * @return byte[]
     * @throws IOException 
     * 
     */
    public byte[] serialize( Serializable obj )
        throws IOException
    {
        return serializer.serialize( obj );
    }
    
    /**
     * @param testProperty
     */
    public void setTestProperty( String testProperty )
    {
        this.testProperty = testProperty;
    }

    /**
     * @return testProperty
     */
    public String getTestProperty()
    {
        return testProperty;
    }    
}
