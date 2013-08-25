package org.apache.commons.jcs.engine.behavior;

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

import java.io.Serializable;

/**
 * This interface defines the behavior of the serialized element wrapper.
 * <p>
 * The value is stored as a byte array. This should allow for a variety of serialization mechanisms.
 * <p>
 * This currently extends ICacheElement<K, V> for backward compatibility.
 *<p>
 * @author Aaron Smuts
 */
public interface ICacheElementSerialized<K extends Serializable, V extends Serializable>
    extends ICacheElement<K, V>
{
    /**
     * Gets the cacheName attribute of the ICacheElement<K, V> object. The cacheName is also known as the
     * region name.
     *<p>
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Gets the key attribute of the ICacheElementSerialized object. This is the standard key that
     * the value can be reference by.
     *<p>
     * @return The key value
     */
    public K getKey();

    /**
     * Gets the value attribute of the ICacheElementSerialized object. This is the value the client
     * cached serialized by some mechanism.
     *<p>
     * @return The serialized value
     */
    public byte[] getSerializedValue();

    /**
     * Gets the attributes attribute of the ICacheElement<K, V> object
     *<p>
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes();

    /**
     * Sets the attributes attribute of the ICacheElement<K, V> object
     *<p>
     * @param attr The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr );
}
