package org.apache.commons.jcs4.utils.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.TestElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verify that serializer functionality works.
 */
class SerializerUnitTest
{
    /**
     * Test setup
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestElementSerializer.ccf" );
    }

    @AfterAll
    public static void tearDown()
        throws Exception
    {
        JCS.shutdown();
    }

    /**
     * Verify that object reading and writing with different IElementSerializer implementations works
     *
     * @throws Exception
     */
    @ParameterizedTest
    @ValueSource(strings = {"blockRegion1", "blockRegion2", "blockRegion3"})
    public void testReadWriteSerializer(String cacheRegion)
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance(cacheRegion);
        final int count = 500; // 100 fit in memory

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( "key:" + i, "data" + i );
        }

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs.get( "key:" + i );
            assertNotNull( res, "[key:" + i + "] should not be null, " + jcs.getStatistics() );
        }

        Thread.sleep(500); // Give some time to empty the write queue
        JCS.shutdown();

        // Re-init
        final CacheAccess<String, String> jcs2 = JCS.getInstance(cacheRegion);

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs2.get( "key:" + i );
            assertNotNull( res, "[key:" + i + "] should not be null, " + jcs2.getStatistics() );
        }
    }

    private static Stream<Arguments> provideSerializer() {

        StandardSerializer stdSerializer = new StandardSerializer();
        CompressingSerializer compressingSerializer = new CompressingSerializer();
        EncryptingSerializer encryptingSerializer = new EncryptingSerializer();
        encryptingSerializer.setPreSharedKey("my_secret_key");
        JSONSerializer jsonSerializer = new JSONSerializer();

        return Stream.of(
          Arguments.of(stdSerializer),
          Arguments.of(compressingSerializer),
          Arguments.of(encryptingSerializer),
          Arguments.of(jsonSerializer)
        );
    }

    /**
     * Test serialization with a null object. Verify that we don't get an error.
     *
     * @param serializer the serializer object
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testNullInput(IElementSerializer serializer)
        throws Exception
    {
        final String before = null;

        // DO WORK
        final byte[] serialized = serializer.serialize( before );
        //System.out.println( "testNullInput " + serialized );

        final String after = serializer.deSerialize( serialized, null );
        //System.out.println( "testNullInput " + after );

        // VERIFY
        assertNull( after, "Should have nothing." );
    }

    /**
     * Verify that we don't get any errors for null input.
     *
     * @param serializer the serializer object
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testDeserialize_NullInput(IElementSerializer serializer)
        throws IOException, ClassNotFoundException
    {
        // DO WORK
        final Object result = serializer.deSerialize( null, null );

        // VERIFY
        assertNull( result, "Should have nothing." );
    }

    /**
     * Test simple back and forth with a string.
     * <p>
     * ))&lt;=&gt;((
     *
     * @param serializer the serializer object
     * @throws Exception on error
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testSimpleBackAndForth(IElementSerializer serializer)
        throws Exception
    {
        // DO WORK
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }

    /**
     * Test simple back and forth with a string.
     *
     * @param serializer the serializer object
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testBigStringBackAndForth(IElementSerializer serializer)
        throws Exception
    {
        final String string = "This is my big string ABCDEFGH";
        final StringBuilder sb = new StringBuilder();
        sb.append( string );
        for ( int i = 0; i < 4; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        final String before = sb.toString();

        // DO WORK
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }

    public static record Person(String name, int age, Date birthdate) implements Serializable {}

    /**
     * Test simple back and forth with an object.
     *
     * @param serializer the serializer object
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testObjectBackAndForth(IElementSerializer serializer)
        throws Exception
    {
        Date date = new GregorianCalendar(1977, Calendar.DECEMBER, 15).getTime();
        final Person before = new Person("joe", 21, date);

        // DO WORK
        byte[] serialized = serializer.serialize(before);
        // System.out.println(new String(serialized, StandardCharsets.UTF_8));
        final Person after = serializer.deSerialize(serialized, null);

        // VERIFY
        assertEquals(before.name(), after.name(), "Before and after should be the same.");
        assertEquals(before.age(), after.age(), "Before and after should be the same.");
        assertEquals(before.birthdate(), after.birthdate(), "Before and after should be the same.");
    }

    /**
     * Test CacheElement back and forth.
     *
     * @param serializer the serializer object
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("provideSerializer")
    void testCacheElementBackAndForth(IElementSerializer serializer)
        throws Exception
    {
        final CacheElement<Integer, Person> before = new CacheElement<>("testCache", Integer.valueOf(4),
                new Person("joe", 21, new GregorianCalendar(1977, Calendar.DECEMBER, 15).getTime()),
                new ElementAttributes(false, true, false, true, Duration.ofMinutes(2), Duration.ofSeconds(65)));

        // DO WORK
        byte[] serialized = serializer.serialize(before);
        final CacheElement<Integer, Person> after = serializer.deSerialize(serialized, null);

        // VERIFY
        assertEquals(before.cacheName(), after.cacheName(), "Before and after should be the same.");
        assertEquals(before.key(), after.key(), "Before and after should be the same.");
        assertEquals(before.value(), after.value(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().IsSpool(), after.elementAttributes().IsSpool(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().IsLateral(), after.elementAttributes().IsLateral(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().IsRemote(), after.elementAttributes().IsRemote(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().IsEternal(), after.elementAttributes().IsEternal(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().MaxLife(), after.elementAttributes().MaxLife(), "Before and after should be the same.");
        assertEquals(before.elementAttributes().MaxIdleTime(), after.elementAttributes().MaxIdleTime(), "Before and after should be the same.");
   }

    /**
     * Verify that we can go back and forth with the simplest of objects.
     *
     * @throws Exception
     */
    @Test
    void testAccidentalDoubleConversion()
        throws Exception
    {
        // SETUP
        final String cacheName = "testName";
        final String key = "key";
        final String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        final IElementSerializer elementSerializer = new StandardSerializer();
        final ElementAttributes attr = TestElementAttributes
                .withEternalFalseAndMaxLife(Duration.ofSeconds(34));

        final ICacheElement<String, String> before = new CacheElement<>(cacheName, key,
                value, attr);

        // DO WORK
        final ICacheElementSerialized<String, String> alreadySerialized =
                elementSerializer.getSerializedCacheElement(before);
        final ICacheElementSerialized<String, String> serialized =
                elementSerializer.getSerializedCacheElement(alreadySerialized);

        // VERIFY
        assertNotNull(serialized, "Should have a serialized object.");

        // DO WORK
        final ICacheElement<String, String> after = elementSerializer.getDeSerializedCacheElement(serialized);

        // VERIFY
        assertNotNull( after, "Should have a deserialized object." );
        assertEquals( before.value(), after.value(), "Values should be the same." );
        assertEquals( before.elementAttributes().MaxLife(), after
            .elementAttributes().MaxLife(), "Attributes should be the same." );
        assertEquals( before.key(), after.key(), "Keys should be the same." );
        assertEquals( before.cacheName(), after.cacheName(), "Cache name should be the same." );
    }

    /**
     * Verify null for null.
     *
     * @throws Exception
     */
    @Test
    void testGetDeSerializedCacheElement_null()
        throws Exception
    {
        // SETUP
        final IElementSerializer elementSerializer = new StandardSerializer();
        final ICacheElementSerialized<String, String> before = null;

        // DO WORK
        final ICacheElement<String, String> result = elementSerializer.getDeSerializedCacheElement(before);

        // VERIFY
        assertNull( result, "Should get null for null" );
    }

    /**
     * Verify null for null.
     *
     * @throws IOException
     */
    @Test
    void testgGetSerializedCacheElement_null()
        throws IOException
    {
        // SETUP
        final IElementSerializer elementSerializer = new StandardSerializer();
        final ICacheElement<String, String> before = null;

        // DO WORK
        final ICacheElementSerialized<String, String> result = elementSerializer.getSerializedCacheElement(before);

        // VERIFY
        assertNull( result, "Should get null for null" );
    }

    /**
     * Verify that we can go back and forth with the simplest of objects.
     *
     * @throws Exception
     */
    @Test
    void testSimpleConversion()
        throws Exception
    {
        // SETUP
        final String cacheName = "testName";
        final String key = "key";
        final String value = "value fdsadf dsafdsa fdsaf dsafdsaf dsafdsaf dsaf dsaf dsaf dsafa dsaf dsaf dsafdsaf";

        final IElementSerializer elementSerializer = new StandardSerializer();
        final ElementAttributes attr = TestElementAttributes
                .withEternalFalseAndMaxLife(Duration.ofSeconds(34));

        final ICacheElement<String, String> before = new CacheElement<>(cacheName, key,
                value, attr);

        // DO WORK
        final ICacheElementSerialized<String, String> serialized =
                elementSerializer.getSerializedCacheElement(before);

        // VERIFY
        assertNotNull( serialized, "Should have a serialized object." );

        // DO WORK
        final ICacheElement<String, String> after =
                elementSerializer.getDeSerializedCacheElement(serialized);

        // VERIFY
        assertNotNull( after, "Should have a deserialized object." );
        assertEquals( before.value(), after.value(), "Values should be the same." );
        assertEquals( before.elementAttributes().MaxLife(), after
            .elementAttributes().MaxLife(), "Attributes should be the same." );
        assertEquals( before.key(), after.key(), "Keys should be the same." );
        assertEquals( before.cacheName(), after.cacheName(), "Cache name should be the same." );
    }
}
