package org.apache.commons.jcs3.engine.logging.behavior;

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

/** Defines the common fields required by a cache event. */
public interface ICacheEvent<K>
    extends Serializable
{
    /**
     * @return the eventName
     */
    String getEventName();

    /**
     * @return the key
     */
    K getKey();

    /**
     * @return the optionalDetails
     */
    String getOptionalDetails();

    /**
     * @return the region
     */
    String getRegion();

    /**
     * @return the source
     */
    String getSource();

    /**
     * @param eventName the eventName to set
     */
    void setEventName( String eventName );

    /**
     * @param key the key to set
     */
    void setKey( K key );

    /**
     * @param optionalDetails the optionalDetails to set
     */
    void setOptionalDetails( String optionalDetails );

    /**
     * @param region the region to set
     */
    void setRegion( String region );

    /**
     * @param source the source to set
     */
    void setSource( String source );
}
