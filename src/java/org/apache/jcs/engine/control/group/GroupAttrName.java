package org.apache.jcs.engine.control.group;

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
        GroupAttrName to = ( GroupAttrName ) obj;
        return groupId.equals( to.groupId ) && attrName.equals( to.attrName );
    }


    /**
     * @return A hash code based on the hash code of {@ #groupid} and
     * {@link #attrName}.
     */
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
