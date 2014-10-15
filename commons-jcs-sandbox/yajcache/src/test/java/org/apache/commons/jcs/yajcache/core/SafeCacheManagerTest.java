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
import org.apache.commons.jcs.yajcache.util.TestSerializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TestOnly
public class SafeCacheManagerTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());

    public void testGetCache() {
        log.debug("Test getCache and get");
        ICacheSafe<String> c = CacheManager.inst.getSafeCache(
                "myCache", String.class, CacheType.SOFT_REFERENCE_SAFE);
        assertTrue(null == c.get("bla"));
        log.debug("Test getCache and put");
        c = CacheManager.inst.getSafeCache("myCache", String.class);
        c.put("bla", "First Put");
        assertTrue("First Put" == c.get("bla"));
        assertEquals(c.size(), 1);
        log.debug("Test getCache and remove");
        c = CacheManager.inst.getSafeCache("myCache", String.class);
        c.remove("bla");
        assertTrue(null == c.get("bla"));
        log.debug("Test getCache and two put's");
        c = CacheManager.inst.getSafeCache("myCache", String.class);
        c.put("1", "First Put");
        c.put("2", "Second Put");
        assertEquals(c.size(), 2);
        assertTrue("Second Put" == c.get("2"));
        assertTrue("First Put" == c.get("1"));
        log.debug("Test getCache and clear");
        c = CacheManager.inst.getSafeCache("myCache", String.class);
        c.clear();
        assertEquals(c.size(), 0);
        assertTrue(null == c.get("2"));
        assertTrue(null == c.get("1"));
        log.debug("Test getCache and getValueType");
        ICacheSafe c1 = CacheManager.inst.getSafeCache("myCache");
        assertTrue(c1.getValueType() == String.class);
        log.debug("Test checking of cache value type");
        try {
            ICacheSafe<Integer> c2 = CacheManager.inst.getSafeCache("myCache", Integer.class);
            assert false : "Bug: Cache for string cannot be used for Integer.";
        } catch(ClassCastException ex) {
            // should go here.
        }
        log.debug(CacheManager.inst);
    }

    public void testGetCacheRaceCondition() {
        log.debug("Test simulation of race condition in creating cache");
        ICache intCache = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE_SAFE);
        ICache intCache1 = CacheManager.inst.testCreateCacheRaceCondition(
                "race", Integer.class, CacheType.SOFT_REFERENCE_SAFE);
        log.debug("Test simulation of the worst case scenario: "
                + "race condition in creating cache AND class cast exception");
        try {
            ICache doubleCache = CacheManager.inst.testCreateCacheRaceCondition(
                    "race", Double.class, CacheType.SOFT_REFERENCE_SAFE);
            assert false : "Bug: Cache for Integer cannot be used for Double.";
        } catch(ClassCastException ex) {
            // should go here.
        }
        assertTrue(intCache == intCache1);
    }

    public void testRemoveCache() {
        log.debug("Test remove cache");
        ICacheSafe<Integer> intCache = CacheManager.inst.getSafeCache("race", Integer.class);
        intCache.put("1", 1);
        assertEquals(intCache.size(), 1);
        assertEquals(intCache, CacheManager.inst.removeCache("race"));
        assertEquals(intCache.size(), 0);
        ICacheSafe intCache1 = CacheManager.inst.getSafeCache("race", Integer.class);
        assertFalse(intCache == intCache1);
        CacheManager.inst.removeCache("race");
        ICache<Double> doubleCache =
                CacheManager.inst.testCreateCacheRaceCondition(
                "race", Double.class, CacheType.SOFT_REFERENCE_SAFE);
        doubleCache.put("double", 1.234);
        assertEquals(1.234, doubleCache.get("double"));
        log.debug(CacheManager.inst);
    }

    public void testGetSafeCache() {
        log.debug("Test getCache and getCopy");
        {
            ICacheSafe<String> c = CacheManager.inst.getSafeCache(
                    "myCache", String.class, CacheType.SOFT_REFERENCE_SAFE);
            assertTrue(null == c.getCopy("bla"));
            log.debug("Test getCache and putCopy");
            c = CacheManager.inst.getSafeCache("myCache", String.class);
            c.putCopy("bla", "First Put");
            assertTrue("First Put" == c.getBeanClone("bla"));
            assertEquals(c.size(), 1);
            log.debug("Test getCache and remove");
            c = CacheManager.inst.getSafeCache("myCache", String.class);
            c.remove("bla");
            assertTrue(null == c.getCopy("bla"));
            log.debug("Test getCache and two putCopy's");
        }
        CacheManager.inst.removeCache("myCache");
        ICacheSafe<TestSerializable> c = CacheManager.inst.getSafeCache(
                "myCache", TestSerializable.class, CacheType.SOFT_REFERENCE_SAFE);
        TestSerializable[] ta = {
                new TestSerializable("First Put"),
                new TestSerializable("Second Put"),
                new TestSerializable("Third Put")
        };
        log.debug("test putCopy");
        c.putCopy("1", ta[0]);
        assertFalse(ta[0] == c.get("1"));
        assertEquals(ta[0], c.get("1"));
        log.debug("test putBeanCopy");
        c.putBeanCopy("2", ta[1]);
        assertFalse(ta[1] == c.get("2"));
        assertEquals(ta[1], c.get("2"));
        log.debug("test putBeanClone");
        c.putBeanClone("2a", ta[1]);
        assertFalse(ta[1] == c.get("2a"));
        assertEquals(ta[1], c.get("2a"));

        c.put("3", ta[2]);
        assertEquals(c.size(), 4);
        assertFalse(ta[1] == c.getBeanClone("2"));
        assertFalse(ta[0] == c.getBeanCopy("1"));
        assertFalse(ta[0] == c.get("1"));
        log.debug("Test get, getBeanClone, getBeanCopy and getCopy");
        assertTrue(ta[2] == c.get("3"));
        assertFalse(ta[2] == c.getBeanClone("3"));
        assertFalse(ta[2] == c.getBeanCopy("3"));
        assertFalse(ta[2] == c.getCopy("3"));
        assertEquals(ta[2], c.getBeanClone("3"));
        assertEquals(ta[2], c.getBeanCopy("3"));
        assertEquals(ta[2], c.getCopy("3"));
        log.debug("Test getCache and clear");
        c = CacheManager.inst.getSafeCache(
                "myCache", TestSerializable.class, CacheType.SOFT_REFERENCE_SAFE);
        c.clear();
        assertEquals(c.size(), 0);
        assertTrue(null == c.getCopy("2"));
        assertTrue(null == c.getCopy("1"));
        log.debug("Test getCache and getValueType");
        ICacheSafe c1 = CacheManager.inst.getSafeCache("myCache");
        assertTrue(c1.getValueType() == TestSerializable.class);
        log.debug("Test checking of cache value type");
        try {
            ICacheSafe<Integer> c2 = CacheManager.inst.getSafeCache("myCache", Integer.class);
            assert false : "Bug: Cache for string cannot be used for Integer.";
        } catch(ClassCastException ex) {
            // should go here.
        }
        log.debug(CacheManager.inst);
    }
}
