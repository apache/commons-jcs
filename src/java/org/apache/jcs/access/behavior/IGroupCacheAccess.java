package org.apache.jcs.access.behavior;


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


import java.util.Set;

import org.apache.jcs.access.exception.CacheException;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 */
public interface IGroupCacheAccess extends ICacheAccess
{
    /**
     * Gets the g attribute of the IGroupCacheAccess object
     *
     * @return The g value
     */
    Object getFromGroup( Object name, String group );


    /** Description of the Method */
    void putInGroup( Object key, String group, Object obj )
        throws CacheException;


    /** Description of the Method */
    void putInGroup( Object key, String group, Object obj, IElementAttributes attr )
        throws CacheException;

    /** Description of the Method */
    public void remove( Object name, String group );

    /**
     * Gets the set of keys of objects currently in the group
     */
    public Set getGroupKeys(String group);

    /** Invalidates a group */
    public void invalidateGroup( String group );
}
