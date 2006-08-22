package org.apache.jcs.auxiliary.remote;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * For testing.
 * <p>
 * @author admin
 */
public class RemoteCacheListenerMockImpl
    implements IRemoteCacheListener
{
    /** Setup the listener id that this will return. */
    private long listenerId;

    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub
    }

    /**
     * returns the listener id, which can be setup.
     */
    public long getListenerId()
        throws IOException
    {
        return listenerId;
    }

    public String getLocalHostAddress()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRemoteType()
        throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Allows you to setup the listener id.
     */
    public void setListenerId( long id )
        throws IOException
    {
        listenerId = id;
    }

    public void handleDispose( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void handlePut( ICacheElement item )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
