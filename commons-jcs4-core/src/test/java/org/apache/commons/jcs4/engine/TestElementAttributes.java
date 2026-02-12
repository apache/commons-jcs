package org.apache.commons.jcs4.engine;

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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Allow test access to parametrized ElementAttributes objects
 */
public class TestElementAttributes
{
    /**
     * Get an ElementAttributes object suitable for tests.
     *
     * @param maxLife The new MaxLife value
     */
    public static ElementAttributes withEternalFalseAndMaxLife(long maxLife)
    {
        ElementAttributes element = new ElementAttributes(
                ElementAttributes.defaults().isSpool(),
                ElementAttributes.defaults().isLateral(),
                ElementAttributes.defaults().isRemote(),
                false,
                maxLife,
                ElementAttributes.defaults().maxIdleTime(),
                System.currentTimeMillis(),
                new AtomicLong(),
                ElementAttributes.defaults().timeFactorForMilliseconds(),
                new ArrayList<>());

        element.mutableLastAccessTime().set(element.createTime());
        return element;
    }

    /**
     * Get an ElementAttributes object suitable for tests.
     *
     * @param maxLife The new MaxLife value
     * @param maxIdleTime The new MaxIdleTime value
     */
    public static ElementAttributes withEternalFalseAndMaxLifeAndMaxIdleTime(long maxLife, long maxIdleTime)
    {
        ElementAttributes element = new ElementAttributes(
                ElementAttributes.defaults().isSpool(),
                ElementAttributes.defaults().isLateral(),
                ElementAttributes.defaults().isRemote(),
                false,
                maxLife,
                maxIdleTime,
                System.currentTimeMillis(),
                new AtomicLong(),
                ElementAttributes.defaults().timeFactorForMilliseconds(),
                new ArrayList<>());

        element.mutableLastAccessTime().set(element.createTime());
        return element;
    }
}
