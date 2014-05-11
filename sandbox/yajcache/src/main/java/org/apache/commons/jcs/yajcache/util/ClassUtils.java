package org.apache.commons.jcs.yajcache.util;

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

import org.apache.commons.jcs.yajcache.lang.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
/**
 *
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum ClassUtils {
    inst;
    /**
     * Returns true if instances of the given class is known to be immutable;
     * false if we don't know.
     */
    public boolean isImmutable(@NonNullable Class t) {
        return t == String.class
        ||  t.isPrimitive()
        ||  t == Boolean.class
        ||  t == Byte.class
        ||  t == Character.class
        ||  t == Short.class
        ||  t == Integer.class
        ||  t == Long.class
        ||  t == Float.class
        ||  t == Double.class
        ||  t == BigInteger.class
        ||  t == BigDecimal.class
        ||  t.isEnum()
        ;
    }
    public boolean isImmutable(@NonNullable Object obj) {
        return this.isImmutable(obj.getClass());
    }
}
