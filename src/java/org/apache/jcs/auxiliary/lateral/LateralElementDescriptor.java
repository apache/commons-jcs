package org.apache.jcs.auxiliary.lateral;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralElementDescriptor implements Serializable
{

    // command types
    /** Description of the Field */
    public final static int UPDATE = 1;
    /** Description of the Field */
    public final static int REMOVE = 2;
    /** Description of the Field */
    public final static int REMOVEALL = 3;
    /** Description of the Field */
    public final static int DISPOSE = 4;

    /** Command to return an object. */
    public final static int GET = 5;

    /** Description of the Field */
    public ICacheElement ce;
    /** Description of the Field */
    public byte requesterId;

    /** Description of the Field */
    public int command = UPDATE;


    // for update command
    /** Constructor for the LateralElementDescriptor object */
    public LateralElementDescriptor() { }


    /**
     * Constructor for the LateralElementDescriptor object
     *
     * @param ce
     */
    public LateralElementDescriptor( ICacheElement ce )
    {
        this.ce = ce;
    }

}
