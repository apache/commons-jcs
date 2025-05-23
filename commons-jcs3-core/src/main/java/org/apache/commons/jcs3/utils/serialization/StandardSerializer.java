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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.io.ObjectInputStreamClassLoaderAware;

/**
 * Performs default serialization and de-serialization.
 */
public class StandardSerializer
    implements IElementSerializer
{
    /**
     * Uses default de-serialization to turn a byte array into an object. All exceptions are
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
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStreamClassLoaderAware(bais, loader))
        {
            @SuppressWarnings("unchecked") // Need to cast from Object
            final
            T readObject = (T) ois.readObject();
            return readObject;
        }
    }

    /**
     * Serializes an object using default serialization.
     *
     * @param obj
     * @return byte[]
     * @throws IOException
     */
    @Override
    public <T> byte[] serialize(final T obj)
        throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeUnshared(obj);
        }

        return baos.toByteArray();
    }
}
