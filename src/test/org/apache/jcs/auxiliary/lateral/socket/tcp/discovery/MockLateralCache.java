package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.auxiliary.lateral.LateralCache;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * For testing things that need a lateral cache
 * 
 * @author Aaron Smuts
 *  
 */
public class MockLateralCache
    extends LateralCache
    implements ICache
{

    /**
     * @param cattr
     */
    protected MockLateralCache( ILateralCacheAttributes cattr )
    {
        super( cattr );
    }

    // generalize this, use another interface
    private ILateralCacheAttributes cattr;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#get(java.io.Serializable)
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#remove(java.io.Serializable)
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#removeAll()
     */
    public void removeAll()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#dispose()
     */
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#getSize()
     */
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#getStatus()
     */
    public int getStatus()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#getStats()
     */
    public String getStats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICache#getCacheName()
     */
    public String getCacheName()
    {
        return super.getCacheName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheType#getCacheType()
     */
    public int getCacheType()
    {
        return super.getCacheType();
    }

}
