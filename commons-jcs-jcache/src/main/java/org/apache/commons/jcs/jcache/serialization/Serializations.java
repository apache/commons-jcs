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
package org.apache.commons.jcs.jcache.serialization;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Proxy;

public class Serializations
{
    public static <K extends Serializable> K copy(final ClassLoader loader, final K key)
    {
        try
        {
            return deSerialize(serialize(key), loader);
        }
        catch ( final Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static  <T extends Serializable> byte[] serialize( T obj ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }

    private static  <T extends Serializable> T deSerialize( final byte[] data, final ClassLoader loader ) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( data );
        BufferedInputStream bis = new BufferedInputStream( bais );
        ObjectInputStream ois = new ObjectInputStreamClassLoaderAware( bis, loader );
        try
        {
            return (T) ois.readObject();
        }
        finally
        {
            ois.close();
        }
    }

    private static class ObjectInputStreamClassLoaderAware extends ObjectInputStream
    {
        private final ClassLoader classLoader;

        public ObjectInputStreamClassLoaderAware(final InputStream in, final ClassLoader classLoader) throws IOException
        {
            super(in);
            this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        }

        @Override
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws ClassNotFoundException
        {
            return Class.forName(desc.getName(), false, classLoader);
        }

        @Override
        protected Class resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException
        {
            final Class[] cinterfaces = new Class[interfaces.length];
            for (int i = 0; i < interfaces.length; i++)
            {
                cinterfaces[i] = Class.forName(interfaces[i], false, classLoader);
            }

            try
            {
                return Proxy.getProxyClass(classLoader, cinterfaces);
            }
            catch (IllegalArgumentException e)
            {
                throw new ClassNotFoundException(null, e);
            }
        }

    }
}
