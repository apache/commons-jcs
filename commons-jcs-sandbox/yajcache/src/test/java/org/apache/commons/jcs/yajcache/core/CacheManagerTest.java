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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import junit.framework.TestCase;

import org.apache.commons.jcs.yajcache.lang.annotation.CopyRightApache;
import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TestOnly
public class CacheManagerTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());

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
        assertEquals(c.size(), 2);
        assertTrue("Second Put" == c.get("2"));
        assertTrue("First Put" == c.get("1"));
        log.debug("Test getCache and clear");
        c = CacheManager.inst.getCache("myCache", String.class);
        c.clear();
        assertEquals(c.size(), 0);
        assertTrue(null == c.get("2"));
        assertTrue(null == c.get("1"));
        log.debug("Test getCache and getValueType");
        ICache c1 = CacheManager.inst.getCache("myCache");
        assertTrue(c1.getValueType() == String.class);
        log.debug("Test checking of cache value type");
        try {
            ICache<Integer> c2 = CacheManager.inst.getCache("myCache", Integer.class);
            assert false : "Bug: Cache for string cannot be used for Integer";
        } catch(ClassCastException ex) {
            // should go here.
        }
        log.debug(CacheManager.inst);
    }

    public void testGetCacheRaceCondition() {
        log.debug("Test simulation of race condition in creating cache");
        ICache intCache = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE);
        ICache intCache1 = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE);
        log.debug("Test simulation of the worst case scenario: "
                + "race condition in creating cache AND class cast exception");
        try {
            ICache<Double> doubleCache =
                    CacheManager.inst.testCreateCacheRaceCondition(
                    "race", Double.class, CacheType.SOFT_REFERENCE);
            assert false : "Bug: Cache for Integer cannot be used for Double";
        } catch(ClassCastException ex) {
            // should go here.
        }
        assertTrue(intCache == intCache1);
        log.debug(CacheManager.inst);
    }

    public void testRemoveCache() {
        log.debug("Test remove cache");
        ICache<Integer> intCache = CacheManager.inst.getCache("race", Integer.class);
        intCache.put("1", 1);
        assertEquals(intCache.size(), 1);
        assertEquals(intCache, CacheManager.inst.removeCache("race"));
        assertEquals(intCache.size(), 0);
        ICache intCache1 = CacheManager.inst.getCache("race", Integer.class);
        assertFalse(intCache == intCache1);
        CacheManager.inst.removeCache("race");
        ICache<Double> doubleCache = CacheManager.inst.testCreateCacheRaceCondition(
                    "race", Double.class, CacheType.SOFT_REFERENCE);
        doubleCache.put("double", 1.234);
        assertEquals(1.234, doubleCache.get("double"));
        log.debug(CacheManager.inst);
    }
}
