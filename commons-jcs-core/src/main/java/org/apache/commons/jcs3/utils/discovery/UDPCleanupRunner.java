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

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

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
    /** log instance */
    private static final Log log = LogManager.getLog( UDPCleanupRunner.class );

    /** UDP discovery service */
    private final UDPDiscoveryService discoveryService;

    /** default for max idle time, in seconds */
    private static final long DEFAULT_MAX_IDLE_TIME_SECONDS = 180;

    /** The configured max idle time, in seconds */
    private final long maxIdleTimeSeconds = DEFAULT_MAX_IDLE_TIME_SECONDS;

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
        final long now = System.currentTimeMillis();

        // the listeners need to be notified.
        discoveryService.getDiscoveredServices().stream()
            .filter(service -> {
                if (now - service.getLastHearFromTime() > maxIdleTimeSeconds * 1000)
                {
                    log.info( "Removing service, since we haven't heard from it in "
                            + "{0} seconds. service = {1}", maxIdleTimeSeconds, service );
                    return true;
                }

                return false;
            })
            // remove the bad ones
            // call this so the listeners get notified
            .forEach(service -> discoveryService.removeDiscoveredService(service));
    }
}
