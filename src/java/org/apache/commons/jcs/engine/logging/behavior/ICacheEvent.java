package org.apache.commons.jcs.engine.logging.behavior;

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
public interface ICacheEvent<K extends Serializable>
    extends Serializable
{
    /**
     * @param source the source to set
     */
    public void setSource( String source );

    /**
     * @return the source
     */
    public String getSource();

    /**
     * @param region the region to set
     */
    public void setRegion( String region );

    /**
     * @return the region
     */
    public String getRegion();

    /**
     * @param eventName the eventName to set
     */
    public void setEventName( String eventName );

    /**
     * @return the eventName
     */
    public String getEventName();

    /**
     * @param optionalDetails the optionalDetails to set
     */
    public void setOptionalDetails( String optionalDetails );

    /**
     * @return the optionalDetails
     */
    public String getOptionalDetails();

    /**
     * @param key the key to set
     */
    public void setKey( K key );

    /**
     * @return the key
     */
    public K getKey();
}
