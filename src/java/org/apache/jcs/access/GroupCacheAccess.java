package org.apache.jcs.access;

/*
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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.behavior.IGroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupCacheHub;
import org.apache.jcs.engine.control.group.GroupId;

/**
 * Access for groups.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class GroupCacheAccess extends CacheAccess implements IGroupCacheAccess
{
    private final static Log log =
        LogFactory.getLog( GroupCacheAccess.class );

    private static boolean SET_ATTR_INVOCATION = true;
    private static boolean REMOVE_ATTR_INVOCATION = false;

    private static CacheHub cacheMgr;

    /**
     * Constructor for the GroupCacheAccess object
     *
     * @param cacheControl
     */
    protected GroupCacheAccess( Cache cacheControl )
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
                    cacheMgr = GroupCacheHub.getInstance();
                }
            }
        }
        return new GroupCacheAccess( ( Cache ) cacheMgr.getCache( region ) );
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
                    cacheMgr = GroupCacheHub.getInstance();
                }
            }
        }

        return new GroupCacheAccess( ( Cache ) cacheMgr.getCache( region, icca ) );
    }

    /**
     * Gets an item out of the cache that is in a specified group.
     *
     * @param name The key name.
     * @param group The group name.
     * @return The cahe value, null if not found.
     */
    public Object getFromGroup( Object name, String group )
    {
        return getAttribute( name, group );
    }

    /**
     * Gets the attribute attribute of the GroupCacheAccess object
     *
     * @return The attribute value
     */
    public Object getAttribute( Object name, String group )
    {
        ICacheElement element
            = cacheControl.get( new GroupAttrName( group, name ) );
        return ( element != null ) ? element.getVal() : null;
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
    public void putInGroup( Object key, String group, Object value )
        throws CacheException
    {
        setAttribute( key, group, value );
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
    public void putInGroup( Object key, String group, Object value, IElementAttributes attr )
        throws CacheException
    {
        setAttribute( key, group, value, attr );
    }

    /**
     * DefineGroup is used to create a new group object. Attributes may be set
     * on the group. If no attributes are specified, the attributes of the
     * region or group the new group is associated with are used. If group is
     * specified the new group will be associated with the group specified.
     *
     * @param name Name of the gorup.
     */
    public void defineGroup( String name )
        throws CacheException
    {
        defineGroup(name, null);
    }

    /**
     * Description of the Method
     *
     * @param name Name of the group
     * @param attr Default attributes for the group.
     */
    public void defineGroup( String name, IElementAttributes attr )
        throws CacheException
    {
        // update the attribute name set.
        GroupId groupId = new GroupId( cacheControl.getCacheName(), name );
        if ( get(groupId) != null )
        {
            throw new CacheException( "group " + name + " already exists " );
        }

        // TODO: revisit and verify that this works
        // not sure it will, need special id putting
        if (attr == null) 
        {
            put( groupId, new HashSet() );            
        }
        else 
        {
            put( groupId, new HashSet(), attr );
        }        
    }

    /**
     * Gets the groupAttributes attribute of the GroupCacheAccess object.
     * Slighly confusing since the other method conside an "attribute" to be an
     * element of the cache and not the parameters governing an element.
     *
     * @return The Element Attributes for the group
     */
    public IElementAttributes getGroupAttributes( String name )
        throws CacheException
    {
        IElementAttributes attr = null;
        try
        {
            attr = cacheControl.getElementAttributes( ( Serializable ) name );
        }
        catch ( IOException ioe )
        {
            throw new CacheException(
                "Failure getting element attributes due to ", ioe );
        }
        return attr;
    }

    /**
     * Gets the attributeNames attribute of the GroupCacheAccess object
     *
     * @return The attributeNames value
     */
    public Enumeration getAttributeNames( String group_name )
    {
        //Set s = getAttributeNameSet( name );
        //p( s.toString() );
        //return Collections.enumeration(s);
        return Collections.enumeration( getAttributeNameSet( group_name ) );
    }

    /**
     * Gets the attributeNameSet attribute of the GroupCacheAccess object
     *
     * @return The attributeNameSet value
     */
    public Set getAttributeNameSet( String groupName )
    {
        Object obj = get(new GroupId(cacheControl.getCacheName(), groupName));
        if ( obj == null || !( obj instanceof Set ) )
        {
            return new HashSet();
        }
        return (Set) obj;
    }

    /**
     * Sets the attribute attribute of the GroupCacheAccess object
     *
     * @param name The new attribute value
     * @param group The new attribute value
     * @param value The new attribute value
     */
    public void setAttribute( Object name, String groupName, Object value )
        throws CacheException
    {
        setAttribute(name, groupName, value, null);
    }

    /**
     * Sets the attribute attribute of the GroupCacheAccess object
     *
     * @param name The new attribute value
     * @param group The new attribute value
     * @param value The new attribute value
     * @param attr The new attribute value
     */
    public void setAttribute( Object name, String groupName, Object value, 
                              IElementAttributes attr )
        throws CacheException
    {
        Set group = (Set)
            get(new GroupId(cacheControl.getCacheName(), groupName));
        if (group == null) 
        {
            throw new CacheException(
                "Group must be defined prior to being used.");
        }

        // unbind object first if any.
        boolean isPreviousObj = removeAttribute( name, groupName, false);

        if (attr == null) 
        {
            put( new GroupAttrName(groupName, name), value );            
        }
        else 
        {
            put( new GroupAttrName(groupName, name), value, attr );            
        }

        if (!isPreviousObj) 
        {
            group.add(name);   
        }        
    }

    /** Description of the Method */
    public void removeAttribute( Object name, String group )
    {
        removeAttribute( name, group, true );
    }

    /** Description of the Method */
    private boolean removeAttribute( Object name, String groupName, 
                                     boolean removeFromGroup )
    {
        GroupAttrName key = new GroupAttrName( groupName, name );
        // Needs to retrieve the attribute so as to do object unbinding, 
        // if necessary.
        boolean isPreviousObj = cacheControl.get(key) != null;
        if (isPreviousObj) 
        {
            cacheControl.remove(key);            
        }
        if (removeFromGroup) 
        {
            Set group = getAttributeNameSet(groupName);
            group.remove(name);
        }
        return isPreviousObj;
    }

    /**
     * Removes an element from the group
     *
     * @deprecated
     */
    public void destroy( Object name, String group )
    {
        removeAttribute( name, group );
    }

    /** Description of the Method */
    public void remove( Object name, String group )
    {
        removeAttribute( name, group );
    }

    /** Invalidates a group */
    public void invalidateGroup( String group )
    {
        // Removes all the attributes and attribute names from the Cache.
        // In doing so, need to unbind any object associated with the session.
        // need a static list not dependent on the current state of the source
        // remove each item, may want to try using partial delete here
        // move to gorupcache?
        Set set = getAttributeNameSet( group );
        Object[] ar = set.toArray();
        int arS = ar.length;
        for ( int i = 0; i < arS; i++ )
        {
            removeAttribute( ar[i], group, false );
        }

        // get into concurrent modificaiton problems here.
        // could make the removal of the ID invalidate the list?
        cacheControl.remove(new GroupId( cacheControl.getCacheName(), group ));
    }

    /**
     * Gets the valueNames attribute of the GroupCacheAccess object
     *
     * @return The valueNames value
     */
    public String[] getValueNames( String group )
    {
        return ( String[] ) getAttributeNameSet( group ).toArray( new String[ 0 ] );
    }

}
