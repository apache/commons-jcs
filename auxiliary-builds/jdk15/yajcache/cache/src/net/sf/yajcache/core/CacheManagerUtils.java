/*
 * CacheManagerUtils.java
 *
 * Created on 19 January 2005, 00:05
 */

package net.sf.yajcache.core;

/**
 *
 * @author Hanson Char
 */
enum CacheManagerUtils {
    inst;
    /** Checks the value type assignability of an existing cache. */
    void checkValueType(ICache c, Class<?> valueType) {
        if (!c.getValueType().isAssignableFrom(valueType))
            throw new ClassCastException("Cache " + c.getName()
                + " of " + c.getValueType() 
                + " already exists and cannot be used for " + valueType);
        return;
    }
}
