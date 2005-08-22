package org.apache.jcs.engine.control;

import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 */
public class CompositeCacheManagerMockImpl
    implements ICompositeCacheManager
{

    private CompositeCache cache;
    
    /* (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICompositeCacheManager#getCache(java.lang.String)
     */
    public CompositeCache getCache( String cacheName )
    {
        if ( cache == null )
        {
            System.out.println( "Creating mock cache" );
            CompositeCache newCache = new CompositeCache( cacheName, new CompositeCacheAttributes(), new ElementAttributes() );
            this.setCache( newCache );
        }
        return cache;
    }

    /**
     * @param cache The cache to set.
     */
    public void setCache( CompositeCache cache )
    {
        this.cache = cache;
    }

    /**
     * @return Returns the cache.
     */
    public CompositeCache getCache()
    {
        return cache;
    }

}
