package org.apache.jcs.access;


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


import java.util.Set;

import org.apache.jcs.access.behavior.IGroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;

/**
 * Access for groups.
 *
 */
public class GroupCacheAccess extends CacheAccess implements IGroupCacheAccess
{
    private static CompositeCacheManager cacheMgr;

    /**
     * Constructor for the GroupCacheAccess object
     *
     * @param cacheControl
     */
    public GroupCacheAccess( CompositeCache cacheControl )
    {
        super( cacheControl );
    }

    /**
     * Gets the groupAccess attribute of the GroupCacheAccess class.
     * 
     * @param region
     * @return The groupAccess value
     * @throws CacheException
     */
    public static GroupCacheAccess getGroupAccess( String region )
        throws CacheException
    {
        synchronized ( GroupCacheAccess.class )
        {
            if ( cacheMgr == null )
            {
                cacheMgr = CompositeCacheManager.getInstance();
            }
        }
        return new GroupCacheAccess( cacheMgr.getCache( region ) );
    }

    /**
     * Gets the groupAccess attribute of the GroupCacheAccess class.
     * 
     * @param region
     * @param icca
     * @return The groupAccess value
     * @throws CacheException
     */
    public static GroupCacheAccess getGroupAccess( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        synchronized ( GroupCacheAccess.class )
        {
            if ( cacheMgr == null )
            {
                cacheMgr = CompositeCacheManager.getInstance();
            }
        }

        return new GroupCacheAccess( cacheMgr.getCache( region, icca ) );
    }

    /**
     * Gets an item out of the cache that is in a specified group.
     *
     * @param name The key name.
     * @param group The group name.
     * @return The cached value, null if not found.
     */
    public Object getFromGroup( Object name, String group )
    {
        ICacheElement element
            = this.cacheControl.get( getGroupAttrName( group, name ) );
        return ( element != null ) ? element.getVal() : null;
    }

    private GroupAttrName getGroupAttrName(String group, Object name)
    {
        GroupId gid = new GroupId(this.cacheControl.getCacheName(), group);
        return new GroupAttrName(gid, name);
    }

    /**
     * Allows the user to put an object into a group within a particular cache
     * region. This method sets the object's attributes to the default for the
     * region.
     *
     * @param name The key name.
     * @param groupName The group name.
     * @param value The object to cache
     * @throws CacheException
     */
    public void putInGroup( Object name, String groupName, Object value )
        throws CacheException
    {
        putInGroup(name, groupName, value, null);
    }

    /**
     * Allows the user to put an object into a group within a particular cache
     * region. This method allows the object's attributes to be individually
     * specified.
     *
     * @param name The key name.
     * @param groupName The group name.
     * @param value The object to cache
     * @param attr The objects attributes.
     * @throws CacheException
     */
    public void putInGroup( Object name, String groupName, Object value,
                            IElementAttributes attr )
        throws CacheException
    {
        // unbind object first if any.
        remove( name, groupName);

        if (attr == null)
        {
            put( getGroupAttrName( groupName, name ), value );
        }
        else
        {
            put( getGroupAttrName( groupName, name ), value, attr );
        }
    }

    /**
     * @param name
     * @param group
     * 
     */
    public void remove( Object name, String group )
    {
        GroupAttrName key = getGroupAttrName( group, name );
        this.cacheControl.remove( key );
    }

    /**
     * Gets the set of keys of objects currently in the group.
     * 
     * @param group
     * @return A Set of keys.
     */
    public Set getGroupKeys(String group)
    {
        return this.cacheControl.getGroupKeys( group );
    }

    /** 
     * Invalidates a group: remove all the group members 
     * 
     * @param group The name of the group to invalidate
     */
    public void invalidateGroup( String group )
    {
        this.cacheControl.remove( new GroupId( this.cacheControl.getCacheName(), group ) );
    }
}
