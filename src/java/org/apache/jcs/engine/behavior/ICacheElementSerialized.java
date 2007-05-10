package org.apache.jcs.engine.behavior;

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
 * The value is stored as a byte array. This should allow for a variety of
 * serialization mechanisms.
 * <p>
 * This currently extends ICacheElement for backward compatibility.
 *
 * @author Aaron Smuts
 *
 */
public interface ICacheElementSerialized
    extends ICacheElement
{

    /**
     * Gets the cacheName attribute of the ICacheElement object. The cacheName
     * is also known as the region name.
     *
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Gets the key attribute of the ICacheElementSerialized object. This is the
     * standard key that the value can be reference by.
     *
     * @return The key value
     */
    public Serializable getKey();

    /**
     * Gets the value attribute of the ICacheElementSerialized object. This is
     * the value the client cached seriailzed by some mechanism.
     *
     * @return The serialized value
     */
    public byte[] getSerializedValue();

    /**
     * Gets the attributes attribute of the ICacheElement object
     *
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes();

    /**
     * Sets the attributes attribute of the ICacheElement object
     *
     * @param attr
     *            The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr );

}
