package org.apache.jcs.auxiliary.lateral;

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

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;


/**
 * Particular lateral caches should define their own factory.  It is
 * not necessary to extend this base factory, but it can be useful.
 * <p>
 * The old factory tried to handle all types of laterals.  It was
 * gettting cluttered by ad hoc if statements.  Since the javagroups
 * lateral was jdk1.4 dependent it had to be moved.  As such, the
 * old factory could no longer import it.  This motivated the change.
 * <p>
 * This abstraction layer should keep things cleaner.
 * <p>
 * @author Aaron Smuts
 */
public abstract class LateralCacheAbstractFactory
	implements AuxiliaryCacheFactory
{

    private String name;

    /* (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.jcs.auxiliary.AuxiliaryCacheAttributes, org.apache.jcs.engine.behavior.ICompositeCacheManager)
     */
    public abstract AuxiliaryCache createCache( AuxiliaryCacheAttributes attr, ICompositeCacheManager cacheMgr );

    /**
     * Makes sure a listener gets created. It will get monitored as soon as it
     * is used.
     * <p>
     * This should be called by create cache.
     * <p>
     * @param lac  ILateralCacheAttributes
     * @param cacheMgr
     */
    public abstract void createListener( ILateralCacheAttributes lac, ICompositeCacheManager cacheMgr );

    /**
     * Gets the name attribute of the LateralCacheFactory object
     * <p>
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the LateralCacheFactory object
     * <p>
     * @param name
     *            The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}
