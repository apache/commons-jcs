package org.apache.jcs.utils.struct;

import java.io.Serializable;
import java.util.Map.Entry;

/**
 * Entry for the LRUMap.
 * 
 * @author Aaron Smuts
 * 
 */
public class LRUMapEntry
    implements Entry, Serializable
{
    private static final long serialVersionUID = -8176116317739129331L;

    private Object key;

    private Object value;

    /**
     * S
     * @param key
     * @param value
     */
    public LRUMapEntry( Object key, Object value )
    {
        this.key = key;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map$Entry#getKey()
     */
    public Object getKey()
    {
        return this.key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map$Entry#getValue()
     */
    public Object getValue()
    {
        return this.value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map$Entry#setValue(java.lang.Object)
     */
    public Object setValue( Object valueArg )
    {
        Object old = this.value;
        this.value = valueArg;
        return old;
    }

}
