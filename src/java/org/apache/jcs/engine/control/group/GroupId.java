package org.apache.jcs.engine.control.group;

import java.io.Serializable;

/**
 * Used to avoid name conflict when group cache items are mixed with non-group
 * cache items in the same cache.
 *
 * @author asmuts
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @created January 15, 2002
 */
public class GroupId implements Serializable
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
            throw new IllegalArgumentException("cacheName must not be null.");
        }
        if ( groupName == null )
        {
            throw new IllegalArgumentException("groupName must not be null.");
        }
    }

    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( obj == null || !(obj instanceof GroupId) )
        {
            return false;
        }
        GroupId g = (GroupId)obj;
        return cacheName.equals(g.cacheName) && groupName.equals(g.groupName);
    }


    /** Description of the Method */
    public int hashCode()
    {
        return cacheName.hashCode() + groupName.hashCode();
    }

    /** Description of the Method */
    public String toString()
    {
        if (toString == null) 
        {
            toString = "[groupId=" + cacheName + ", " + groupName + ']';
        }
        
        return toString;
    }
}
