package org.apache.commons.jcs4.utils.serialization;

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

import java.io.IOException;
import java.time.Duration;

import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Performs JSON serialization and de-serialization.
 */
public class JSONSerializer
    implements IElementSerializer
{
    /** Jackson JSON mapper instance */
    private static final ObjectMapper mapper;

    static
    {
        mapper = JsonMapper.builder()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                .build();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule jcsModule = new SimpleModule();
        jcsModule.addDeserializer(IElementAttributes.class, new IElementAttributesDeserializer());
        jcsModule.addSerializer(IElementAttributes.class, new IElementAttributesSerializer());
        jcsModule.addDeserializer(CacheElement.class, new CacheElementDeserializer<>());
        jcsModule.addSerializer(CacheElement.class, new CacheElementSerializer());
        mapper.registerModule(jcsModule);
    }

    /**
     * Returns the singleton instance of the ObjectMapper for further customization
     *
     * @return the mapper
     */
    public static ObjectMapper getObjectMapper()
    {
        return mapper;
    }

    /** Wrapper to save the class name information */
    private record Wrapper<T>(String className, T element) {}

    private static <T> Class<T> getClazz(String className, ClassLoader loader) throws IOException
    {
        try
        {
            @SuppressWarnings("unchecked") // Need to cast from Object
            Class<T> clazz = (Class<T>) Class.forName(className, false, loader);

            return clazz;
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Custom deserializer for IElementAttributes
     */
    private static class IElementAttributesDeserializer extends StdDeserializer<IElementAttributes>
    {
        private static final long serialVersionUID = 452815375027560351L;

        public IElementAttributesDeserializer()
        {
            this(null);
        }

        public IElementAttributesDeserializer(Class<?> vc)
        {
            super(vc);
        }

        @Override
        public IElementAttributes deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException
        {
            ElementAttributes defaults = ElementAttributes.defaults();
            JsonNode node = jp.getCodec().readTree(jp);
            boolean isSpool = node.get("spool").asBoolean(defaults.IsSpool());
            boolean isLateral = node.get("lateral").asBoolean(defaults.IsLateral());
            boolean isRemote = node.get("remote").asBoolean(defaults.IsRemote());
            boolean isEternal = node.get("eternal").asBoolean(defaults.IsEternal());
            String maxLifeString = node.get("maxLife").asText(defaults.MaxLife().toString());
            Duration maxLife = Duration.parse(maxLifeString);
            String maxIdleTimeString = node.get("maxIdleTime").asText(defaults.MaxIdleTime().toString());
            Duration maxIdleTime = Duration.parse(maxIdleTimeString);

            return new ElementAttributes(isSpool, isLateral, isRemote, isEternal, maxLife, maxIdleTime);
        }
    }

    /**
     * Custom serializer for IElementAttributes
     */
    private static class IElementAttributesSerializer extends StdSerializer<IElementAttributes>
    {
        private static final long serialVersionUID = 452815375027560351L;

        public IElementAttributesSerializer()
        {
            this(null);
        }

        public IElementAttributesSerializer(Class<IElementAttributes> vc)
        {
            super(vc);
        }

        @Override
        public void serialize(IElementAttributes value, JsonGenerator gen, SerializerProvider provider)
                throws IOException
        {
            gen.writeStartObject();
            gen.writeBooleanField("spool", value.IsSpool());
            gen.writeBooleanField("lateral", value.IsLateral());
            gen.writeBooleanField("remote", value.IsRemote());
            gen.writeBooleanField("eternal", value.IsEternal());
            gen.writeStringField("maxLife", value.MaxLife().toString());
            gen.writeStringField("maxIdleTime", value.MaxIdleTime().toString());
            gen.writeEndObject();
        }
    }

    /**
     * Custom deserializer for CacheElement
     */
    private static class CacheElementDeserializer<K, V> extends StdDeserializer<CacheElement<K, V>>
    {
        private static final long serialVersionUID = 452815375027560351L;

        public CacheElementDeserializer()
        {
            this(null);
        }

        public CacheElementDeserializer(Class<?> vc)
        {
            super(vc);
        }

        @Override
        public CacheElement<K, V> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException
        {
            JsonNode node = jp.getCodec().readTree(jp);
            String cacheName = node.get("cacheName").asText();

            JsonNode keyNode = node.get("key");
            String keyClassName = keyNode.get("className").asText();
            Class<K> keyClazz = getClazz(keyClassName, this.getClass().getClassLoader());
            K key = mapper.treeToValue(keyNode.get("element"), keyClazz);

            JsonNode valueNode = node.get("value");
            String valueClassName = valueNode.get("className").asText();
            Class<V> valueClazz = getClazz(valueClassName, this.getClass().getClassLoader());
            V value = mapper.treeToValue(valueNode.get("element"), valueClazz);

            IElementAttributes elementAttributes = mapper.treeToValue(node.get("elementAttributes"),
                    IElementAttributes.class);
            return new CacheElement<>(cacheName, key, value, elementAttributes);
        }
    }

    /**
     * Custom serializer for CacheElement
     */
    private static class CacheElementSerializer<K, V> extends StdSerializer<CacheElement<K, V>>
    {
        private static final long serialVersionUID = 452815375027560351L;

        public CacheElementSerializer()
        {
            this(null);
        }

        public CacheElementSerializer(Class<CacheElement<K, V>> vc)
        {
            super(vc);
        }

        @Override
        public void serialize(CacheElement<K, V> value, JsonGenerator gen, SerializerProvider provider)
                throws IOException
        {
            gen.writeStartObject();
            gen.writeStringField("cacheName", value.cacheName());
            gen.writeObjectField("key", new Wrapper<K>(value.key().getClass().getName(), value.key()));
            gen.writeObjectField("value", new Wrapper<V>(value.value().getClass().getName(), value.value()));
            gen.writeObjectField("elementAttributes", value.elementAttributes());
            gen.writeEndObject();
        }
    }

    /**
     * Uses JSON de-serialization to turn a byte array into an object. All exceptions are
     * converted into IOExceptions.
     *
     * @param data data bytes
     * @param loader class loader to use
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public <T> T deSerialize(final byte[] data, final ClassLoader loader)
        throws IOException, ClassNotFoundException
    {
        if (data == null || data.length == 0)
        {
            return null;
        }

        try
        {
            JsonNode root = mapper.readTree(data);
            String className = root.at("/className").asText();
            Class<T> clazz = getClazz(className, loader == null ? this.getClass().getClassLoader() : loader);

            return mapper.treeToValue(root.at("/element"), clazz);
        }
        catch (StreamReadException | DatabindException e)
        {
            throw new IOException("Error deserializing JSON", e);
        }
    }

    /**
     * Serializes an object using JSON serialization.
     *
     * @param obj
     * @return byte[]
     * @throws IOException
     */
    @Override
    public <T> byte[] serialize(final T obj)
        throws IOException
    {
        if (obj == null)
        {
            return null;
        }

        Wrapper<T> wrapper = new Wrapper<T>(obj.getClass().getName(), obj);
        return mapper.writeValueAsBytes(wrapper);
    }
}
