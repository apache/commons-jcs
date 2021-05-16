package org.apache.commons.jcs3.engine.control.group;

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
import java.util.Objects;

/**
 * Description of the Class
 */
public class GroupAttrName<T>
    implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = 1586079686300744198L;

    /** Description of the Field */
    public final GroupId groupId;

    /** the name of the attribute */
    public final T attrName;

    /**
     * Constructor for the GroupAttrName object
     * @param groupId
     * @param attrName
     */
    public GroupAttrName( final GroupId groupId, final T attrName )
    {
        this.groupId = groupId;
        this.attrName = attrName;

        if ( groupId == null )
        {
            throw new IllegalArgumentException( "groupId must not be null." );
        }
    }

    /**
     * Tests object equality.
     * @param obj The <code>GroupAttrName</code> instance to test.
     * @return Whether equal.
     */
    @Override
    public boolean equals( final Object obj )
    {
        if (!(obj instanceof GroupAttrName))
        {
            return false;
        }
        final GroupAttrName<?> to = (GroupAttrName<?>) obj;

        if (groupId.equals( to.groupId ))
        {
            return Objects.equals(attrName, to.attrName);
        }

        return false;
    }

    /**
     * @return A hash code based on the hash code of @ #groupid} and {@link #attrName}.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(groupId, attrName);
    }

    /**
     * @return the cached value.
     */
    @Override
    public String toString()
    {
        return String.format("GAN:%s:%s", groupId, Objects.toString(attrName, ""));
    }

}
