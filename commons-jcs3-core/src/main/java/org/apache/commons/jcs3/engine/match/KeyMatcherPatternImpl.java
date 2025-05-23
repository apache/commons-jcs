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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.engine.match.behavior.IKeyMatcher;

/** This implementation of the KeyMatcher uses standard Java Pattern matching. */
public class KeyMatcherPatternImpl<K>
    implements IKeyMatcher<K>
{
    /** Serial version */
    private static final long serialVersionUID = 6667352064144381264L;

    /**
     * Creates a pattern and find matches on the array.
     *
     * @param pattern
     * @param keyArray
     * @return Set of the matching keys
     */
    @Override
    public Set<K> getMatchingKeysFromArray( final String pattern, final Set<K> keyArray )
    {
        final Pattern compiledPattern = Pattern.compile( pattern );

        return keyArray.stream()
                .filter(key -> compiledPattern.matcher(key.toString()).matches())
                .collect(Collectors.toSet());
    }
}
