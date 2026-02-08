package org.apache.commons.jcs4.engine.behavior;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs4.engine.control.event.behavior.IElementEventHandler;

/**
 * Interface for cache element attributes classes. Every item is the cache is associated with an
 * element attributes object. It is used to track the life of the object as well as to restrict its
 * behavior. By default, elements get a clone of the region's attributes.
 */
public interface IElementAttributes extends Serializable
{
    /**
     * Adds a ElementEventHandler. Handler's can be registered for multiple events. A registered
     * handler will be called at every recognized event.
     * @param eventHandler The feature to be added to the ElementEventHandler
     */
    void addElementEventHandler( IElementEventHandler eventHandler );

    /**
     * Sets the eventHandlers of the IElementAttributes object
     * @param eventHandlers value
     */
    void addElementEventHandlers( List<IElementEventHandler> eventHandlers );

    /**
     * Gets the elementEventHandlers.
     * <p>
     * Event handlers are transient. The only events defined are in memory events. All handlers are
     * lost if the item goes to disk.
     * @return The elementEventHandlers value, null if there are none
     */
    ArrayList<IElementEventHandler> elementEventHandlers();

    /**
     * Gets the createTime attribute of the IAttributes object.
     * <p>
     * This should be the current time in milliseconds returned by the sysutem call when the element
     * is put in the cache.
     * <p>
     * Putting an item in the cache overrides any existing items.
     * @return The createTime value
     */
    long createTime();

    /**
     * Gets the idleTime attribute of the IAttributes object
     * @return The idleTime value
     */
    long maxIdleTime();

    /**
     * This turns off expiration if it is true.
     * @return The IsEternal value
     */
    boolean isEternal();

    /**
     * Is this item laterally distributable. Can it be sent to auxiliaries of type lateral.
     * <p>
     * By default this is true.
     * @return The isLateral value
     */
    boolean isLateral();

    /**
     * Can this item be sent to the remote cache.
     * <p>
     * By default this is true.
     * @return The isRemote value
     */
    boolean isRemote();

    /**
     * Can this item be spooled to disk
     * <p>
     * By default this is true.
     * @return The spoolable value
     */
    boolean isSpool();

    /**
     * Gets the LastAccess attribute of the IAttributes object.
     *
     * @return The LastAccess value.
     */
    long lastAccessTime();

    /**
     * Sets the maxLife attribute of the IAttributes object. How many seconds it can live after
     * creation.
     * <p>
     * If this is exceeded the element will not be returned, instead it will be removed. It will be
     * removed on retrieval, or removed actively if the memory shrinker is turned on.
     * @return The MaxLifeSeconds value
     */
    long maxLife();

    /**
     * Gets the size attribute of the IAttributes object
     *
     * @return The size value
     */
    int size();

    /**
     * Get the time factor to convert durations to milliseconds
     * @return The time factor to convert durations to milliseconds
     */
    long timeFactorForMilliseconds();

    /**
     * Sets the LastAccessTime as now of the IElementAttributes object
     */
    void setLastAccessTimeNow();
}
