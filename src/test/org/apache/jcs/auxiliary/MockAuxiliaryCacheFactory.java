package org.apache.jcs.auxiliary;

import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/** For testing */
public class MockAuxiliaryCacheFactory
    implements AuxiliaryCacheFactory
{
    /** the name of the aux */
    public String name = "MockAuxiliaryCacheFactory";

    /**
     * Creates a mock aux.
     * <p>
     * @param attr
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return AuxiliaryCache
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes attr, ICompositeCacheManager cacheMgr,
                                       ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        MockAuxiliaryCache auxCache = new MockAuxiliaryCache();
        auxCache.setCacheEventLogger( cacheEventLogger );
        auxCache.setElementSerializer( elementSerializer );
        return auxCache;
    }

    /**
     * @return String
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param s
     */
    public void setName( String s )
    {
        this.name = s;
    }
}
