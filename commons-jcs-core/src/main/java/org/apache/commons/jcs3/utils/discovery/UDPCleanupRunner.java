package org.apache.commons.jcs3.utils.discovery;

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
 * This class periodically check the lastHeardFrom time on the services.
 * <p>
 * If they exceed the configurable limit, it removes them from the set.
 * <p>
 * @author Aaron Smuts
 * @deprecated Functionality moved to UDPDiscoveryService
 */
@Deprecated
public class UDPCleanupRunner
    implements Runnable
{
    /** UDP discovery service */
    private final UDPDiscoveryService discoveryService;

    /**
     * @param service UDPDiscoveryService
     */
    public UDPCleanupRunner( final UDPDiscoveryService service )
    {
        this.discoveryService = service;
    }

    /**
     * This goes through the list of services and removes those that we haven't heard from in longer
     * than the max idle time.
     * <p>
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        discoveryService.cleanup();
    }
}
