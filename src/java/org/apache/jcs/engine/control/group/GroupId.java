package org.apache.jcs.engine.control.group;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;

/**
 * Used to avoid name conflict when group cache items are mixed with non-group
 * cache items in the same cache.
 *  
 */
public class GroupId
    implements Serializable
{
    /** Description of the Field */
    public final String groupName;

    public final String cacheName;

    private String toString;

    /**
     * Constructor for the GroupId object
     * 
     * @param cacheName
     * @param groupName
     */
    public GroupId( String cacheName, String groupName )
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

    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof GroupId ) )
        {
            return false;
        }
        GroupId g = (GroupId) obj;
        return cacheName.equals( g.cacheName ) && groupName.equals( g.groupName );
    }

    /** Description of the Method */
    public int hashCode()
    {
        return cacheName.hashCode() + groupName.hashCode();
    }

    /** Description of the Method */
    public String toString()
    {
        if ( toString == null )
        {
            toString = "[groupId=" + cacheName + ", " + groupName + ']';
        }

        return toString;
    }
}
