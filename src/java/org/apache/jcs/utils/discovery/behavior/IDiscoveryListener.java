package org.apache.jcs.utils.discovery.behavior;

import org.apache.jcs.utils.discovery.DiscoveredService;

/**
 * Interface for things that want to listen to discovery events. This will allow discovery to be
 * used outside of the TCP lateral.
 */
public interface IDiscoveryListener
{
    /**
     * Add the service if needed. This does not necessarily mean that the service is not already
     * added. This can be called if there is a change.
     * <p>
     * @param service the service to add
     */
    public void addDiscoveredService( DiscoveredService service );

    /**
     * Remove the service from the list.
     * <p>
     * @param service the service to remove
     */
    public void removeDiscoveredService( DiscoveredService service );
}
