/*
 * ThreadSafetyType.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

/**
 * Thread Safety Types.
 *
 * http://www-106.ibm.com/developerworks/java/library/j-jtp09263.html
 *
 * @author Hanson Char
 */
public enum ThreadSafetyType {
    /**  Immutable objects are guaranteed to be thread-safe. */
    IMMUTABLE, 
    SAFE, 
    /**
     * Conditionally thread-safe classes are those for which each individual 
     * operation may be thread-safe, but certain sequences of operations may 
     * require external synchronization. The most common example of 
     * conditional thread safety is traversing an iterator returned from 
     * Hashtable or Vector -- the fail-fast iterators returned by these 
     * classes assume that the underlying collection will not be mutated 
     * while the iterator traversal is in progress. To ensure that other 
     * threads will not mutate the collection during traversal, the 
     * iterating thread should be sure that it has exclusive access to 
     * the collection for the entirety of the traversal. Typically, 
     * exclusive access is ensured by synchronizing on a lock -- and the 
     * class's documentation should specify which lock that is 
     * (typically the object's intrinsic monitor).
     */
    CONDITIONAL, 
    /**
     * Thread-compatible classes are not thread-safe, but can be used 
     * safely in concurrent environments by using synchronization 
     * appropriately.
     */
    COMPATIBLE, 
    /**
     * Thread-hostile classes are those that cannot be rendered safe to 
     * use concurrently, regardless of what external synchronization is 
     * invoked.
     */
    HOSTILE
}
