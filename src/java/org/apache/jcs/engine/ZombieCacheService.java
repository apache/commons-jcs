
package org.apache.jcs.engine;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ZombieCacheService implements ICacheService, IZombie
{

    /** Description of the Method */
    public void put( ICacheElement item ) { }


    /** Description of the Method */
    public void update( ICacheElement item ) { }


    /** Description of the Method */
    public Serializable get( String cacheName, Serializable key )
    {
        return null;
    }


    /** Description of the Method */
    public Serializable get( String cacheName, Serializable key, boolean container )
    {
        return null;
    }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key ) { }


    /** Description of the Method */
    public void removeAll( String cacheName ) { }


    /** Description of the Method */
    public void dispose( String cacheName ) { }


    /** Description of the Method */
    public void release() { }

}
