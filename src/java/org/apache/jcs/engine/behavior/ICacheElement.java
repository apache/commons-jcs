
package org.apache.jcs.engine.behavior;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheElement extends Serializable
{
    //, Cloneable

    /**
     * Gets the cacheName attribute of the ICacheElement object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    /**
     * Gets the key attribute of the ICacheElement object
     *
     * @return The key value
     */
    public Serializable getKey();


    /**
     * Gets the val attribute of the ICacheElement object
     *
     * @return The val value
     */
    public Serializable getVal();


    /**
     * Gets the attributes attribute of the ICacheElement object
     *
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes();


    /**
     * Sets the attributes attribute of the ICacheElement object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr );
}
