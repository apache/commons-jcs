package org.apache.jcs.auxiliary.remote.group;

import org.apache.jcs.auxiliary.remote.RemoteCache;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteGroupCache extends RemoteCache
{

    /**
     * Constructor for the RemoteGroupCache object
     *
     * @param irca
     * @param remote
     */
    protected RemoteGroupCache( IRemoteCacheAttributes irca, IRemoteCacheService remote )
    {
        super( irca, remote );
        //p( "constructing remote group cache" );
    }

}
