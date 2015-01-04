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

package org.apache.commons.jcs.jcache.lang;

public interface Subsitutor
{
    String substitute(String value);

    public static class Helper {
        public static final Subsitutor INSTANCE;
        static {
            Subsitutor value = null;
            for (final String name : new String[]
            { // ordered by features
                    "org.apache.commons.jcs.jcache.lang.Lang3Substitutor",
                    "org.apache.commons.jcs.jcache.lang.DefaultSubsitutor"
            })
            {
                try
                {
                    value = Subsitutor.class.cast(
                            Subsitutor.class.getClassLoader().loadClass(name).newInstance());
                    value.substitute("${java.version}"); // ensure it works
                }
                catch (final Throwable e) // not Exception otherwise NoClassDefFoundError
                {
                    // no-op: next
                }
            }
            if (value == null) {
                throw new IllegalStateException("Can't find a " + Subsitutor.class.getName());
            }
            INSTANCE = value;
        }
    }
}
