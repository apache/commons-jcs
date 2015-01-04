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
package org.apache.commons.jcs.jcache.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXs
{
    private static final MBeanServer SERVER = findMBeanServer();

    public static MBeanServer server()
    {
        return SERVER;
    }

    public static void register(final ObjectName on, final Object bean)
    {
        if (!SERVER.isRegistered(on))
        {
            try
            {
                SERVER.registerMBean(bean, on);
            }
            catch (final Exception e)
            {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public static void unregister(final ObjectName on)
    {
        if (SERVER.isRegistered(on))
        {
            try
            {
                SERVER.unregisterMBean(on);
            }
            catch (final Exception e)
            {
                // no-op
            }
        }
    }

    private static MBeanServer findMBeanServer()
    {
        if (System.getProperty("javax.management.builder.initial") != null)
        {
            return MBeanServerFactory.createMBeanServer();
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    private JMXs()
    {
        // no-op
    }
}
