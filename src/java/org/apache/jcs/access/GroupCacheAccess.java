package org.apache.jcs.access;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @author asmuts
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @created January 15, 2002
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
     * Gets the groupAccess attribute of the GroupCacheAccess class
     *
     * @return The groupAccess value
     */
    public static GroupCacheAccess getGroupAccess( String region )
        throws CacheException
    {
        if ( cacheMgr == null )
        {
            synchronized ( GroupCacheAccess.class )
            {
                if ( cacheMgr == null )
                {
                    cacheMgr = CompositeCacheManager.getInstance();
                }
            }
        }
        return new GroupCacheAccess( ( CompositeCache ) cacheMgr.getCache( region ) );
    }

    /**
     * Gets the groupAccess attribute of the GroupCacheAccess class
     *
     * @return The groupAccess value
     */
    public static GroupCacheAccess getGroupAccess( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        if ( cacheMgr == null )
        {
            synchronized ( GroupCacheAccess.class )
            {
                if ( cacheMgr == null )
                {
                    cacheMgr = CompositeCacheManager.getInstance();
                }
            }
        }

        return new GroupCacheAccess( ( CompositeCache ) cacheMgr.getCache( region, icca ) );
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
            = cacheControl.get( getGroupAttrName( group, name ) );
        return ( element != null ) ? element.getVal() : null;
    }

    private GroupAttrName getGroupAttrName(String group, Object name)
    {
        GroupId gid = new GroupId(cacheControl.getCacheName(), group);
        return new GroupAttrName(gid, name);
    }

    /**
     * Allows the user to put an object into a group within a particular cache
     * region. This method sets the object's attributes to the default for the
     * region.
     *
     * @param key The key name.
     * @param group The group name.
     * @param value The object to cache
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
     * @param key The key name.
     * @param group The group name.
     * @param value The object to cache
     * @param attr The objects attributes.
     */
    public void putInGroup( Object name, String groupName, Object value, 
                            IElementAttributes attr )
        throws CacheException
    {
        // unbind object first if any.
        remove( name, groupName);

        if (attr == null) 
        {
            put( getGroupAttrName(groupName, name), value );            
        }
        else 
        {
            put( getGroupAttrName(groupName, name), value, attr );            
        }
    }

    /** Description of the Method */
    public void remove( Object name, String group )
    {
        GroupAttrName key = getGroupAttrName( group, name );
        cacheControl.remove(key);            
    }

    /**
     * Gets the set of keys of objects currently in the group
     */
    public Set getGroupKeys(String group)
    {
        return cacheControl.getGroupKeys(group);
    }

    /** Invalidates a group */
    public void invalidateGroup( String group )
    {
        cacheControl.remove(new GroupId( cacheControl.getCacheName(), group ));
    }
}
