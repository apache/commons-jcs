package org.apache.jcs.engine.memory;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class MemoryElementDescriptor implements Serializable
{

    // needed for memory cache element LRU linked lisk
    /** Description of the Field */
    public MemoryElementDescriptor prev, next;
    /** Description of the Field */
    public ICacheElement ce;


    /**
     * Constructor for the MemoryElementDescriptor object
     *
     * @param ce
     */
    public MemoryElementDescriptor( ICacheElement ce )
    {
        this.ce = ce;
    }

}
