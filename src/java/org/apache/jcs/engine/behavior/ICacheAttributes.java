package org.apache.jcs.engine.behavior;

import java.io.Serializable;

import java.util.ArrayList;

/**
 * This interface allows programatic control of a cache. Caches described in the
 * cache.properties file will be created with ICacheAttributes that correspond
 * to the props entires.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheAttributes extends Serializable, Cloneable
{

    // WHETHER ELEMENT CAN USE AUXILLIARY CACHES

    /**
     * SetLocal sets the attribute to indicate the cache is local. Invalidations
     * and updates will not be propagated to other caches in the system.
     *
     * @param isLocal The new isLocal value
     */
    public void setIsLocal( boolean isLocal );


    /**
     * Gets the isLocal attribute of the ICacheAttributes object
     *
     * @return The isLocal value
     */
    public boolean getIsLocal();


    /**
     * Sets the useDisk attribute of the ICacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk );


    /**
     * Gets the useDisk attribute of the ICacheAttributes object
     *
     * @return The useDisk value
     */
    public boolean getUseDisk();


    // REMOVE
    /**
     * Sets the dist attribute of the ICacheAttributes object
     *
     * @param dist The new dist value
     */
    public void setDist( boolean dist );


    /**
     * Gets the dist attribute of the ICacheAttributes object
     *
     * @return The dist value
     */
    public boolean getDist();


    /**
     * set whether the cache should use a lateral cache
     *
     * @param d The new useLateral value
     */
    public void setUseLateral( boolean d );


    /**
     * Gets the useLateral attribute of the ICacheAttributes object
     *
     * @return The useLateral value
     */
    public boolean getUseLateral();


    /**
     * Sets whether the cache is remote enabled
     *
     * @param isRemote The new useRemote value
     */
    public void setUseRemote( boolean isRemote );


    /**
     * returns whether the cache is remote enabled
     *
     * @return The useRemote value
     */
    public boolean getUseRemote();


    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s );


    /**
     * Gets the cacheName attribute of the ICacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    // ALL POSSIBLE AUXIALLARY CONFIGURATION
    // this might become too much if we continue adding attribute
    // may need specific attributes.  Say the alteral cache doesn't have a servlet,
    // then why should we have a deleteServlet property.  Also, why
    // should the remote get an attribute set so large.
    // The problem with breaking the attributes up is that
    // it would become impossible to programatically create a cache.
    // perhaps it is worth it.  If we broke it up then mor proerties could be
    // added reflectively.  You could set whatever your particular
    // lateral cache needs.  The ICacheAttributes could then be distilled
    // into general properties and each manager would ahve some other
    // atribute object that could e set dynamically.
    // the problem with the static getInstance methods may necesitate doing this.

    /**
     * manages the remote serice name for the remotecache
     *
     * @param s The new remoteServiceName value
     */
    public void setRemoteServiceName( String s );


    /**
     * Gets the remoteServiceName attribute of the ICacheAttributes object
     *
     * @return The remoteServiceName value
     */
    public String getRemoteServiceName();


    /**
     * Sets whether the cache is remote enabled
     *
     * @param seq The new accessSeq value
     */
    public void setAccessSeq( String seq );


    /**
     * Gets the accessSeq attribute of the ICacheAttributes object
     *
     * @return The accessSeq value
     */
    public String getAccessSeq();


    /**
     * SetMaxObjects is used to set the attribute to determine the maximum
     * number of objects allowed in the memory cache. If the max number of
     * objects or the cache size is set, the default for the one not set is
     * ignored. If both are set, both are used to determine the capacity of the
     * cache, i.e., object will be removed from the cache if either limit is
     * reached.
     *
     * @param size The new maxObjects value
     */
    public void setMaxObjects( int size );


    /**
     * Gets the maxObjects attribute of the ICacheAttributes object
     *
     * @return The maxObjects value
     */
    public int getMaxObjects();


    /**
     * SetMemoryCacheSize sets the attribute to indicate the maximum size of the
     * memory cache. Size is in megabytes. If the max number of objects or the
     * cache size is set, the default for the one not set is ignored. If both
     * are set, both are used to determine the capacity of the cache, i.e.,
     * object will be removed from the cache if either limit is reached.
     *
     * @param size The new maxBytes value
     */
    public void setMaxBytes( int size );


    /**
     * Gets the maxBytes attribute of the ICacheAttributes object
     *
     * @return The maxBytes value
     */
    public int getMaxBytes();


    /**
     * SetDiskCacheSize sets the attribute to indicate the maximum size of the
     * disk cache. Size is in megabytes.
     *
     * @param size The new diskCacheSize value
     */
    public void setDiskCacheSize( int size );


    /**
     * SetDiskPath sets the attribute indicating the root location for the disk
     * cache.
     *
     * @param path The new diskPath value
     */
    public void setDiskPath( String path );


    /**
     * Gets the diskPath attribute of the ICacheAttributes object
     *
     * @return The diskPath value
     */
    public String getDiskPath();


    /**
     * SetCleanInterval sets the attribute indicating the how often the cache
     * should be checked for objects invalidated by ?time to live? or ?idle
     * time? attributes.
     *
     * @param seconds The new cleanInterval value
     */
    public void setCleanInterval( int seconds );


    /**
     * AddCacheAddr is used to specify the network address and port to be used
     * by the cache messaging system. At least one known address is required by
     * the cache to allow discovery when a process using the cache is first
     * brought on line. If no address is specified, localhost with a default
     * port is use. If the system of caches is across multiple nodes, it is best
     * to have an address specified for each node to protect against unavailable
     * nodes.
     *
     * @param ipAddr The feature to be added to the LateralCacheAddr attribute
     * @param port The feature to be added to the LateralCacheAddr attribute
     */
    public void addLateralCacheAddr( String ipAddr, int port );


    /**
     * Sets the lateralDeleteServlet attribute of the ICacheAttributes object
     *
     * @param name The new lateralDeleteServlet value
     */
    public void setLateralDeleteServlet( String name );


    /**
     * Gets the lateralDeleteServlet attribute of the ICacheAttributes object
     *
     * @return The lateralDeleteServlet value
     */
    public String getLateralDeleteServlet();


    /**
     * Sets the lateralReceiveServlet attribute of the ICacheAttributes object
     *
     * @param name The new lateralReceiveServlet value
     */
    public void setLateralReceiveServlet( String name );


    /**
     * Gets the lateralReceiveServlet attribute of the ICacheAttributes object
     *
     * @return The lateralReceiveServlet value
     */
    public String getLateralReceiveServlet();


    /**
     * Sets the lateralCacheAddrs attribute of the ICacheAttributes object
     *
     * @param addrs The new lateralCacheAddrs value
     */
    public void setLateralCacheAddrs( ArrayList addrs );


    /**
     * Returns an ArrayList of Strings representing the address for all the
     * cache address configured. If no address were configured the default value
     * is returned. The address is in the form of ipaddress:port
     * (127.0.0.1:12345)
     *
     * @return The lateralCacheAddrs value
     */
    public ArrayList getLateralCacheAddrs();


    /**
     * Sets the remote cache address
     *
     * @param host The new remoteHost value
     */
    public void setRemoteHost( String host );


    /**
     * return the remote cache address
     *
     * @return The remoteHost value
     */
    public String getRemoteHost();


    /**
     * Sets the remote cache port
     *
     * @param port The new remotePort value
     */
    public void setRemotePort( int port );


    /**
     * returns the port of the remote cahe
     *
     * @return The remotePort value
     */
    public int getRemotePort();


    // add the secondary remote configuration information
    /**
     * Sets the remote cache address
     *
     * @param host The new secondaryRemoteHost value
     */
    public void setSecondaryRemoteHost( String host );


    /**
     * return the remote cache address
     *
     * @return The secondaryRemoteHost value
     */
    public String getSecondaryRemoteHost();


    /**
     * Sets the remote cache port
     *
     * @param port The new secondaryRemotePort value
     */
    public void setSecondaryRemotePort( int port );


    /**
     * returns the port of the remote cahe
     *
     * @return The secondaryRemotePort value
     */
    public int getSecondaryRemotePort();


    // soultion to interface cloning
    /** Description of the Method */
    public ICacheAttributes copy();

}
