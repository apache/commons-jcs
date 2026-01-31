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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;

import org.apache.commons.jcs4.engine.behavior.IElementSerializer;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Performs JSON serialization and de-serialization.
 */
public class JSONSerializer
    implements IElementSerializer
{
    /** Jackson JSON mapper instance */
    private static final ObjectMapper mapper = JsonMapper.builder().build();

    /** Wrapper to save the class name information */
    private record Wrapper<T>(String className, T element) {}

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

            @SuppressWarnings("unchecked") // Need to cast from Object
            Class<T> clazz = (Class<T>)Class.forName(className, false,
                    loader == null ? this.getClass().getClassLoader() : loader);

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
