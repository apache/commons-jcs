package org.apache.commons.jcs.auxiliary;

import java.io.Serializable;

import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

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
    public <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V>
        createCache( AuxiliaryCacheAttributes attr, ICompositeCacheManager cacheMgr,
           ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        MockAuxiliaryCache<K, V> auxCache = new MockAuxiliaryCache<K, V>();
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
