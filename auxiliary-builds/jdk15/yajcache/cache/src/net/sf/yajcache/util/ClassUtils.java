/*
 * ClassUtils.java
 *
 * Created on 19 January 2005, 00:17
 */

package net.sf.yajcache.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Hanson Char
 */
public enum ClassUtils {
    inst;
    /** 
     * Returns true if instances of the given class is known to be immutable; 
     * false if we don't know.
     */
    public boolean isImmutable(Class t) {
        return t == String.class
        ||  t.isPrimitive()
        ||  t == Boolean.class
        ||  t == Byte.class
        ||  t == Character.class
        ||  t == Short.class
        ||  t == Integer.class
        ||  t == Long.class
        ||  t == Float.class
        ||  t == Double.class
        ||  t == BigInteger.class
        ||  t == BigDecimal.class
        ||  t.isEnum()
        ;
    }
    public boolean isImmutable(Object obj) {
        return this.isImmutable(obj.getClass());
    }
}
