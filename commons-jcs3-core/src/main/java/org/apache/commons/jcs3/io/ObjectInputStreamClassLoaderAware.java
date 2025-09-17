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
package org.apache.commons.jcs3.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * ObjectInputStream implementation that allows to specify a class loader for deserializing
 * objects
 *
 * The class also evaluates the system property {@code jcs.serialization.class.filter}
 * to define a list of classes that are allowed to be de-serialized. The filter value
 * is directly fed into {@link java.io.ObjectInputFilter.Config#createFilter(String)}
 * See the syntax documentation there.
 */
public class ObjectInputStreamClassLoaderAware extends ObjectInputStream
{
    public static final String SYSTEM_PROPERTY_SERIALIZATION_FILTER = "jcs.serialization.class.filter";

    private static final String filter = System.getProperty(
	SYSTEM_PROPERTY_SERIALIZATION_FILTER,
        "!org.codehaus.groovy.runtime.**;!org.apache.commons.collections.functors.**;!org.apache.xalan*");

    private final ClassLoader classLoader;

    public ObjectInputStreamClassLoaderAware(final InputStream in, final ClassLoader classLoader) throws IOException
    {
        super(in);
        setObjectInputFilter(ObjectInputFilter.Config.createFilter(filter));
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws ClassNotFoundException {
        return Class.forName(desc.getName(), false, classLoader);
    }

    @Override
    protected Class<?> resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException {
        final Class<?>[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            cinterfaces[i] = Class.forName(interfaces[i], false, classLoader);
        }

        try {
            return Proxy.getProxyClass(classLoader, cinterfaces);
        } catch (final IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}
