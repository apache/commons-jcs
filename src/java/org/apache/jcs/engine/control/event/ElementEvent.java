package org.apache.jcs.engine.control.event;

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

import java.util.EventObject;

import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;

/**
 * Element events will trigger the creation of Element Event objects. This is a
 * wrapper around the cache element that indicates the event triggered.
 *
 */
public class ElementEvent
    extends EventObject
    implements IElementEventConstants, IElementEvent
{

    private static final long serialVersionUID = -5364117411457467056L;

    private int elementEvent = ElementEvent.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND;

    /**
     * Constructor for the ElementEvent object
     *
     * @param source
     *            The Cache Element (should restrict?)
     * @param elementEvent
     *            The event id defined in the constants class.
     */
    public ElementEvent( Object source, int elementEvent )
    {
        super( source );
        this.elementEvent = elementEvent;
    }

    /**
     * Gets the elementEvent attribute of the ElementEvent object
     *
     * @return The elementEvent value. The List of values is defined in
     *         IElementEventConstants.
     */
    public int getElementEvent()
    {
        return elementEvent;
    }

}
