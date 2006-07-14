package org.apache.jcs.utils.struct;

/**
 * This is a node in the double linked list. It is stored as the value in the
 * underlying map used by the LRUMap class.
 * <p>
 * @author aaronsm
 */
public class LRUElementDescriptor
    extends DoubleLinkedListNode
{
    private static final long serialVersionUID = 8249555756363020156L;

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
