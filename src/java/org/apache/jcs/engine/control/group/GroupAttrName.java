package org.apache.jcs.engine.control.group;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
 * Description of the Class
 *
 */
public class GroupAttrName
    implements Serializable
{
    private static final long serialVersionUID = 1586079686300744198L;

    /** Description of the Field */
    public final GroupId groupId;

    /** the name of the attribute */
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
            throw new IllegalArgumentException( "groupId " + groupId + " and attrName " + attrName
                + ", must not be null." );
        }
    }

    /**
     * Tests object equality.
     *
     * @param obj
     *            The <code>GroupAttrName</code> instance to test.
     * @return Whether equal.
     */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof GroupAttrName ) )
        {
            return false;
        }
        GroupAttrName to = (GroupAttrName) obj;
        return groupId.equals( to.groupId ) && attrName.equals( to.attrName );
    }

    /**
     * @return A hash code based on the hash code of {@ #groupid}and
     *         {@link #attrName}.
     */
    public int hashCode()
    {
        return groupId.hashCode() ^ attrName.hashCode();
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if ( toString == null )
        {
            toString = "[GAN: groupId=" + groupId + ", attrName=" + attrName + "]";
        }

        return toString;
    }

}
