package org.apache.commons.jcs.engine;

/**
 * Allow test access to set last access time without exposing public method
 */
public class ElementAttributesUtils {
    public static void setLastAccessTime(ElementAttributes ea, long time) {
        ea.setLastAccessTime(time);
    }
}
