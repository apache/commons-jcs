package org.apache.jcs.engine.memory;

import java.io.Serializable;

import org.apache.jcs.engine.CacheElement;

/**
 * A cache entry in memory.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class MemoryElement extends CacheElement implements Serializable
{
    //public final Serializable key;
    //public final Serializable val;
    //public final long createTime;

    // needed for memory cache element LRU linked lisk
    /** Description of the Field */
    public MemoryElement prev, next;


    /**
     * Constructor for the MemoryElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public MemoryElement( String cacheName, Serializable key, Serializable val )
    {
        super( cacheName, key, val );
        //this.key = key;
        //this.val = val;
        //createTime = System.currentTimeMillis();
    }


    /** Description of the Method */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof MemoryElement ) )
        {
            return false;
        }
        MemoryElement to = ( MemoryElement ) obj;
        return key.equals( to.key ) && val.equals( to.val );
    }


    /** Description of the Method */
    public int hashCode()
    {
        return val.hashCode();
    }
}
