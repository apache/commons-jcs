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
package org.apache.commons.jcs.jcache;

import org.junit.Test;

import javax.cache.annotation.BeanProvider;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// useless test but without it we are not sure
// CDI TCKs passed
public class EnsureCDIIsTestedWhenTCKsRunTest
{
    @Test
    public void checkOWBProvider()
    {
        try {
            final Iterator<BeanProvider> iterator = ServiceLoader.load(BeanProvider.class).iterator();
            assertTrue(iterator.hasNext());
            assertThat(iterator.next(), instanceOf(OWBBeanProvider.class));
        } catch (java.lang.UnsupportedClassVersionError e) {
            System.err.println("Ignoring checkOWBProvider test failure on " + System.getProperty("java.version"));
        }
    }
}
