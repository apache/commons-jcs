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

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigurableMBeanServerIdBuilder extends MBeanServerBuilder
{
    private static ConcurrentMap<Key, MBeanServer> JVM_SINGLETONS = new ConcurrentHashMap<Key, MBeanServer>();

    private static class Key
    {
        private final String domain;
        private final MBeanServer outer;

        private Key(final String domain, final MBeanServer outer)
        {
            this.domain = domain;
            this.outer = outer;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Key key = Key.class.cast(o);
            return !(domain != null ? !domain.equals(key.domain) : key.domain != null)
                    && !(outer != null ? !outer.equals(key.outer) : key.outer != null);

        }

        @Override
        public int hashCode()
        {
            int result = domain != null ? domain.hashCode() : 0;
            result = 31 * result + (outer != null ? outer.hashCode() : 0);
            return result;
        }
    }

    @Override
    public MBeanServer newMBeanServer(final String defaultDomain, final MBeanServer outer, final MBeanServerDelegate delegate)
    {
        final Key key = new Key(defaultDomain, outer);
        MBeanServer server = JVM_SINGLETONS.get(key);
        if (server == null)
        {
            server = super.newMBeanServer(defaultDomain, outer, new ForceIdMBeanServerDelegate(delegate));
            final MBeanServer existing = JVM_SINGLETONS.putIfAbsent(key, server);
            if (existing != null)
            {
                server = existing;
            }
        }
        return server;
    }

    private class ForceIdMBeanServerDelegate extends MBeanServerDelegate
    {
        private final MBeanServerDelegate delegate;

        public ForceIdMBeanServerDelegate(final MBeanServerDelegate delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public String getMBeanServerId()
        {
            return System.getProperty("org.jsr107.tck.management.agentId", delegate.getMBeanServerId());
        }

        @Override
        public String getSpecificationName()
        {
            return delegate.getSpecificationName();
        }

        @Override
        public String getSpecificationVersion()
        {
            return delegate.getSpecificationVersion();
        }

        @Override
        public String getSpecificationVendor()
        {
            return delegate.getSpecificationVendor();
        }

        @Override
        public String getImplementationName()
        {
            return delegate.getImplementationName();
        }

        @Override
        public String getImplementationVersion()
        {
            return delegate.getImplementationVersion();
        }

        @Override
        public String getImplementationVendor()
        {
            return delegate.getImplementationVendor();
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo()
        {
            return delegate.getNotificationInfo();
        }

        @Override
        public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback)
                throws IllegalArgumentException
        {
            delegate.addNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback)
                throws ListenerNotFoundException
        {
            delegate.removeNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException
        {
            delegate.removeNotificationListener(listener);
        }

        @Override
        public void sendNotification(final Notification notification)
        {
            delegate.sendNotification(notification);
        }
    }
}
