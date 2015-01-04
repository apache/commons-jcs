package org.apache.commons.jcs.utils.discovery;

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

import org.apache.commons.jcs.utils.discovery.behavior.IDiscoveryListener;

import java.util.ArrayList;
import java.util.List;

/** Mock listener, for testing. */
public class MockDiscoveryListener
    implements IDiscoveryListener
{
    /** discovered services. */
    public List<DiscoveredService> discoveredServices = new ArrayList<DiscoveredService>();

    /**
     * Adds the entry to a list. I'm not using a set. I want to see if we get dupes.
     * <p>
     * @param service
     */
    @Override
    public void addDiscoveredService( DiscoveredService service )
    {
        discoveredServices.add( service );
    }

    /**
     * Removes it from the list.
     * <p>
     * @param service
     */
    @Override
    public void removeDiscoveredService( DiscoveredService service )
    {
        discoveredServices.remove( service );
    }

}
