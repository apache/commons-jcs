package org.apache.jcs.auxiliary.disk;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extension of LRUMap for logging of removals. Can switch this back to a
 * HashMap easily.
 */
public class LRUMapJCS extends LRUMap
{

    private static final Log log = LogFactory.getLog( LRUMapJCS.class );

    /**
     * This creates an unbounded version.
     */
    public LRUMapJCS()
    {
        super();
    }

    /**
     * This creates a list bounded by the max key size argument.  The
     * Boundary is enforces by an LRU eviction policy.
     * <p>
     * This is used in the Disk cache to store keys and purgatory elements if a boundary
     * is requested.
     * <p>
     * The LRU memory cache uses its own LRU implementation.
     * 
     * @param maxKeySize
     */
    public LRUMapJCS(int maxKeySize)
    {
        super( maxKeySize );
    }

    /**
     * This is called when an item is removed from the LRU. We just log some
     * information.
     * 
     * @param key
     * @param value
     */
    protected void processRemovedLRU( Object key, Object value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "Removing key: [" + key + "] from key store, value = [" + value +"]" );
            log.debug( "Key store size: '" + this.size() + "'." );
        }

    }
}