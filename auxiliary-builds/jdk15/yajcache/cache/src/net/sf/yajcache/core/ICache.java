/*
 * ICache.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.core;

import java.util.Map;

import net.sf.yajcache.annotate.*;


/**
 * Interface of a Cache.
 *
 * @author Hanson Char
 */
@ThreadSafety(ThreadSafetyType.SAFE)
public interface ICache<V> extends Map<String,V> {
    /** Returns the cache name. */
    @ThreadSafety(ThreadSafetyType.IMMUTABLE)
    public String getName();
    /** Returns the value type of the cached items. */
    @ThreadSafety(ThreadSafetyType.IMMUTABLE)
    public Class<V> getValueType();
    /** Returns the value cached for the specified key. */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="This method itself is thread-safe.  However,"
             + " the thread-safetyness of the return value cannot be guaranteed"
             + " by this interface."
    )
    public V get(String key);
}
