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

import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 * All auxiliary caches must have a factory that the cache configurator can use
 * to create instances.
 *
 */
public interface AuxiliaryCacheFactory
{

    /**
     *
     * @param attr
     *
     * @param cacheMgr
     *            This allows auxiliaries to reference the manager without
     *            assuming that it is a singleton. This will allow JCS to be a
     *            nonsingleton. Also, it makes it easier to test.
     *
     * @return AuxiliaryCache
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes attr, ICompositeCacheManager cacheMgr );

    /**
     * Sets the name attribute of the AuxiliaryCacheFactory object
     *
     * @param s
     *            The new name value
     */
    public void setName( String s );

    /**
     * Gets the name attribute of the AuxiliaryCacheFactory object
     *
     * @return The name value
     */
    public String getName();

}
