/*
 * SoftRefCacheSafe.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.soft;

import java.io.Serializable;
import java.util.Map;
import net.sf.yajcache.core.ICacheSafe;
import net.sf.yajcache.util.BeanUtils;
import net.sf.yajcache.util.SerializeUtils;

/**
 *
 * @author Hanson Char
 */
public class SoftRefCacheSafe<V> extends SoftRefCache<V> 
        implements ICacheSafe<V> 
{
    public SoftRefCacheSafe(String name, Class<V> valueType, 
            int initialCapacity, float loadFactor, int concurrencyLevel) 
    {
        super(name, valueType, initialCapacity, loadFactor, concurrencyLevel);
    }
    public SoftRefCacheSafe(String name, Class<V> valueType, 
            int initialCapacity) 
    {
        super(name, valueType, initialCapacity);
    }

    public SoftRefCacheSafe(String name, Class<V> valueType) {
        super(name, valueType);
    }
    public V getCopy(String key) {
        V val = this.get(key);
        return this.dup(val);
    }
    public V putCopy(String key, V value) {
        return this.put(key, this.dup(value));
    }
    public void putAll(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), e.getValue());
    }
    public void putAllCopies(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), this.dup(e.getValue()));
    }
    public V getBeanCopy(String key) {
        V val = this.get(key);
        return BeanUtils.inst.cloneDeep(val);
    }
    public V putBeanCopy(String key, V value) {
        return this.put(key, BeanUtils.inst.cloneDeep(value));
    }
    public void putAllBeanCopies(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), BeanUtils.inst.cloneDeep(e.getValue()));
    }
    public V getBeanClone(String key) {
        V val = this.get(key);
        return BeanUtils.inst.cloneShallow(val);
    }
    public V putBeanClone(String key, V value) {
        return this.put(key, BeanUtils.inst.cloneShallow(value));
    }
    public void putAllBeanClones(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), BeanUtils.inst.cloneShallow(e.getValue()));
    }
    private V dup(V val) {
        if (val instanceof Serializable) {
            return (V)SerializeUtils.inst.dup((Serializable)val);
        }
        return val;
    }
}
