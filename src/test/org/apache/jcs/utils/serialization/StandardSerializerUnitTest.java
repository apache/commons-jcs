package org.apache.jcs.utils.serialization;

import junit.framework.TestCase;

/**
 * Tests the standard serializer.
 * 
 * @author Aaron Smuts
 * 
 */
public class StandardSerializerUnitTest
    extends TestCase
{

    /**
     * Test simple back and forth with a string.
     * 
     * @throws Exception
     * 
     */
    public void testSimpleBackAndForth()
        throws Exception
    {
        StandardSerializer serializer = new StandardSerializer();

        String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";

        String after = (String) serializer.deSerialize( serializer.serialize( before ) );

        assertEquals( "Before and after should be the same.", before, after );
    }

    /**
     * Test serialization with a null object.  Verify that we don't get an error.
     * 
     * @throws Exception
     * 
     */
    public void testNullInput()
        throws Exception
    {
        StandardSerializer serializer = new StandardSerializer();

        String before = null;

        byte[] serialized = serializer.serialize( before );

        System.out.println( "testNullInput " + serialized );
        
        String after = (String) serializer.deSerialize( serialized );

        System.out.println( "testNullInput " + after );

        assertNull( "Should have nothing.", after );

    }
}
