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
    private String hashString;
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
        this.hashString = groupId.toString() + attrName.toString();
    }


    /**
     * Tests object equality.
     *
     * @param obj The <code>GroupAttrName</code> instance to test.
     * @return Whether equal.
     */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof GroupAttrName ) )
        {
            return false;
        }
        return hashString.equals( ((GroupAttrName) obj).hashString );
    }


    /** Description of the Method */
    public int hashCode()
    {
        return hashString.hashCode();
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
