package org.apache.commons.jcs3.engine.match;

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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** Tests for the key matcher. */
public class KeyMatcherPatternImpllUnitTest
{
    /**
     * Verify that the matching method works.
     */
    @Test
    public void testGetMatchingKeysFromArray_AllMatch()
    {
        // SETUP
        final int numToInsertPrefix1 = 10;
        final Set<String> keyArray = new HashSet<>();

        final String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        final KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<>();

        // DO WORK
        final Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + ".", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    @Test
    public void testGetMatchingKeysFromArray_AllMatchFirstNull()
    {
        // SETUP
        final int numToInsertPrefix1 = 10;
        final Set<String> keyArray = new HashSet<>();

        final String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 1; i < numToInsertPrefix1 + 1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        final KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<>();

        // DO WORK
        final Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + "\\S+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    @Test
    public void testGetMatchingKeysFromArray_TwoTypes()
    {
        // SETUP
        final int numToInsertPrefix1 = 10;
        final int numToInsertPrefix2 = 50;
        final Set<String> keyArray = new HashSet<>();

        final String keyprefix1 = "MyPrefixA";
        final String keyprefix2 = "MyPrefixB";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        // insert with prefix2
        for ( int i = numToInsertPrefix1; i < numToInsertPrefix2 + numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix2 + String.valueOf( i ));
        }

        final KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<>();

        // DO WORK
        final Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + ".+", keyArray );
        final Set<String> result2 = keyMatcher.getMatchingKeysFromArray( keyprefix2 + ".+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2: " + result2, numToInsertPrefix2, result2.size() );
    }
}
