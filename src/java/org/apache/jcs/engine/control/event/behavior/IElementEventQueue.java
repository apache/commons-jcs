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

import java.io.IOException;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;

/**
 * Interface for an element event queue. An event queue is used to propagate
 * ordered element events in one region.
 *
 */
public interface IElementEventQueue
{

    /**
     * Adds an ElementEvent to be handled
     *
     * @param hand
     *            The IElementEventHandler
     * @param event
     *            The IElementEventHandler IElementEvent event
     * @throws IOException
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException;

    /** Description of the Method */
    public void destroy();

    /**
     * Gets the alive attribute of the IElementEventQueue object
     *
     * @return The alive value
     */
    public boolean isAlive();

}
