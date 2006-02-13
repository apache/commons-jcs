package org.apache.jcs.utils.serialization;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.IElementSerializer;

/**
 * Tests the serialization conversion util.
 * 
 * @author Aaron Smuts
 * 
 */
public class SerializationConversionUtilUnitTest
    extends TestCase
{

    /**
     * Verify that we can go back and forth with the simplest of objects.
     * 
     * @throws Exception
     */
    public void testSimpleConversion()
        throws Exception
    {
        String cacheName = "testName";
        String key = "key";
        String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        IElementSerializer elementSerializer = new StandardSerializer();

        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( 34 );

        ICacheElement before = new CacheElement( cacheName, key, value );
        before.setElementAttributes( attr );

        ICacheElementSerialized serialized = SerializationConversionUtil.getSerializedCacheElement( before,
                                                                                                    elementSerializer );
        assertNotNull( "Should have a serialized object.", serialized );
        System.out.println( "testSimpleConversion, " + serialized );

        ICacheElement after = SerializationConversionUtil.getDeSerializedCacheElement( serialized, elementSerializer );

        assertNotNull( "Should have a deserialized object.", after );
        assertEquals( "Values should be the same.", before.getVal(), after.getVal() );
        assertEquals( "Attributes should be the same.", before.getElementAttributes().getMaxLifeSeconds(), after
            .getElementAttributes().getMaxLifeSeconds() );
        assertEquals( "Keys should be the same.", before.getKey(), after.getKey() );
        assertEquals( "Cache name should be the same.", before.getCacheName(), after.getCacheName() );

    }

    /**
     * Verify that we get an IOException for a null serializer.
     * 
     * @throws Exception
     */
    public void testNullSerializerConversion()
    {
        String cacheName = "testName";
        String key = "key";
        String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        IElementSerializer elementSerializer = null;// new StandardSerializer();

        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( 34 );

        ICacheElement before = new CacheElement( cacheName, key, value );
        before.setElementAttributes( attr );

        try
        {
            SerializationConversionUtil.getSerializedCacheElement( before, elementSerializer );
            fail( "We should have received an IOException." );
        }
        catch ( IOException e )
        {
            // expected
        }

    }

}
