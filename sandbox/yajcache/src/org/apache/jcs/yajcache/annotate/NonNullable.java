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

package org.apache.jcs.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Element so annotated is never expected to be null.
 *
 * @author Hanson Char
 */
@CopyRightApache
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.METHOD,         // return value of a method is never null
    ElementType.FIELD,          // field is never null
    ElementType.LOCAL_VARIABLE, // variable is never null
    ElementType.PARAMETER       // parameter is never null
})
public @interface NonNullable {
}
