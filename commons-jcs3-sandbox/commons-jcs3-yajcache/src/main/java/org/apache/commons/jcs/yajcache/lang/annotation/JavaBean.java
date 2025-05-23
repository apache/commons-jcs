package org.apache.commons.jcs.yajcache.lang.annotation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Element so annotated is expected to be a JavaBean.
 */
@CopyRightApache
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.TYPE,           // return value of a method is a
    ElementType.METHOD,           // return value of a method is a
    ElementType.FIELD,          // field is never null
    ElementType.LOCAL_VARIABLE, // variable is never null
    ElementType.PARAMETER       // parameter is never null
})
public @interface JavaBean {
}
