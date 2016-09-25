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

import junit.framework.TestCase;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteRequestType;

/** Unit tests for the dispatcher. */
public class RemoteHttpCacheDispatcherUnitTest
    extends TestCase
{
    /**
     * Verify that we don't get two ?'s
     */
    public void testAddParameters_withQueryString()
    {
        // SETUP
        RemoteHttpCacheAttributes remoteHttpCacheAttributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheDispatcher dispatcher = new RemoteHttpCacheDispatcher( remoteHttpCacheAttributes );

        RemoteCacheRequest<String, String> remoteCacheRequest = new RemoteCacheRequest<String, String>();
        remoteCacheRequest.setRequestType( RemoteRequestType.REMOVE_ALL );
        String cacheName = "myCache";
        remoteCacheRequest.setCacheName( cacheName );

        String baseUrl = "http://localhost?thishasaquestionmark";

        // DO WORK
        String result = dispatcher.addParameters( remoteCacheRequest, baseUrl );

        // VERIFY
        assertEquals( "Wrong url", baseUrl + "&CacheName=" + cacheName + "&Key=&RequestType=REMOVE_ALL", result  );
    }
}
