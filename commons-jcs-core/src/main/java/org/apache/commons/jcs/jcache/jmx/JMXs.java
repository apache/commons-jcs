package org.apache.commons.jcs.jcache.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXs {
    private static final MBeanServer SERVER = findMBeanServer();

    public static MBeanServer server() {
        return SERVER;
    }

    public static void register(final ObjectName on, final Object bean) {
        if (!SERVER.isRegistered(on)) {
            try {
                SERVER.registerMBean(bean, on);
            } catch (final Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public static void unregister(final ObjectName on) {
        if (SERVER.isRegistered(on)) {
            try {
                SERVER.unregisterMBean(on);
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private static MBeanServer findMBeanServer() {
        if (System.getProperty("javax.management.builder.initial") != null) {
            return MBeanServerFactory.createMBeanServer();
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    private JMXs() {
        // no-op
    }
}
