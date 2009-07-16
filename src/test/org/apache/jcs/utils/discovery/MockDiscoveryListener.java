package org.apache.jcs.utils.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.jcs.utils.discovery.behavior.IDiscoveryListener;

/** Mock listener, for testing. */
public class MockDiscoveryListener
    implements IDiscoveryListener
{
    /** discovered services. */
    public List discoveredServices = new ArrayList();

    /**
     * Adds the entry to a list. I'm not using a set. I want to see if we get dupes.
     * <p>
     * @param service
     */
    public void addDiscoveredService( DiscoveredService service )
    {
        discoveredServices.add( service );
    }

    /**
     * Removes it from the list.
     * <p>
     * @param service
     */
    public void removeDiscoveredService( DiscoveredService service )
    {
        discoveredServices.remove( service );
    }

}
