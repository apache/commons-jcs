package org.apache.jcs.engine;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Generic element wrapper. Often stuffed inside another.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheElement implements ICacheElement, Serializable
{

    /** Description of the Field */
    public final String cacheName;
    /** Description of the Field */
    public final Serializable key;
    /** Description of the Field */
    public final Serializable val;

    /** Description of the Field */
    public ElementAttributes attr;


    // make sure it is open for spooling?
    //public boolean isLocked = false;

    /**
     * Constructor for the CacheElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement( String cacheName, Serializable key, Serializable val )
    {
        this.cacheName = cacheName;
        this.key = key;
        this.val = val;
    }


    /**
     * Constructor for the CacheElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement( String cacheName, Serializable key, Object val )
    {
        this( cacheName, key, ( Serializable ) val );
    }


    /**
     * Gets the cacheName attribute of the CacheElement object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Gets the key attribute of the CacheElement object
     *
     * @return The key value
     */
    public Serializable getKey()
    {
        return this.key;
    }


    /**
     * Gets the val attribute of the CacheElement object
     *
     * @return The val value
     */
    public Serializable getVal()
    {
        return this.val;
    }


    /**
     * Sets the attributes attribute of the CacheElement object
     *
     * @param attr The new IElementAttributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = ( ElementAttributes ) attr;
    }


    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     *
     * @return The IElementAttributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return this.attr;
    }

    /** Description of the Method */
    public int hashCode()
    {
        return key.hashCode();
    }


    /** Description of the Method */
    public String toString()
    {
        return "[cacheName=" + cacheName + ", key=" + key + ", val=" + val + ", attr = " + attr + "]";
    }

}
// end class


