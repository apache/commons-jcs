package org.apache.jcs.engine;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheGroup
{

    //private Atrributes attr;
    /** Description of the Field */
    public IElementAttributes attr;


    /** Constructor for the CacheGroup object */
    public CacheGroup() { }


    /**
     * Sets the attributes attribute of the CacheGroup object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = attr;
    }


    /**
     * Gets the attrributes attribute of the CacheGroup object
     *
     * @return The attrributes value
     */
    public IElementAttributes getElementAttrributes()
    {
        return attr;
    }

}
