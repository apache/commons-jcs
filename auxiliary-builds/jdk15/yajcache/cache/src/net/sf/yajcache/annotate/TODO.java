/*
 * TODO.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Annotates what needs to be done.
 *
 * @author Hanson Char
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface TODO {
    /** Summary of what needs to be done. */
    String value() default "";
    /** Details of what needs to be done. */
    String details() default "";
}
