package org.apache.jcs.auxiliary.remote.behavior;

import java.io.IOException;

import java.rmi.Remote;

import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * Listens for remote cache event notification ( rmi callback ).
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheListener extends ICacheListener, Remote
{

    /** Description of the Field */
    public final static int SERVER_LISTENER = 0;
    /** Description of the Field */
    public final static int CLIENT_LISTENER = 0;


    /**
     * Gets the remoteType attribute of the IRemoteCacheListener object
     *
     * @return The remoteType value
     */
    public int getRemoteType()
        throws IOException;

}
