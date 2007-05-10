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

import java.util.ArrayList;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 * Interface for cache element attributes classes. Every item is the cache is
 * associated with an element attributes object. It is used to track the life of
 * the object as well as to restrict its behavior. By default, elements get a
 * clone of the region's attributes.
 *
 */
public interface IElementAttributes
{

    /**
     * Sets the version attribute of the IAttributes object
     *
     * @param version
     *            The new version value
     */
    public void setVersion( long version );

    /**
     * Sets the maxLife attribute of the IAttributes object.
     *
     * @param mls
     *            The new MaxLifeSeconds value
     */
    public void setMaxLifeSeconds( long mls );

    /**
     * Sets the maxLife attribute of the IAttributes object. How many seconds it
     * can live after creation.
     * <p>
     * If this is exceeded the element will not be returned, instead it will be
     * removed. It will be removed on retrieval, or removed actively if the
     * memory shrinker is turned on.
     *
     *
     * @return The MaxLifeSeconds value
     */
    public long getMaxLifeSeconds();

    /**
     * Sets the idleTime attribute of the IAttributes object. This is the
     * maximum time the item can be idle in the cache, that is not accessed.
     * <p>
     * If this is exceeded the element will not be returned, instead it will be
     * removed. It will be removed on retrieval, or removed actively if the
     * memory shrinker is turned on.
     *
     * @param idle
     *            The new idleTime value
     */
    public void setIdleTime( long idle );

    /**
     * Size in bytes. This is not used except in the admin pages. It will be -1
     * by default.
     *
     * @param size
     *            The new size value
     */
    public void setSize( int size );

    /**
     * Gets the size attribute of the IAttributes object
     *
     * @return The size value
     */
    public int getSize();

    /**
     * Gets the createTime attribute of the IAttributes object.
     * <p>
     * This shoudd be the current time in milliseconds returned by the sysutem
     * call when the element is put in the cache.
     * <p>
     * Putting an item in the cache overrides any existing items.
     *
     * @return The createTime value
     */
    public long getCreateTime();

    /**
     * Gets the LastAccess attribute of the IAttributes object.
     *
     * @return The LastAccess value.
     */
    public long getLastAccessTime();

    /**
     * Sets the LastAccessTime as now of the IElementAttributes object
     */
    public void setLastAccessTimeNow();

    /**
     * Gets the version attribute of the IAttributes object
     *
     * @return The version value
     */
    public long getVersion();

    /**
     * Gets the idleTime attribute of the IAttributes object
     *
     * @return The idleTime value
     */
    public long getIdleTime();

    /**
     * Gets the time left to live of the IAttributes object.
     * <p>
     * This is the (max life + create time) - current time.
     *
     * @return The TimeToLiveSeconds value
     */
    public long getTimeToLiveSeconds();

    /**
     * Returns a copy of the object.
     *
     * @return IElementAttributes
     */
    public IElementAttributes copy();

    /**
     * Can this item be spooled to disk
     * <p>
     * By default this is true.
     *
     * @return The spoolable value
     */
    public boolean getIsSpool();

    /**
     * Sets the isSpool attribute of the IElementAttributes object
     * <p>
     * By default this is true.
     *
     * @param val
     *            The new isSpool value
     */
    public void setIsSpool( boolean val );

    /**
     * Is this item laterally distributable. Can it be sent to auxiliaries of
     * type lateral.
     * <p>
     * By default this is true.
     *
     * @return The isLateral value
     */
    public boolean getIsLateral();

    /**
     * Sets the isLateral attribute of the IElementAttributes object
     * <p>
     * By default this is true.
     *
     * @param val
     *            The new isLateral value
     */
    public void setIsLateral( boolean val );

    /**
     * Can this item be sent to the remote cache.
     * <p>
     * By default this is true.
     *
     * @return The isRemote value
     */
    public boolean getIsRemote();

    /**
     * Sets the isRemote attribute of the IElementAttributes object.
     * <p>
     * By default this is true.
     *
     * @param val
     *            The new isRemote value
     */
    public void setIsRemote( boolean val );

    /**
     * This turns off expiration if it is true.
     *
     * @return The IsEternal value
     */
    public boolean getIsEternal();

    /**
     * Sets the isEternal attribute of the IElementAttributes object
     *
     * @param val
     *            The new isEternal value
     */
    public void setIsEternal( boolean val );

    /**
     * Adds a ElementEventHandler. Handler's can be registered for multiple
     * events. A registered handler will be called at every recognized event.
     *
     * @param eventHandler
     *            The feature to be added to the ElementEventHandler
     */
    public void addElementEventHandler( IElementEventHandler eventHandler );

    /**
     * Gets the elementEventHandlers.
     * <p>
     * Event handlers are transient. The only events defined are in memory
     * events. All handlers are lost if the item goes to disk.
     *
     * @return The elementEventHandlers value, null if there are none
     */
    public ArrayList getElementEventHandlers();

    /**
     * Sets the eventHandlers of the IElementAttributes object
     *
     * @param eventHandlers
     *            value
     */
    public void addElementEventHandlers( ArrayList eventHandlers );

}
