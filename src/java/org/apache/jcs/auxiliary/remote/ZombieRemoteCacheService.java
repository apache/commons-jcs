package org.apache.jcs.auxiliary.remote;

import java.io.Serializable;
import java.util.Set;
import java.util.Collections;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.ZombieCacheService;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ZombieRemoteCacheService extends ZombieCacheService implements IRemoteCacheService
{

    /** Description of the Method */
    public void update( ICacheElement item, byte listenerId ) { }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key, byte listenerId ) { }


    /** Description of the Method */
    public void removeAll( String cacheName, byte listenerId ) { }

    public Set getGroupKeys(String cacheName, String groupName)
    {
        return Collections.EMPTY_SET;
    }
}
