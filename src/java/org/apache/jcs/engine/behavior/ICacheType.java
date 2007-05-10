package org.apache.jcs.engine.behavior;

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

/**
 * Interface implemented by a specific cache.
 *
 */
public interface ICacheType
    extends Serializable
{
    /** Composite/ memory cache type, cetral hub. */
    public final static int CACHE_HUB = 1;

    /** Disk cache type. */
    public final static int DISK_CACHE = 2;

    /** Lateral cache type. */
    public final static int LATERAL_CACHE = 3;

    /** Remote cache type. */
    public final static int REMOTE_CACHE = 4;

    /**
     * Returns the cache type.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType();

}
