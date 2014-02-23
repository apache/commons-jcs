package org.apache.commons.jcs.auxiliary.disk;

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

import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.jcs.TestLogConfigurationUtil;

/** Unit tests for the LRUMapJCS implementation. */
public class LRUMapJCSUnitTest
    extends TestCase
{
    /** Verify that we default to unlimited */
    public void testDefault()
    {
        // SETUP

        // DO WORK
        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>();

        // VERIFY
        assertEquals( "Should be unlimted", -1, map.getMaxObjects() );
    }

    /** Verify that we default to unlimited */
    public void testLimited()
    {
        // SETUP
        int expected = 100;

        // DO WORK
        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>( expected );

        // VERIFY
        assertEquals( "Should be expected", expected, map.getMaxObjects() );
    }

    /** Verify that the log message. */
    public void testProcessRemovedLRU()
    {
        // SETUP
        StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, LRUMapJCS.class.getName() );

        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>();

        String key = "myKey";
        String value = "myValue";

        // DO WORK
        map.processRemovedLRU( key, value );
        String result = stringWriter.toString();

        // VERIFY
        assertTrue( "Debug log should contain the key,", result.indexOf( key ) != -1 );
        assertTrue( "Debug log should contain the value,", result.indexOf( value ) != -1 );
    }
}
