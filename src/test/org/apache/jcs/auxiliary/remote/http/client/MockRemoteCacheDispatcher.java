package org.apache.jcs.auxiliary.remote.http.client;

import java.io.IOException;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;

/** For testing the service. */
public class MockRemoteCacheDispatcher
    implements IRemoteCacheDispatcher
{
    /** The last request passes to dispatch */
    public RemoteCacheRequest lastRemoteCacheRequest;
    
    /** The response setup */
    public RemoteCacheResponse setupRemoteCacheResponse;
    
    /** Records the last and returns setupRemoteCacheResponse. 
     * <p>
     * @param remoteCacheRequest 
     * @return RemoteCacheResponse
     * @throws IOException */
    public RemoteCacheResponse dispatchRequest( RemoteCacheRequest remoteCacheRequest )
        throws IOException
    {
        this.lastRemoteCacheRequest = remoteCacheRequest;
        return setupRemoteCacheResponse;
    }
}
