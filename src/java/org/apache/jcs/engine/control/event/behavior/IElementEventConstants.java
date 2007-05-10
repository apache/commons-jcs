package org.apache.jcs.engine.control.event.behavior;

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

/**
 * This describes the events that an item can encounter.
 *
 */
public interface IElementEventConstants
{

    /**
     * Background expiration
     */
    public final static int ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND = 0;

    /**
     * Expiration discovered on request
     */
    public final static int ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST = 1;

    /**
     * Background expiration
     */
    public final static int ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND = 2;

    /**
     * Expiration discovered on request
     */
    public final static int ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST = 3;

    /**
     * Moving from memory to disk (what if no disk?)
     */
    public final static int ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE = 4;

    /**
     * Moving from memory to disk (what if no disk?)
     */
    public final static int ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE = 5;

    /**
     * Moving from memory to disk, but item is not spoolable
     */
    public final static int ELEMENT_EVENT_SPOOLED_NOT_ALLOWED = 6;

    /**
     * Removed activley by a remove command. (Could distinguish between local
     * and remote)
     */
    //public final static int ELEMENT_EVENT_REMOVED = 7;
    /**
     * Element was requested from cache. Not sure we ever want to implement
     * this.
     */
    //public final static int ELEMENT_EVENT_GET = 8;
}
