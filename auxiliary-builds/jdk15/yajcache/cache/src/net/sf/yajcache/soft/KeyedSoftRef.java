/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.soft;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * Soft reference with an embedded key.
 *
 * @author Hanson Char
 */
class KeyedSoftRef<T> extends SoftReference<T> {
    private final String key;
    
//    KeyedSoftRef(String key, T value) {
//	super(value);
//        this.key = key;
//    }
    KeyedSoftRef(String key, T value, ReferenceQueue<? super T> q) {
        super(value, q);
        this.key = key;
    }
    public String getKey() {
        return this.key;
    }
}
