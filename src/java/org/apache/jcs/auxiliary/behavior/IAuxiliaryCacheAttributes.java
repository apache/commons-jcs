package org.apache.jcs.auxiliary.behavior;


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

/**
 * This is a nominal interface that auxilliary cache attributes should
 * implement. This allows the auxiliary mangers to share a common interface.
 *
 */
public interface IAuxiliaryCacheAttributes extends Cloneable, Serializable
{

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s );


    /**
     * Gets the cacheName attribute of the IAuxiliaryCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    /**
     * Name know by by configurator
     *
     * @param s The new name value
     */
    public void setName( String s );


    /**
     * Gets the name attribute of the IAuxiliaryCacheAttributes object
     *
     * @return The name value
     */
    public String getName();


    /** Description of the Method */
    public IAuxiliaryCacheAttributes copy();

}
