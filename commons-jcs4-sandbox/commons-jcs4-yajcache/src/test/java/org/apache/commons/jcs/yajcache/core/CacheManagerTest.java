package org.apache.commons.jcs.yajcache.core;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.jcs4.log.Log;
import org.junit.jupiter.api.Test;

/**
 */
@TestOnly
public class CacheManagerTest {
    /** The logger. */
    private final Log log = Log.getLog(this.getClass());

    @Test
    public void testGetCache() {
        CacheManager.inst.getCache("myCache", String.class);
        CacheManager.inst.removeCache("myCache");
        log.debug("Test getCache and get");
        ICache<String> c = CacheManager.inst.getCache(
                "myCache", String.class, CacheType.SOFT_REFERENCE);
        assertTrue(null == c.get("bla"));
        log.debug("Test getCache and put");
        c = CacheManager.inst.getCache("myCache", String.class);
        c.put("bla", "First Put");
        assertTrue("First Put" == c.get("bla"));
        assertEquals(c.size(), 1);
        log.debug("Test getCache and remove");
        c = CacheManager.inst.getCache("myCache", String.class);
        c.remove("bla");
        assertTrue(null == c.get("bla"));
        log.debug("Test getCache and two put's");
        c = CacheManager.inst.getCache("myCache", String.class);
        c.put("1", "First Put");
        c.put("2", "Second Put");
        assertEquals(2, c.size());
        assertEquals("Second Put", c.get("2"));
        assertEquals("First Put", c.get("1"));
        log.debug("Test getCache and clear");
        c = CacheManager.inst.getCache("myCache", String.class);
        c.clear();
        assertEquals(c.size(), 0);
        assertNull(c.get("2"));
        assertNull(c.get("1"));
        log.debug("Test getCache and getValueType");
        final ICache<?> c1 = CacheManager.inst.getCache("myCache");
        assertTrue(c1.getValueType() == String.class);

        log.debug("Test checking of cache value type");
        assertNull(CacheManager.inst.getCache("myCache", Integer.class), "Expected null");
        log.debug(CacheManager.inst);
    }

    @Test
    public void testGetCacheRaceCondition() {
        log.debug("Test simulation of race condition in creating cache");
        CacheManager.inst.removeCache("race");
        final ICache<?> intCache = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE);
        final ICache<?> intCache1 = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE);
        log.debug("Test simulation of the worst case scenario: "
                + "race condition in creating cache AND class cast exception");
        assertThrows(ClassCastException.class, () -> CacheManager.inst.testCreateCacheRaceCondition(
                    "race", Double.class, CacheType.SOFT_REFERENCE),
                "Bug: Cache for Integer cannot be used for Double");
        assertSame(intCache, intCache1);
        log.debug(CacheManager.inst);
    }

    @Test
    public void testRemoveCache() {
        log.debug("Test remove cache");
        final ICache<Integer> intCache = CacheManager.inst.getCache("race", Integer.class, CacheType.SOFT_REFERENCE);
        intCache.put("1", 1);
        assertEquals(intCache.size(), 1);
        assertEquals(intCache, CacheManager.inst.removeCache("race"));
        assertEquals(intCache.size(), 0);
        final ICache<?> intCache1 = CacheManager.inst.getCache("race", Integer.class);
        assertNotSame(intCache, intCache1);
        CacheManager.inst.removeCache("race");
        final ICache<Double> doubleCache = CacheManager.inst.testCreateCacheRaceCondition(
                    "race", Double.class, CacheType.SOFT_REFERENCE);
        doubleCache.put("double", 1.234);
        assertEquals(1.234, doubleCache.get("double"));
        log.debug(CacheManager.inst);
    }
}
