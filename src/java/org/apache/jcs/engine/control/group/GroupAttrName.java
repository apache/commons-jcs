package org.apache.jcs.engine.control.group;

import java.io.Serializable;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class GroupAttrName implements Serializable
{
    //final GroupId groupId;
    /** Description of the Field */
    public final String groupId;
    final Object attrName;


    /**
     * Constructor for the GroupAttrName object
     *
     * @param groupId
     * @param attrName
     */
    public GroupAttrName( String groupId, Object attrName )
    {
        //this.groupId = new GroupId(groupId);
        this.groupId = groupId;
        this.attrName = attrName;

        if ( groupId == null || attrName == null )
        {
            throw new IllegalArgumentException( "groupId " + groupId + " and attrName " + attrName + ", must not be null." );
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
        return "[groupId=" + groupId + ", attrName=" + attrName + "]";
    }

}
