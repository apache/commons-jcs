package org.apache.commons.jcs4.engine.control.event;

import org.apache.commons.jcs4.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs4.engine.control.event.behavior.IElementEvent;

/**
 * Element events will trigger the creation of Element Event objects. This is a wrapper around the
 * cache element that indicates the event triggered.
 */
public record ElementEvent<T>(
        /** Event source */
        T source,

        /** Default event code */
        ElementEventType elementEventType
) implements IElementEvent<T>
{
    /** Don't change */
    private static final long serialVersionUID = -5364117411457467056L;
}
