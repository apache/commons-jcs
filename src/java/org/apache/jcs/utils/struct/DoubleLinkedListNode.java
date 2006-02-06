package org.apache.jcs.utils.struct;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;

/**
 * This serves as a placeholder in a double linked list. You can extend this to
 * add functionality. This allows you to remove in constant time from a linked
 * list.
 */
public class DoubleLinkedListNode
    implements Serializable
{
    private static final long serialVersionUID = -1114934407695836097L;

    private Object payload;

    /** Double Linked list references */
    public DoubleLinkedListNode prev;

    /** Double Linked list references */
    public DoubleLinkedListNode next;

    /**
     * 
     * @param payloadP
     */
    public DoubleLinkedListNode( Object payloadP )
    {
        payload = payloadP;
    }

    /**
     * @return Object
     */
    public Object getPayload()
    {
        return payload;
    }

}
