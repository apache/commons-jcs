
package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

/**
 * Interface for a cache event queue. An event queue is used to propagate
 * ordered cache events to one and only one target listener.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheEventQueue
{

    /**
     * Adds a feature to the PutEvent attribute of the ICacheEventQueue object
     *
     * @param ce The feature to be added to the PutEvent attribute
     */
    public void addPutEvent( ICacheElement ce )
        throws IOException;


    /**
     * Adds a feature to the RemoveEvent attribute of the ICacheEventQueue
     * object
     *
     * @param key The feature to be added to the RemoveEvent attribute
     */
    public void addRemoveEvent( Serializable key )
        throws IOException;


    /**
     * Adds a feature to the RemoveAllEvent attribute of the ICacheEventQueue
     * object
     */
    public void addRemoveAllEvent()
        throws IOException;


    /**
     * Adds a feature to the DisposeEvent attribute of the ICacheEventQueue
     * object
     */
    public void addDisposeEvent()
        throws IOException;


    /**
     * Gets the listenerId attribute of the ICacheEventQueue object
     *
     * @return The listenerId value
     */
    public byte getListenerId();


    /** Description of the Method */
    public void destroy();


    /**
     * Gets the alive attribute of the ICacheEventQueue object
     *
     * @return The alive value
     */
    public boolean isAlive();

}

