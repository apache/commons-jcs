package org.apache.commons.jcs3.engine.control.group;

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
 * Used to avoid name conflict when group cache items are mixed with non-group cache items in the
 * same cache.
 */
public record GroupId(String cacheName, String groupName)
    implements Serializable
{
    /**
     * Constructor for the GroupId object
     *
     * @param cacheName
     * @param groupName
     */
    public GroupId( final String cacheName, final String groupName )
    {
        this.cacheName = cacheName;
        this.groupName = groupName;

        if ( cacheName == null )
        {
            throw new IllegalArgumentException( "cacheName must not be null." );
        }
        if ( groupName == null )
        {
            throw new IllegalArgumentException( "groupName must not be null." );
        }
    }

    /**
     * Convert to string
     *
     * @return the string representation of this ID.
     */
    @Override
    public String toString()
    {
        return String.format("[groupId=%s, %s]", cacheName, groupName);
    }
}
