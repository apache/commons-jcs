package org.apache.jcs.engine.behavior;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 */
public interface ICacheElement extends Serializable
{
    //, Cloneable

    /**
     * Gets the cacheName attribute of the ICacheElement object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    /**
     * Gets the key attribute of the ICacheElement object
     *
     * @return The key value
     */
    public Serializable getKey();


    /**
     * Gets the val attribute of the ICacheElement object
     *
     * @return The val value
     */
    public Serializable getVal();


    /**
     * Gets the attributes attribute of the ICacheElement object
     *
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes();


    /**
     * Sets the attributes attribute of the ICacheElement object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr );
}
