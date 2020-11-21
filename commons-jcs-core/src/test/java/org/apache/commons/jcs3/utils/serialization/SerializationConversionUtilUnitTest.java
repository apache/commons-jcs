package org.apache.commons.jcs3.utils.serialization;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.TestCase;

import java.io.IOException;

import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;

/**
 * Tests the serialization conversion util.
 * <p>
 * @author Aaron Smuts
 */
public class SerializationConversionUtilUnitTest
    extends TestCase
{
    /**
     * Verify null for null.
     * <p>
     * @throws IOException
     */
    public void testgGetSerializedCacheElement_null()
        throws IOException
    {
        // SETUP
        final IElementSerializer elementSerializer = new StandardSerializer();
        final ICacheElement<String, String> before = null;

        // DO WORK
        final ICacheElementSerialized<String, String> result =
            SerializationConversionUtil.getSerializedCacheElement( before, elementSerializer );

        // VERIFY
        assertNull( "Should get null for null", result );
    }

    /**
     * Verify null for null.
     * <p>
     * @throws Exception
     */
    public void testgGetDeSerializedCacheElement_null()
        throws Exception
    {
        // SETUP
        final IElementSerializer elementSerializer = new StandardSerializer();
        final ICacheElementSerialized<String, String> before = null;

        // DO WORK
        final ICacheElement<String, String> result =
            SerializationConversionUtil.getDeSerializedCacheElement( before, elementSerializer );

        // VERIFY
        assertNull( "Should get null for null", result );
    }

    /**
     * Verify that we can go back and forth with the simplest of objects.
     * <p>
     * @throws Exception
     */
    public void testSimpleConversion()
        throws Exception
    {
        // SETUP
        final String cacheName = "testName";
        final String key = "key";
        final String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        final IElementSerializer elementSerializer = new StandardSerializer();

        final IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(34);

        final ICacheElement<String, String> before = new CacheElement<>( cacheName, key, value );
        before.setElementAttributes( attr );

        // DO WORK
        final ICacheElementSerialized<String, String> serialized =
            SerializationConversionUtil.getSerializedCacheElement( before, elementSerializer );

        // VERIFY
        assertNotNull( "Should have a serialized object.", serialized );

        // DO WORK
        final ICacheElement<String, String> after =
            SerializationConversionUtil.getDeSerializedCacheElement( serialized, elementSerializer );

        // VERIFY
        assertNotNull( "Should have a deserialized object.", after );
        assertEquals( "Values should be the same.", before.getVal(), after.getVal() );
        assertEquals( "Attributes should be the same.", before.getElementAttributes().getMaxLife(), after
            .getElementAttributes().getMaxLife() );
        assertEquals( "Keys should be the same.", before.getKey(), after.getKey() );
        assertEquals( "Cache name should be the same.", before.getCacheName(), after.getCacheName() );
    }

    /**
     * Verify that we can go back and forth with the simplest of objects.
     *<p>
     * @throws Exception
     */
    public void testAccidentalDoubleConversion()
        throws Exception
    {
        // SETUP
        final String cacheName = "testName";
        final String key = "key";
        final String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        final IElementSerializer elementSerializer = new StandardSerializer();

        final IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(34);

        final ICacheElement<String, String> before = new CacheElement<>( cacheName, key, value );
        before.setElementAttributes( attr );

        // DO WORK
        final ICacheElementSerialized<String, String> alreadySerialized =
            SerializationConversionUtil.getSerializedCacheElement( before, elementSerializer );
        final ICacheElementSerialized<String, String> serialized =
            SerializationConversionUtil.getSerializedCacheElement( alreadySerialized, elementSerializer );

        // VERIFY
        assertNotNull( "Should have a serialized object.", serialized );

        // DO WORK
        final ICacheElement<String, String> after =
            SerializationConversionUtil.getDeSerializedCacheElement( serialized, elementSerializer );

        // VERIFY
        assertNotNull( "Should have a deserialized object.", after );
        assertEquals( "Values should be the same.", before.getVal(), after.getVal() );
        assertEquals( "Attributes should be the same.", before.getElementAttributes().getMaxLife(), after
            .getElementAttributes().getMaxLife() );
        assertEquals( "Keys should be the same.", before.getKey(), after.getKey() );
        assertEquals( "Cache name should be the same.", before.getCacheName(), after.getCacheName() );
    }

    /**
     * Verify that we get an IOException for a null serializer.
     */
    public void testNullSerializerConversion()
    {
        // SETUP
        final String cacheName = "testName";
        final String key = "key";
        final String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        final IElementSerializer elementSerializer = null;// new StandardSerializer();

        final IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(34);

        final ICacheElement<String, String> before = new CacheElement<>( cacheName, key, value );
        before.setElementAttributes( attr );

        // DO WORK
        try
        {
            SerializationConversionUtil.getSerializedCacheElement( before, elementSerializer );

            // VERIFY
            fail( "We should have received an IOException." );
        }
        catch ( final IOException e )
        {
            // expected
        }
    }
}
