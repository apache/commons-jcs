package org.apache.commons.jcs.auxiliary.disk;

import org.apache.commons.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/** Common disk cache methods and properties. */
public abstract class AbstractDiskCacheManager
    implements AuxiliaryCacheManager
{
    /** Don't change.     */
    private static final long serialVersionUID = 7562753543611662136L;

    /** The event logger. */
    private ICacheEventLogger cacheEventLogger;

    /** The serializer. */
    private IElementSerializer elementSerializer;

    /**
     * @param cacheEventLogger the cacheEventLogger to set
     */
    public void setCacheEventLogger( ICacheEventLogger cacheEventLogger )
    {
        this.cacheEventLogger = cacheEventLogger;
    }

    /**
     * @return the cacheEventLogger
     */
    public ICacheEventLogger getCacheEventLogger()
    {
        return cacheEventLogger;
    }

    /**
     * @param elementSerializer the elementSerializer to set
     */
    public void setElementSerializer( IElementSerializer elementSerializer )
    {
        this.elementSerializer = elementSerializer;
    }

    /**
     * @return the elementSerializer
     */
    public IElementSerializer getElementSerializer()
    {
        return elementSerializer;
    }
}
