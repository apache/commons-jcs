package org.apache.jcs.engine.behavior;

import org.apache.jcs.access.exception.InvalidArgumentException;

/**
 * Inteface for cache element attributes classes.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IElementAttributes
{

    /**
     * Sets the version attribute of the IAttributes object
     *
     * @param version The new version value
     */
    public void setVersion( long version );


    /**
     * Sets the maxLife attribute of the IAttributes object
     *
     * @param ttl The new timeToLive value
     * @param mls The new {3} value
     */
    public void setMaxLifeSeconds( long mls );

    /**
     * Sets the maxLife attribute of the IAttributes object
     *
     * @param ttl The new timeToLive value
     * @return The {3} value
     */
    public long getMaxLifeSeconds();


    /**
     * Sets the idleTime attribute of the IAttributes object
     *
     * @param idle The new idleTime value
     */
    public void setIdleTime( long idle );


    //public void setListener( int event, CacheEventListener listerner) {}

    /**
     * Size in bytes.
     *
     * @param size The new size value
     */
    public void setSize( int size );


    /**
     * Gets the size attribute of the IAttributes object
     *
     * @return The size value
     */
    public int getSize();


    /**
     * Gets the createTime attribute of the IAttributes object
     *
     * @return The createTime value
     */
    public long getCreateTime();


    /**
     * Gets the LastAccess attribute of the IAttributes object
     *
     * @return The LastAccess value
     */
    public long getLastAccessTime();

    /**
     * Sets the LastAccessTime as now of the IElementAttributes object
     */
    public void setLastAccessTimeNow();


    /**
     * Gets the version attribute of the IAttributes object
     *
     * @return The version value
     */
    public long getVersion();


    /**
     * Gets the idleTime attribute of the IAttributes object
     *
     * @return The idleTime value
     */
    public long getIdleTime();


    /**
     * Gets the time left to live of the IAttributes object
     *
     * @return The t value
     */
    public long getTimeToLiveSeconds();

    /**
     * Returns a copy of the object.
     *
     * @return IElementAttributes
     */
    public IElementAttributes copy();


    /**
     * Gets the {3} attribute of the IElementAttributes object
     *
     * @return The {3} value
     */
    public boolean getIsDistribute();
    public void setIsDistribute( boolean val );
    // lateral

    /**
     * can this item be flushed to disk
     *
     * @return The {3} value
     */
    public boolean getIsSpool();
    public void setIsSpool(boolean val);

    /**
     * Is this item laterally distributable
     *
     * @return The {3} value
     */
    public boolean getIsLateral();
    public void setIsLateral( boolean val);

    /**
     * Can this item be sent to the remote cache
     *
     * @return The {3} value
     */
    public boolean getIsRemote();
    public void setIsRemote( boolean val);

    /**
     * can turn off expiration
     *
     * @return The {3} value
     */
    public boolean getIsEternal();
    public void setIsEternal( boolean val);
}
