/*
 * CacheEntry.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.core;

import java.util.Map;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Hanson Char
 */
public class CacheEntry<V> implements Map.Entry<String,V> {
    private final String key;
    private V value;
    /** Creates a new instance of CacheEntry */
    public CacheEntry(String key, V val) {
        this.key = key;
        this.value = val;
    }

    public String getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V val) {
        V ret = this.value;
        this.value = val;
        return ret;
    }
    @Override public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
