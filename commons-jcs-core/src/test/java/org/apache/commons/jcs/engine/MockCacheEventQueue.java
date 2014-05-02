package org.apache.commons.jcs.engine;

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

import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheListener;

/** For testing the factory */
public class MockCacheEventQueue<K extends Serializable, V extends Serializable>
    extends CacheEventQueue<K, V>
{
    /** junk */
    public MockCacheEventQueue()
    {
        super( new MockRemoteCacheListener<K, V>(), 1, null, 1, 1 );
    }
}