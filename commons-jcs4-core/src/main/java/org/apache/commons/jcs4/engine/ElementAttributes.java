package org.apache.commons.jcs4.engine;

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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.apache.commons.jcs4.engine.control.event.behavior.IElementEventHandler;

/**
 * This it the element attribute descriptor class. Each element in the cache has an ElementAttributes
 * object associated with it. An ElementAttributes object can be associated with an element in 3
 * ways:
 * <ol>
 * <li>When the item is put into the cache, you can associate an element attributes object.</li>
 * <li>If no attributes object is specified when the element is put into the cache, then the default
 * attributes for the region will be used.</li>
 * <li>The element attributes can be reset. This effectively results in a retrieval followed by a
 * put. Hence, this is the same as 1.</li>
 * </ol>
 */
public record ElementAttributes(
        /** Can this item be flushed to disk */
        boolean isSpool,

        /** Is this item laterally distributable */
        boolean isLateral,

        /** Can this item be sent to the remote cache */
        boolean isRemote,

        /**
         * You can turn off expiration by setting this to true. This causes the cache to bypass both max
         * life and idle time expiration.
         */
        boolean isEternal,

        /** Max life seconds */
        long maxLife,

        /**
         * The maximum time an entry can be idle. Setting this to -1 causes the idle time check to be
         * ignored.
         */
        long maxIdleTime,

        /** The creation time. This is used to enforce the max life. */
        long createTime,

        /** The last access time. This is used to enforce the max idle time. */
        AtomicLong atomicLastAccessTime,

        /** The time factor to convert durations to milliseconds */
        long timeFactorForMilliseconds,

        /**
         * The list of Event handlers to use. This is transient, since the event handlers cannot usually
         * be serialized. This means that you cannot attach a post serialization event to an item.
         * <p>
         * TODO we need to check that when an item is passed to a non-local cache that if the local
         * cache had a copy with event handlers, that those handlers are used.
         */
        ArrayList<IElementEventHandler> elementEventHandlers
) implements IElementAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = 7814990748035017441L;

    /** Default */
    private static final boolean DEFAULT_IS_SPOOL = true;
    /** Default */
    private static final boolean DEFAULT_IS_LATERAL = true;
    /** Default */
    private static final boolean DEFAULT_IS_REMOTE = true;
    /** Default */
    private static final boolean DEFAULT_IS_ETERNAL = true;
    /** Default */
    private static final long DEFAULT_MAX_LIFE = -1;
    /** Default */
    private static final long DEFAULT_MAX_IDLE_TIME = -1;
    /** Default */
    private static final long DEFAULT_TIME_FACTOR = 1000;

    /** Record with all defaults set */
    private static final ElementAttributes DEFAULT = new ElementAttributes(
            DEFAULT_IS_SPOOL,
            DEFAULT_IS_LATERAL,
            DEFAULT_IS_REMOTE,
            DEFAULT_IS_ETERNAL,
            DEFAULT_MAX_LIFE,
            DEFAULT_MAX_IDLE_TIME,
            0,
            new AtomicLong(),
            DEFAULT_TIME_FACTOR,
            new ArrayList<>());

    /**
     * @return an object containing the default settings
     */
    public static ElementAttributes defaults()
    {
        return DEFAULT;
    }

    /**
     * Constructor for the ElementAttributes object
     */
    public ElementAttributes()
    {
        this(defaults());
        this.atomicLastAccessTime.set(createTime());
    }

    /**
     * Copy constructor for the ElementAttributes object
     */
    public ElementAttributes(IElementAttributes from)
    {
        this(from.isSpool(),
             from.isLateral(),
             from.isRemote(),
             from.isEternal(),
             from.maxLife(),
             from.maxIdleTime(),
             System.currentTimeMillis(),
             new AtomicLong(from.lastAccessTime()),
             from.timeFactorForMilliseconds(),
             new ArrayList<>(from.elementEventHandlers()));
    }

    /**
     * Constructor for the ElementAttributes object
     */
    public ElementAttributes(
            boolean isSpool,
            boolean isLateral,
            boolean isRemote,
            boolean isEternal,
            long maxLife,
            long maxIdleTime,
            long timeFactorForMilliseconds
          )
    {
        this(isSpool, isLateral, isRemote, isEternal, maxLife, maxIdleTime,
                System.currentTimeMillis(), new AtomicLong(), timeFactorForMilliseconds,
                new ArrayList<>());

        this.atomicLastAccessTime.set(createTime());
    }

    /**
     * Adds a ElementEventHandler. Handler's can be registered for multiple events. A registered
     * handler will be called at every recognized event.
     * <p>
     * The alternative would be to register handlers for each event. Or maybe The handler interface
     * should have a method to return whether it cares about certain events.
     *
     * @param eventHandler The ElementEventHandler to be added to the list.
     */
    @Override
    public void addElementEventHandler( final IElementEventHandler eventHandler )
    {
        this.elementEventHandlers.add( eventHandler );
    }

    /**
     * Gets the LastAccess attribute of the IAttributes object.
     *
     * @return The LastAccess value.
     */
    @Override
    public long lastAccessTime()
    {
        return atomicLastAccessTime().get();
    }

    /**
     * Sets the LastAccessTime as now of the IElementAttributes object
     */
    @Override
    public void setLastAccessTimeNow()
    {
        this.atomicLastAccessTime.set(System.currentTimeMillis());
    }

    /**
     * Gets the time left to live of the IElementAttributes object.
     * <p>
     * This is the (max life + create time) - current time.
     * @return The TimeToLiveSeconds value
     */
    private long getTimeToLiveSeconds()
    {
        final long now = System.currentTimeMillis();
        return ( createTime() + maxLife() * timeFactorForMilliseconds() - now ) / 1000;
    }

    /**
     * For logging and debugging the element IElementAttributes.
     *
     * @return String info about the values.
     */
    @Override
    public String toString()
    {
        final StringBuilder dump = new StringBuilder();

        dump.append( "[ isLateral = " ).append( isLateral() );
        dump.append( ", isSpool = " ).append( isSpool() );
        dump.append( ", isRemote = " ).append( isRemote() );
        dump.append( ", isEternal = " ).append( isEternal() );
        dump.append( ", MaxLifeSeconds = " ).append( maxLife() );
        dump.append( ", MaxIdleTime = " ).append( maxIdleTime() );
        dump.append( ", CreateTime = " ).append( createTime() );
        dump.append( ", LastAccessTime = " ).append( lastAccessTime() );
        dump.append( ", getTimeToLiveSeconds() = " ).append(getTimeToLiveSeconds());
        dump.append( " ]" );

        return dump.toString();
    }
}
