/* ========================================================================
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * ========================================================================
 */
/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.soft;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * Soft reference with an embedded key.
 *
 * @author Hanson Char
 */
class KeyedSoftRef<T> extends SoftReference<T> {
    private final String key;
    
//    KeyedSoftRef(String key, T value) {
//	super(value);
//        this.key = key;
//    }
    KeyedSoftRef(String key, T value, ReferenceQueue<? super T> q) {
        super(value, q);
        this.key = key;
    }
    public String getKey() {
        return this.key;
    }
}
