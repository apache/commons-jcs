/*
 * CacheManagerUtils.java
 *
 * $Revision$ $Date$
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
        Class<?> cacheValueType = c.getValueType();
        
        if (!cacheValueType.isAssignableFrom(valueType))
            throw new ClassCastException("Cache " + c.getName()
                + " of " + c.getValueType() 
                + " already exists and cannot be used for " + valueType);
        return;
    }
}
