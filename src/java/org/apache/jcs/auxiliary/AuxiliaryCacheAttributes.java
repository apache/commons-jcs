package org.apache.jcs.auxiliary;

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
 * This is a nominal interface that auxiliary cache attributes should
 * implement. This allows the auxiliary mangers to share a common interface.
 *
 */
public interface AuxiliaryCacheAttributes
    extends Cloneable, Serializable
{

    /**
     * Does not use a thread pool.
     */
    public static final String SINGLE_QUEUE_TYPE = "SINGLE";

    /**
     * Uses a thread pool
     */
    public static final String POOLED_QUEUE_TYPE = "POOLED";

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s
     *            The new cacheName value
     */
    public void setCacheName( String s );

    /**
     * Gets the cacheName attribute of the AuxiliaryCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Name known by by configurator
     *
     * @param s
     *            The new name value
     */
    public void setName( String s );

    /**
     * Gets the name attribute of the AuxiliaryCacheAttributes object
     *
     * @return The name value
     */
    public String getName();

    /**
     * SINGLE is the default. If you choose POOLED, the value of
     * EventQueuePoolName will be used
     *
     * @param s
     *            SINGLE or POOLED
     * @return
     */
    public void setEventQueueType( String s );

    /**
     *
     * @return SINGLE or POOLED
     */
    public String getEventQueueType();

    /**
     * Returns the value used by the factory.
     *
     * @return
     */
    public int getEventQueueTypeFactoryCode();

    /**
     * If you choose a POOLED event queue type, the value of EventQueuePoolName
     * will be used. This is ignored if the pool type is SINGLE
     *
     * @param s
     *            SINGLE or POOLED
     * @return
     */
    public void setEventQueuePoolName( String s );

    /**
     * Sets the pool name to use. If a pool is not found by this name, the
     * thread pool manager will return a default configuration.
     *
     * @return name of thread pool to use for this auxiliary
     */
    public String getEventQueuePoolName();

    /**
     * Description of the Method
     *
     * @return
     */
    public AuxiliaryCacheAttributes copy();

}
