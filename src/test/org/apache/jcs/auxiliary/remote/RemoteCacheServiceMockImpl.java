package org.apache.jcs.auxiliary.remote;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Set;

import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This is a mock impl of the remote cache service.
 * <p>
 * @author admin
 */
public class RemoteCacheServiceMockImpl
    implements IRemoteCacheService
{
    /** The object that was last passed to update. */
    public Object lastUpdate;

    /** The key that was last passed to remove. */
    public Object lastRemoveKey;

    /**
     * The cache name that was last passed to removeAll.
     */
    public String lastRemoveAllCacheName;

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#get(java.lang.String,
     *      java.io.Serializable, long)
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#getGroupKeys(java.lang.String,
     *      java.lang.String)
     */
    public Set getGroupKeys( String cacheName, String groupName )
        throws RemoteException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#remove(java.lang.String,
     *      java.io.Serializable, long)
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        lastRemoveKey = key;
    }

    /**
     * Set the lastRemoveAllCacheName to the cacheName.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#removeAll(java.lang.String,
     *      long)
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        lastRemoveAllCacheName = cacheName;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#update(org.apache.jcs.engine.behavior.ICacheElement,
     *      long)
     */
    public void update( ICacheElement item, long requesterId )
        throws ObjectExistsException, IOException
    {
        lastUpdate = item;
    }

    public void dispose( String cacheName )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#get(java.lang.String, java.io.Serializable)
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws ObjectNotFoundException, IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#release()
     */
    public void release()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#remove(java.lang.String,
     *      java.io.Serializable)
     */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        lastRemoveKey = key;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#removeAll(java.lang.String)
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        lastRemoveAllCacheName = cacheName;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement item )
        throws ObjectExistsException, IOException
    {
        lastUpdate = item;
    }

}
