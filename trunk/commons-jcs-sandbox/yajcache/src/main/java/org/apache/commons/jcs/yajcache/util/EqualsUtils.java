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

import java.util.Arrays;

/**
 * @author Hanson Char
 */
public enum EqualsUtils {
    inst;

    /**
     * Returns true if the two input arguments are equal, handling cases
     * when they are arrays.
     * Returns false otherwise.
     */
    public boolean equals(Object lhs, Object rhs) {
        if (lhs == rhs)
            return true;
        if (lhs == null || rhs == null)
            return false;
        Class lClass = lhs.getClass();
        Class rClass = rhs.getClass();

        if (lClass.isArray()
        &&  rClass.isArray())
        {
            Class lCompType = lClass.getComponentType();
            Class rCompType = rClass.getComponentType();

            if (lCompType.isPrimitive()) {
                if (rCompType.isPrimitive()) {
                    if (lCompType != rCompType)
                        return false;
                    if (lCompType == int.class)
                        return Arrays.equals((int[])lhs, (int[])rhs);
                    if (lCompType == boolean.class)
                        return Arrays.equals((boolean[])lhs, (boolean[])rhs);
                    if (lCompType == byte.class)
                        return Arrays.equals((byte[])lhs, (byte[])rhs);
                    if (lCompType == char.class)
                        return Arrays.equals((char[])lhs, (char[])rhs);
                    if (lCompType == double.class)
                        return Arrays.equals((double[])lhs, (double[])rhs);
                    if (lCompType == float.class)
                        return Arrays.equals((float[])lhs, (float[])rhs);
                    if (lCompType == long.class)
                        return Arrays.equals((long[])lhs, (long[])rhs);
                    if (lCompType == short.class)
                        return Arrays.equals((short[])lhs, (short[])rhs);
                }
                return false;
            }
            if (rCompType.isPrimitive())
                return false;
            return Arrays.equals((Object[])lhs, (Object[])rhs);
        }
        return lhs.equals(rhs);
    }
}
