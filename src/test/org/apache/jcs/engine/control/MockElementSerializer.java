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

    /** What's used in the background */
    private final StandardSerializer serializer = new StandardSerializer();

    /** times out was called */
    public int deSerializeCount = 0;

    /** times in was called */
    public int serializeCount = 0;

    /**
     * @param bytes
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException
     *
     */
    public <T extends Serializable> T deSerialize( byte[] bytes )
        throws IOException, ClassNotFoundException
    {
        deSerializeCount++;
        return serializer.deSerialize( bytes );
    }

    /**
     * @param obj
     * @return byte[]
     * @throws IOException
     *
     */
    public <T extends Serializable> byte[] serialize( T obj )
        throws IOException
    {
        serializeCount++;
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
