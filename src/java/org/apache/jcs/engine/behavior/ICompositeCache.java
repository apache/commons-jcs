package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

// this sort of breaks the package dependency.  Should probably move
// the event packages lower
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;

/**
 * Much like the ICache interface, but this is specifically designed for the
 * CompositeCaches. This interface guarantees a simple set of methods for use by
 * auxiliary cache implementations.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICompositeCache extends ICache
{
    public ICacheElement localGet( Serializable key )
        throws IOException;

    public void localUpdate( ICacheElement ce )
        throws IOException;

    public boolean localRemove( Serializable key )
        throws IOException;

    public void localRemoveAll()
        throws IOException;

    /**
     * Adds an  ElementEvent  to be handled
     *
     * @param hand The IElementEventHandler
     * @param event The IElementEventHandler IElementEvent event
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException;
}
