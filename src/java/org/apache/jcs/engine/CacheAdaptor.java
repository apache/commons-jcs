package org.apache.jcs.engine;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used for Cache-to-Cache messaging purposes.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheAdaptor implements ICacheListener
{
    private final static Log log = LogFactory.getLog( CacheAdaptor.class );

    private final ICache cache;

    /** Description of the Field */
    protected byte listenerId = 0;


    /**
     * Sets the listenerId attribute of the CacheAdaptor object
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException
    {
        this.listenerId = id;
        log.debug( "listenerId = " + id );
    }


    /**
     * Gets the listenerId attribute of the CacheAdaptor object
     *
     * @return The listenerId value
     */
    public byte getListenerId()
        throws IOException
    {
        return this.listenerId;
    }


    /**
     * Constructor for the CacheAdaptor object
     *
     * @param cache
     */
    public CacheAdaptor( ICache cache )
    {
        this.cache = cache;
    }


    /** Description of the Method */
    public void handlePut( ICacheElement item )
        throws IOException
    {
        try
        {
            //cache.put(item.getKey(), item.getVal());
            //cache.update( (CacheElement)item );// .put(item.getKey(), item.getVal());
            cache.update( item );
        }
        catch ( Exception e )
        {

        }
    }


    /** Description of the Method */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        cache.remove( key );
    }


    /** Description of the Method */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        cache.removeAll();
    }


    /** Description of the Method */
    public void handleDispose( String cacheName )
        throws IOException
    {
        cache.dispose();
    }
}
