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

import java.io.Serializable;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.engine.ZombieCacheServiceNonLocal;

/**
 * The ZombieLateralCacheService is used as a facade when the lateral is not available. It balks
 * when the lateral is in error. When lateral service is restored, this is replaced by a live
 * facade.
 * <p>
 * Extends a queing non-local service.
 */
public class ZombieLateralCacheService<K extends Serializable, V extends Serializable>
    extends ZombieCacheServiceNonLocal<K, V>
    implements ILateralCacheService<K, V>
{
    // backwards compatibility

    /**
     * Sets the maximum number of items that will be allowed on the queue.
     * <p>
     * @param maxQueueSize
     */
    public ZombieLateralCacheService( int maxQueueSize )
    {
        super( maxQueueSize );
    }
}
