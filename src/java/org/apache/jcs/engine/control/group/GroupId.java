package org.apache.jcs.engine.control.group;

import java.io.Serializable;

/**
 * Used to avoid name conflict when group cache items are mixed with non-group
 * cache items in the same cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class GroupId implements Serializable
{

    /** Description of the Field */
    public final String key;


    /**
     * Constructor for the GroupId object
     *
     * @param cacheName
     * @param key
     */
    public GroupId( String cacheName, String key )
    {
        this.key = cacheName + key;

        if ( key == null )
        {
            throw new IllegalArgumentException( "key must not be null." );
        }
    }


    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof GroupId ) )
        {
            return false;
        }
        GroupId to = ( GroupId ) obj;
        return key.equals( to.key );
    }


    /** Description of the Method */
    public int hashCode()
    {
        return key.hashCode();
    }


    /** Description of the Method */
    public String toString()
    {
        return "[grouId=" + key + "]";
    }
}
