
package org.apache.jcs.engine.behavior;

import java.io.IOException;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheServiceAdmin
{
    /**
     * Gets the stats attribute of the ICacheServiceAdmin object
     *
     * @return The stats value
     */
    public String getStats()
        throws IOException;


    /** Description of the Method */
    public void shutdown()
        throws IOException;


    /** Description of the Method */
    public void shutdown( String host, int port )
        throws IOException;
}
