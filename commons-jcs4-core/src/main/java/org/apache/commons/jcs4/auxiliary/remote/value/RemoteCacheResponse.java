package org.apache.commons.jcs4.auxiliary.remote.value;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
 * This is the response wrapper. The servlet wraps all different type of responses in one of these
 * objects.
 */
public record RemoteCacheResponse<T>(
        /**
         * The payload. Typically a key / ICacheElement&lt;K, V&gt; map. A normal get will return a map with one
         * record.
         */
        T payload,

        /** Was the event processed without error */
        boolean success,

        /** Simple error messaging */
        String errorMessage
) implements Serializable
{
    /** Don't change. */
    private static final long serialVersionUID = -8858447417390442568L;

    /**
     * Construct error message object
     * @param succcess
     * @param errorMessage
     */
    public RemoteCacheResponse(final boolean success, final String errorMessage)
    {
        this(null, success, errorMessage);
    }

    /**
     * Construct payload object
     * @param payload
     */
    public RemoteCacheResponse(final T payload)
    {
        this(payload, true, "OK");
    }

    /** @return string */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\nRemoteHttpCacheResponse" );
        buf.append( "\n success [" + success() + "]" );
        buf.append( "\n payload [" + payload() + "]" );
        buf.append( "\n errorMessage [" + errorMessage() + "]" );
        return buf.toString();
    }
}
