
package org.apache.jcs.engine.control.event.behavior;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;


/**
 * Interface for an element event queue. An event queue is used to propagate
 * ordered element events in one region.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IElementEventQueue
{

    /**
     * Adds an  ElementEvent  to be handled
     *
     * @param hand The IElementEventHandler
     * @param event The IElementEventHandler IElementEvent event
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException;


    /** Description of the Method */
    public void destroy();


    /**
     * Gets the alive attribute of the IElementEventQueue object
     *
     * @return The alive value
     */
    public boolean isAlive();

}

