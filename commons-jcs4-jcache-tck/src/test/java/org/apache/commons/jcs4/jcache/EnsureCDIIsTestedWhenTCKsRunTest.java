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
package org.apache.commons.jcs4.jcache;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.cache.annotation.BeanProvider;

import org.junit.jupiter.api.Test;

// useless test but without it we are not sure
// CDI TCKs passed
class EnsureCDIIsTestedWhenTCKsRunTest
{
    @Test
    void testCheckOWBProvider()
    {
        try {
            final Iterator<BeanProvider> iterator = ServiceLoader.load(BeanProvider.class).iterator();
            assertTrue(iterator.hasNext());
            assertInstanceOf(OWBBeanProvider.class, iterator.next());
        } catch (final UnsupportedClassVersionError e) {
            System.err.println("Ignoring checkOWBProvider test failure on " + System.getProperty("java.version"));
        }
    }
}
