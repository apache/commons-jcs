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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.apache.commons.jcs4.engine.ElementAttributes.LastAccessHolder;

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
    public static ElementAttributes withEternalFalseAndMaxLife(Duration maxLife)
    {
        ElementAttributes element = new ElementAttributes(
                ElementAttributes.defaults().IsSpool(),
                ElementAttributes.defaults().IsLateral(),
                ElementAttributes.defaults().IsRemote(),
                false,
                maxLife,
                ElementAttributes.defaults().MaxIdleTime(),
                Instant.now(),
                new LastAccessHolder(Instant.EPOCH),
                new ArrayList<>());

        element.mutableLastAccessTime().lastAccessTime = element.createTime();
        return element;
    }

    /**
     * Get an ElementAttributes object suitable for tests.
     *
     * @param maxLife The new MaxLife value
     * @param maxIdleTime The new MaxIdleTime value
     */
    public static ElementAttributes withEternalFalseAndMaxLifeAndMaxIdleTime(Duration maxLife, Duration maxIdleTime)
    {
        ElementAttributes element = new ElementAttributes(
                ElementAttributes.defaults().IsSpool(),
                ElementAttributes.defaults().IsLateral(),
                ElementAttributes.defaults().IsRemote(),
                false,
                maxLife,
                maxIdleTime,
                Instant.now(),
                new LastAccessHolder(Instant.EPOCH),
                new ArrayList<>());

        element.mutableLastAccessTime().lastAccessTime = element.createTime();
        return element;
    }
}
