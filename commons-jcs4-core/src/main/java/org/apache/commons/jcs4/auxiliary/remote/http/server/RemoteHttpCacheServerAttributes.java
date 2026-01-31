package org.apache.commons.jcs4.auxiliary.remote.http.server;

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

import org.apache.commons.jcs4.auxiliary.AbstractAuxiliaryCacheAttributes;

/**
 * Configuration for the RemoteHttpCacheServer. Most of these properties are used only by the
 * service.
 */
public class RemoteHttpCacheServerAttributes
    extends AbstractAuxiliaryCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = -3987239306108780496L;

    /** Can a cluster remote put to other remotes */
    private boolean localClusterConsistency = true;

    /** Can a cluster remote get from other remotes */
    private boolean allowClusterGet = true;

    /**
     * Should gets from non-cluster clients be allowed to get from other remote auxiliaries.
     *
     * @return The localClusterConsistency value
     */
    public boolean isAllowClusterGet()
    {
        return allowClusterGet;
    }

    /**
     * Should cluster updates be propagated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean isLocalClusterConsistency()
    {
        return localClusterConsistency;
    }

    /**
     * Should we try to get from other cluster servers if we don't find the items locally.
     *
     * @param r The new localClusterConsistency value
     */
    public void setAllowClusterGet( final boolean r )
    {
        allowClusterGet = r;
    }

    /**
     * Should cluster updates be propagated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( final boolean r )
    {
        this.localClusterConsistency = r;
    }

    /**
     * @return String details
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\nRemoteHttpCacheServiceAttributes" );
        buf.append( "\n cacheName = [" + getCacheName() + "]" );
        buf.append( "\n allowClusterGet = [" + isAllowClusterGet() + "]" );
        buf.append( "\n localClusterConsistency = [" + isLocalClusterConsistency() + "]" );
        buf.append( "\n eventQueueType = [" + getEventQueueType() + "]" );
        buf.append( "\n eventQueuePoolName = [" + getEventQueuePoolName() + "]" );
        return buf.toString();
    }
}
