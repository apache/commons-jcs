/*
 * SerializeUtils.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import org.apache.commons.lang.SerializationUtils;

/**
 * @author Hanson Char
 */
public enum SerializeUtils {
    inst;
    /**
     * Duplicates the given object.
     *
     * @return a duplicate of the given Serializable object,
     * short-cutting the deep clone process if possible.
     */
    public <V extends Serializable> V dup(V obj) {
        Class k = null;
        
        if (obj == null 
        ||  ClassUtils.inst.isImmutable(k=obj.getClass()))
            return obj;
        Class t = k.getComponentType();
        
        if (t != null) {
            // an array.
            if (ClassUtils.inst.isImmutable(t))
            {
                // array elements are immutable.
                // short cut via shallow clone.
                return this.cloneArray(obj);
            }
        }
        // deep clone.
        return (V)SerializationUtils.clone(obj);
    }
    private <A> A cloneArray(A a) {
        int len = Array.getLength(a);
	Object result = Array.newInstance(a.getClass().getComponentType(), len);
        System.arraycopy(a, 0, result, 0, len);
        return (A)result;
    }
//    public Class<?> getLeaveComponentType(Class<?> k) {
//        if (k == null)
//            return k;
//        if (k.isArray()) {
//            return this.getLeaveComponentType(k.getComponentType());
//        }
//        return k.getClass();
//    }
}
