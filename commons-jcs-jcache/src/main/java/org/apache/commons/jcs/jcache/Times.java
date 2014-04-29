package org.apache.commons.jcs.jcache;

public class Times {
    public static long now() {
        return System.currentTimeMillis();
    }

    private Times() {
        // no-op
    }
}
