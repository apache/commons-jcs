/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Annotates the target is for testing purposes only.
 *
 * @author Hanson Char
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface TestOnly {
    String value() default "";
}
