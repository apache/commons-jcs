package org.apache.jcs.engine;


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


import java.rmi.dgc.VMID;

/**
 * A shared static variable holder for the lateral cache
 *
 */
public class CacheInfo
{

    // shouldn't be instantiated
    /** Constructor for the CacheInfo object */
    private CacheInfo() { }


    /**
     * Shouldn't be used till after reconneting, after setting = thread safe
     * Used to identify a client, so we can run multiple clients off one host.
     * Need since there is no way to identify a client other than by host in
     * rmi. TODO: may have some trouble in failover mode if the cache keeps its
     * old id. We may need to reset this when moving into failover.
     */
    protected static VMID vmid = new VMID();
    /** Description of the Field */
    public static long listenerId = vmid.hashCode();

}
