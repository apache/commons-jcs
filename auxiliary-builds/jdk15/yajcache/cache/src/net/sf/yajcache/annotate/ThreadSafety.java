/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Characterizing thread safety.
 *
 * http://www-106.ibm.com/developerworks/java/library/j-jtp09263.html
 *
 * @author Hanson Char
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadSafety {
    ThreadSafetyType value();
    String caveat() default "";
    String note() default "";
}
