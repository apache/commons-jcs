package org.apache.jcs.engine.control.group;

import java.io.Serializable;

/**
 * Description of the Class
 *
 * @author asmuts
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @created January 15, 2002
 */
public class GroupAttrName implements Serializable
{
    //final GroupId groupId;
    /** Description of the Field */
    public final GroupId groupId;
    public final Object attrName;
    private String toString;

    /**
     * Constructor for the GroupAttrName object
     *
     * @param groupId
     * @param attrName
     */
    public GroupAttrName( GroupId groupId, Object attrName )
    {
        this.groupId = groupId;
        this.attrName = attrName;

        if ( groupId == null || attrName == null )
        {
            throw new IllegalArgumentException( "groupId " + groupId + 
                " and attrName " + attrName + ", must not be null." );
        }
    }


    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof GroupAttrName ) )
        {
            return false;
        }
        GroupAttrName to = ( GroupAttrName ) obj;
        return groupId.equals( to.groupId ) && attrName.equals( to.attrName );
    }


    /** Description of the Method */
    public int hashCode()
    {
        return groupId.hashCode() ^ attrName.hashCode();
    }


    /** Description of the Method */
    public String toString()
    {
        if (toString == null) 
        {
            toString = "[GAN: groupId=" + groupId + 
                ", attrName=" + attrName + "]";
        }
        
        return toString;
    }

}
