package org.apache.jcs.auxiliary.remote;

/*
 * This won't work when registered to mulitple remotes, will use a variable int he rcm
 */
/**
 * A shared static variable holder for the remote cache
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheInfo
{

    // shouldn't be instantiated
    /** Constructor for the RemoteCacheInfo object */
    private RemoteCacheInfo() { }


    /**
     * Shouldn't be used till after reconneting, after setting = thread safe
     * Used to identify a client, so we can run multiple clients off one host.
     * Need since there is no way to identify a client other than by host in
     * rmi.
     */
    protected static byte listenerId = 0;

}

