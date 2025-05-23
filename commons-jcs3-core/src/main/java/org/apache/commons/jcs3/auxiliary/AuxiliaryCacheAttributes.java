package org.apache.commons.jcs3.auxiliary;

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

import java.io.Serializable;

import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;

/**
 * This is a nominal interface that auxiliary cache attributes should implement. This allows the
 * auxiliary mangers to share a common interface.
 */
public interface AuxiliaryCacheAttributes
    extends Serializable, Cloneable
{
    /**
     * Clone object
     */
    AuxiliaryCacheAttributes clone();

    /**
     * Gets the cacheName attribute of the AuxiliaryCacheAttributes object
     *
     * @return The cacheName value
     */
    String getCacheName();

    /**
     * Sets the pool name to use. If a pool is not found by this name, the thread pool manager will
     * return a default configuration.
     *
     * @return name of thread pool to use for this auxiliary
     */
    String getEventQueuePoolName();

    /**
     * @return SINGLE or POOLED
     */
    ICacheEventQueue.QueueType getEventQueueType();

    /**
     * Gets the name attribute of the AuxiliaryCacheAttributes object
     *
     * @return The name value
     */
    String getName();

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s The new cacheName value
     */
    void setCacheName( String s );

    /**
     * If you choose a POOLED event queue type, the value of EventQueuePoolName will be used. This
     * is ignored if the pool type is SINGLE
     *
     * @param s SINGLE or POOLED
     */
    void setEventQueuePoolName( String s );

    /**
     * SINGLE is the default. If you choose POOLED, the value of EventQueuePoolName will be used
     *
     * @param s SINGLE or POOLED
     */
    void setEventQueueType( ICacheEventQueue.QueueType s );

    /**
     * Name known by configurator
     *
     * @param s The new name value
     */
    void setName( String s );
}
