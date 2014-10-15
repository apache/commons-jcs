package org.apache.commons.jcs.yajcache.beans;

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

import org.apache.commons.jcs.yajcache.core.ICache;
import org.apache.commons.jcs.yajcache.lang.annotation.*;
/**
 *
 * @author Hanson Char
 */
@CopyRightApache
public class CacheClearEvent<V> extends CacheChangeEvent<V> {
    public CacheClearEvent(@NonNullable ICache<V> cache)
    {
        super(cache);
    }
    @Override
    public boolean dispatch(@NonNullable ICacheChangeHandler<V> handler) {
        return handler.handleClear(super.getCache().getName());
    }
}
