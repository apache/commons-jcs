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
package org.apache.jcs.yajcache.core;

import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * Cache change event listener/handler.
 *
 * @author Hanson CHar
 */
@CopyRightApache
public interface ICacheChangeHandler<V> {
    public boolean handlePut(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value);
    public boolean handlePutCopy(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value);
    public boolean handlePutBeanCopy(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value);
    public boolean handlePutBeanClone(@NonNullable String cacheName, 
            @NonNullable String key, @NonNullable V value);
    public boolean handleRemove(
            @NonNullable String cacheName, @NonNullable String key);
    public boolean handleClear(@NonNullable String cacheName);
}
