package org.apache.jcs.engine;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache
 * License, Version 2.0 (the "License") you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.util.Hashtable;
import java.util.Map;

import org.apache.jcs.engine.behavior.ICache;

/**
 * Used to associates a set of [cache listener to cache event queue] for a
 * cache.
 */
public class CacheListeners
{
    /** Description of the Field */
    public final ICache cache;

    /*
     * Map ICacheListener to ICacheEventQueue.
     */
    /** Description of the Field */
    public final Map eventQMap = new Hashtable();

    /**
     * Constructs with the given cache.
     * <p>
     * @param cache
     */
    public CacheListeners( ICache cache )
    {
        if ( cache == null )
        {
            throw new IllegalArgumentException( "cache must not be null" );
        }
        this.cache = cache;
    }
}
