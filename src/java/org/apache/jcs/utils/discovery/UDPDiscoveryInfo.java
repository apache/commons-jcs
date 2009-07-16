package org.apache.jcs.utils.discovery;

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

import java.rmi.dgc.VMID;

/**
 * Provides info for the udp discovery service.
 * <p>
 * @author Aaron Smuts
 */
public class UDPDiscoveryInfo
{
    /**
     * jvm unique identifier.
     */
    protected static VMID vmid = new VMID();

    /**
     * Identifies the listener, so we don't add ourselves to the list of known services.
     */
    public static long listenerId = vmid.hashCode();
}
