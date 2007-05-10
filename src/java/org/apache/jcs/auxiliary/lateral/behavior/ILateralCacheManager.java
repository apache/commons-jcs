package org.apache.jcs.auxiliary.lateral.behavior;

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
import java.util.Map;

import org.apache.jcs.auxiliary.AuxiliaryCacheManager;

/**
 * This helps ensure some common behvior among LateraLCacheManagers for things
 * such as montiors.
 * <p>
 * @author Aaron Smuts
 */
public interface ILateralCacheManager
    extends AuxiliaryCacheManager
{
    /**
     * This is a temporary solution that allos the monitor to get the instances
     * of a manager.
     * @return
     */
    public abstract Map getInstances();

    /**
     * This is a temporary solution that allos the monitor to get caches from an
     * instance of a manager.
     * @return
     */
    public abstract Map getCaches();

    /**
     * The restore calls thsi on the manger if a cache if found to be in error.
     * @return Object is the service if it can be fixed.
     * @throws IOException
     *             if the service cannot be fixed.
     */
    public abstract Object fixService()
        throws IOException;

    /**
     * Sets the corected service. The restore process will call this if it gets
     * a good service back from fixService.
     * @param lateralService
     * @param lateralWatch
     */
    public void fixCaches( ILateralCacheService lateralService, ILateralCacheObserver lateralWatch );

}
