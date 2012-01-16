package org.apache.jcs.auxiliary.remote;

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

import java.io.Serializable;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.ZombieCacheServiceNonLocal;

/**
 * Zombie adapter for the remote cache service. It just balks if there is no queue configured.
 * <p>
 * If a queue is configured, then events will be added to the queue. The idea is that when proper
 * operation is restored, the remote cache will walk the queue. The queue must be bounded so it does
 * not eat memory.
 * <p>
 * Much of this was reusable, so I moved it to engine.
 */
public class ZombieRemoteCacheService<K extends Serializable, V extends Serializable>
    extends ZombieCacheServiceNonLocal<K, V>
    implements IRemoteCacheService<K, V>
{
    // backwards compatibility

    /**
     * We shouldn't be using this. It's only called on certain startup errors.
     */
    public ZombieRemoteCacheService()
    {
        super();
    }

    /**
     * Sets the maximum number of items that will be allowed on the queue.
     * <p>
     * @param maxQueueSize
     */
    public ZombieRemoteCacheService( int maxQueueSize )
    {
        super( maxQueueSize );
    }
}
