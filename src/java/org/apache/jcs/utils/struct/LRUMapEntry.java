package org.apache.jcs.utils.struct;

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
import java.util.Map.Entry;

/**
 * Entry for the LRUMap.
 * <p>
 * @author Aaron Smuts
 */
public class LRUMapEntry
    implements Entry, Serializable
{
    private static final long serialVersionUID = -8176116317739129331L;

    private Object key;

    private Object value;

    /**
     * S
     * @param key
     * @param value
     */
    public LRUMapEntry( Object key, Object value )
    {
        this.key = key;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map$Entry#getKey()
     */
    public Object getKey()
    {
        return this.key;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map$Entry#getValue()
     */
    public Object getValue()
    {
        return this.value;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map$Entry#setValue(java.lang.Object)
     */
    public Object setValue( Object valueArg )
    {
        Object old = this.value;
        this.value = valueArg;
        return old;
    }
}
