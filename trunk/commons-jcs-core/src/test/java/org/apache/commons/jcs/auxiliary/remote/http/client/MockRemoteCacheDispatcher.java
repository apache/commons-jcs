package org.apache.commons.jcs.auxiliary.remote.http.client;

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

import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheResponse;

import java.io.IOException;

/** For testing the service. */
public class MockRemoteCacheDispatcher
    implements IRemoteCacheDispatcher
{
    /** The last request passes to dispatch */
    public RemoteCacheRequest<?, ?> lastRemoteCacheRequest;

    /** The response setup */
    public RemoteCacheResponse<?> setupRemoteCacheResponse;

    /** Records the last and returns setupRemoteCacheResponse.
     * <p>
     * @param remoteCacheRequest
     * @return RemoteCacheResponse
     * @throws IOException
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, V, T>
        RemoteCacheResponse<T> dispatchRequest( RemoteCacheRequest<K, V> remoteCacheRequest )
        throws IOException
    {
        this.lastRemoteCacheRequest = remoteCacheRequest;
        return (RemoteCacheResponse<T>)setupRemoteCacheResponse;
    }
}
