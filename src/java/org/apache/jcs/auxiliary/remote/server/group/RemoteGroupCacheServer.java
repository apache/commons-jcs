package org.apache.jcs.auxiliary.remote.server.group;

import java.io.IOException;

import java.rmi.NotBoundException;
import java.rmi.registry.Registry;

import org.apache.jcs.auxiliary.remote.server.RemoteCacheServer;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManager;
import org.apache.jcs.engine.control.group.GroupCacheManagerFactory;
import org.apache.jcs.engine.control.group.GroupCacheManagerFactory;
import org.apache.jcs.engine.control.group.GroupCacheManagerFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheServiceAdmin;
import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;

/**
 * Provides session remote cache services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteGroupCacheServer extends RemoteCacheServer
{
    /**
     * Constructor for the RemoteCacheServer object
     *
     * @param rcsa
     * @exception IOException
     * @exception NotBoundException
     */
    protected RemoteGroupCacheServer( IRemoteCacheServerAttributes rcsa )
        throws IOException, NotBoundException
    {
        super( rcsa );
    }


    /** Description of the Method */
    protected CompositeCacheManager createCacheManager( String prop )
    {
        return GroupCacheManagerFactory.getInstance( prop == null ? "/remote.cache.ccf" : prop );
    }

    /////////////////////// Implements the ICacheServiceAdmin interface. //////////////////

    /** Description of the Method */
    public void shutdown()
        throws IOException
    {
        RemoteGroupCacheServerFactory.shutdownImpl( "", Registry.REGISTRY_PORT );
    }


    /** Description of the Method */
    public void shutdown( String host, int port )
        throws IOException
    {
        RemoteGroupCacheServerFactory.shutdownImpl( host, port );
    }
}
