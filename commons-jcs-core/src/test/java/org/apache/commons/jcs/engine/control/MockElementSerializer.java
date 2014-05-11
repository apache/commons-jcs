package org.apache.commons.jcs.engine.control;

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

import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

import java.io.IOException;

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
    @Override
    public <T> T deSerialize( byte[] bytes, ClassLoader loader )
        throws IOException, ClassNotFoundException
    {
        deSerializeCount++;
        return serializer.deSerialize( bytes, loader );
    }

    /**
     * @param obj
     * @return byte[]
     * @throws IOException
     *
     */
    @Override
    public <T> byte[] serialize( T obj )
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
