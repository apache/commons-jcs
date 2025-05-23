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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.jcs.yajcache.lang.annotation.CopyRightApache;
import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
@CopyRightApache
@TestOnly
public class SerializeUtilsTest extends TestCase {
    private final Log log = LogFactory.getLog(this.getClass());
    /**
     * Test of dup method, of class org.apache.commons.jcs.yajcache.util.SerializeUtils.
     */
    public void testDup() {
        // Test string dup: no clone
        log.debug("Test string dup: no clone");
        final String s1 = "Abc Def";
        final String s2 = SerializeUtils.inst.dup(s1);
        assertTrue(s1 == s2);
        // Test non-string Seriailzable dup: deep clone
        log.debug("Test non-string Seriailzable dup: deep clone");
        final TestSerializable o1 = new TestSerializable("abc Def");
        final TestSerializable o2 = SerializeUtils.inst.dup(o1);
        assertFalse(o1 == o2);
        assertEquals(o1, o2);
        // Test string array dup: shallow clone
        log.debug("Test string array dup: shallow clone");
        final String[] sa = {"1", "2", "3"};
        final String[] sa1 = SerializeUtils.inst.dup(sa);
        assertFalse(sa == sa1);
        assertTrue(Arrays.equals(sa, sa1));
        for (int i=sa.length-1; i > -1; i--) {
            assertTrue(sa[i] == sa1[i]);
        }
        // Test int array dup: shallow clone
        log.debug("Test int array dup: shallow clone");
        final int[] ia = {1,2,3};
        final int[] ia1 = SerializeUtils.inst.dup(ia);
        assertFalse(ia == ia1);
        assertTrue(Arrays.equals(ia, ia1));
        for (int i=ia.length-1; i > -1; i--) {
            assertTrue(ia[i] == ia1[i]);
        }
        // Test Integer array dup: shallow clone
        log.debug("Test Integer array dup: shallow clone");
        final Integer[] inta = {1,2,3};
        final Integer[] inta1 = SerializeUtils.inst.dup(inta);
        assertFalse(inta == inta1);
        assertTrue(Arrays.equals(inta, inta1));

        for (int i=inta.length-1; i > -1; i--) {
            assertTrue(inta[i] == inta1[i]);
        }
        // Test non-string Seriailzable array dup: deep clone
        log.debug("Test non-string Seriailzable array dup: deep clone");
        final TestSerializable[] ta1 = {new TestSerializable("abc Def"), new TestSerializable("123"), new TestSerializable("assdk")};
        final TestSerializable[] ta2 = SerializeUtils.inst.dup(ta1);
        assertFalse(ta1 == ta2);
        assertTrue(Arrays.equals(ta1, ta2));

        for (int i=ta1.length-1; i > -1; i--) {
            assertFalse(ta1[i] == ta2[i]);
            assertEquals(ta1[i], ta2[i]);
        }
    }
}
