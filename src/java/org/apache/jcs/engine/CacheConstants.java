package org.apache.jcs.engine;

/**
 * Constants used throughout the JCS cache engine
 *
 * @author jtaylor
 * @version $Id$
 */
public interface CacheConstants
{
    public static final String DEFAULT_CONFIG = "/cache.ccf";

    /** Cache alive status. */
    public final static int STATUS_ALIVE = 1;

    /** Cache disposed status. */
    public final static int STATUS_DISPOSED = 2;

    /** Cache in error. */
    public final static int STATUS_ERROR = 3;

    /** Delimiter of a cache name component. */
    public final static String NAME_COMPONENT_DELIMITER = ":";
}
