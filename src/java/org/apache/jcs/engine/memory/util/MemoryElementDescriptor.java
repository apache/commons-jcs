package org.apache.jcs.engine.memory.util;

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

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.struct.DoubleLinkedListNode;

/**
 * This wrapper is needed for double linked lists.
 */
public class MemoryElementDescriptor
    extends DoubleLinkedListNode
{

    private static final long serialVersionUID = -1905161209035522460L;

    /** The CacheElement wrapped by this descriptor */
    public ICacheElement ce;

    /**
     * Constructor for the MemoryElementDescriptor object
     * 
     * @param ce
     */
    public MemoryElementDescriptor( ICacheElement ce )
    {
        super( ce );
        this.ce = ce;
    }
}
