package org.apache.jcs.engine;

/**
 * Constants used throughout the JCS cache engine
 *
 * @author jtaylor
 * @version $Id$
 */
public interface CacheConstants
{
    /**
     * Where the current activity came from. This effects whether the remote
     * will be included. Prevents remote-local loops.
     */
    public static final boolean REMOTE_INVOKATION = true;

    /**
     * Where the current activity came from. This effects whether the remote
     * will be included. Prevents remote-local loops.
     */
    public static final boolean LATERAL_INVOKATION = true;

    /**
     * Where the current activity came from. This effects whether the remote
     * will be included. Prevents remote-local loops.
     */
    public static final boolean LOCAL_INVOKATION = !REMOTE_INVOKATION;

    /** Whether the update should propagate to the remote */
    public static final boolean INCLUDE_REMOTE_CACHE = true;

    /** Whether the update should propagate to the remote */
    public static final boolean EXCLUDE_REMOTE_CACHE = !INCLUDE_REMOTE_CACHE;

    /** Description of the Field */
    public static final boolean MULTICAST_ON = true;

    /** Description of the Field */
    public static final boolean MULTICAST_OFF = false;

    /** Cache alive status. */
    public final static int STATUS_ALIVE = 1;

    /** Cache disposed status. */
    public final static int STATUS_DISPOSED = 2;

    /** Cache in error. */
    public final static int STATUS_ERROR = 3;

    /** Delimiter of a cache name component. */
    public final static String NAME_COMPONENT_DELIMITER = ":";
}
