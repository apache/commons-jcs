/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Unsupported Operation.
 *
 * @author Hanson Char
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnsupportedOperation {
    String value() default "";
}
