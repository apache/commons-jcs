package org.apache.jcs.utils.struct;

import org.apache.jcs.engine.memory.util.DoubleLinkedListNode;

/**
 * @author aaronsm
 *  
 */
public class LRUElementDescriptor
    extends DoubleLinkedListNode
{

    /**
     * <code>key</code>
     */
    private Object key;

    /**
     * @param key
     * @param payloadP
     */
    public LRUElementDescriptor( Object key, Object payloadP )
    {
        super( payloadP );
        this.setKey( key );
    }

    /**
     * @param key
     *            The key to set.
     */
    public void setKey( Object key )
    {
        this.key = key;
    }

    /**
     * @return Returns the key.
     */
    public Object getKey()
    {
        return key;
    }

}
