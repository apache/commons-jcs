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

package org.apache.jcs.yajcache.event;
import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
@TODO("Remove this class: no longer needed")
public enum CacheChangeEventType {
    CLEAR_CACHE,
    PUT,
    PUT_BEAN_CLONE,
    PUT_BEAN_COPY,
    PUT_COPY,
    REMOVE
}
