package org.apache.jcs.engine.control;

import java.io.Serializable;

import java.util.ArrayList;

import org.apache.jcs.engine.behavior.ICacheAttributes;

/**
 * Most of this class should be deprecated. TODO: remove or deprecate unused
 * methods. org.apache.jcs.engine.CompositeCacheAttributes is used
 * instead. This class should probably be removed and the other repackaged.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheAttributes implements ICacheAttributes, Serializable
{

    private String cacheName;

    // need to keep a cetral configuration
    // so regions can be created programmaticall rather than only
    // during initalization
    private ArrayList lateralCacheAddrs = new ArrayList();
    private String lateralDeleteServlet;
    private String lateralReceiveServlet;

    // remove, unnecessary
    private boolean isLocal = false;
    // remove
    private boolean dist = false;

    private boolean useLateral = false;
    private boolean useRemote = false;
    private boolean useDisk = true;

    private int maxObjs = 100;
    private int maxBytes = 100000;

    // not used by the current disk cache
    private int diskSize = 1000000;
    private int cleanInterval = 1;

    private String diskPath = "c:/";

    // should use an array of the cache types or the particular
    // instances, this is too inflexible
    private String accessSeq = "disk,remote";

    private String remoteServiceName = "";
    private String remoteHost = "localhost";
    private int remotePort = 1101;

    // not used yet
    private String secondaryRemoteHost = "localhost";
    private int secondaryRemotePort = 1102;


    /** Constructor for the CacheAttributes object */
    public CacheAttributes() { }


    /**
     * Sets the isLocal attribute of the CacheAttributes object
     *
     * @param isLocal The new isLocal value
     */
    public void setIsLocal( boolean isLocal )
    {
        this.isLocal = isLocal;
    }


    /**
     * Gets the isLocal attribute of the CacheAttributes object
     *
     * @return The isLocal value
     */
    public boolean getIsLocal()
    {
        return isLocal;
    }


    /**
     * Sets the maxObjects attribute of the CacheAttributes object
     *
     * @param maxObjs The new maxObjects value
     */
    public void setMaxObjects( int maxObjs )
    {
        this.maxObjs = maxObjs;
    }


    /**
     * Gets the maxObjects attribute of the CacheAttributes object
     *
     * @return The maxObjects value
     */
    public int getMaxObjects()
    {
        return this.maxObjs;
    }


    /**
     * Sets the maxBytes attribute of the CacheAttributes object
     *
     * @param maxBytes The new maxBytes value
     */
    public void setMaxBytes( int maxBytes )
    {
        this.maxBytes = maxBytes;
    }


    /**
     * Gets the maxBytes attribute of the CacheAttributes object
     *
     * @return The maxBytes value
     */
    public int getMaxBytes()
    {
        return this.maxBytes;
    }


    /**
     * Sets the useDisk attribute of the CacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk )
    {
        this.useDisk = useDisk;
    }


    /**
     * Gets the useDisk attribute of the CacheAttributes object
     *
     * @return The useDisk value
     */
    public boolean getUseDisk()
    {
        return useDisk;
    }


    // REMOVE THESE
    /**
     * Sets the dist attribute of the CacheAttributes object
     *
     * @param dist The new dist value
     */
    public void setDist( boolean dist )
    {
        this.dist = dist;
    }


    /**
     * Gets the dist attribute of the CacheAttributes object
     *
     * @return The dist value
     */
    public boolean getDist()
    {
        return this.dist;
    }


    /**
     * Sets the useLateral attribute of the CacheAttributes object
     *
     * @param b The new useLateral value
     */
    public void setUseLateral( boolean b )
    {
        this.useLateral = b;
    }


    /**
     * Gets the useLateral attribute of the CacheAttributes object
     *
     * @return The useLateral value
     */
    public boolean getUseLateral()
    {
        return this.useLateral;
    }


    /**
     * Sets the accessSeq attribute of the CacheAttributes object
     *
     * @param seq The new accessSeq value
     */
    public void setAccessSeq( String seq )
    {
        this.accessSeq = seq;
    }


    /**
     * Gets the accessSeq attribute of the CacheAttributes object
     *
     * @return The accessSeq value
     */
    public String getAccessSeq()
    {
        return this.accessSeq;
    }


    /**
     * Sets the cacheName attribute of the CacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the CacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Sets the remoteServiceName attribute of the CacheAttributes object
     *
     * @param s The new remoteServiceName value
     */
    public void setRemoteServiceName( String s )
    {
        this.remoteServiceName = s;
    }


    /**
     * Gets the remoteServiceName attribute of the CacheAttributes object
     *
     * @return The remoteServiceName value
     */
    public String getRemoteServiceName()
    {
        return this.remoteServiceName;
    }


    /**
     * Sets the diskCacheSize attribute of the CacheAttributes object
     *
     * @param size The new diskCacheSize value
     */
    public void setDiskCacheSize( int size )
    {
        this.diskSize = size;
    }


    /**
     * Sets the diskPath attribute of the CacheAttributes object
     *
     * @param path The new diskPath value
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path;
    }


    /**
     * Gets the diskPath attribute of the CacheAttributes object
     *
     * @return The diskPath value
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }


    /**
     * Sets the cleanInterval attribute of the CacheAttributes object
     *
     * @param seconds The new cleanInterval value
     */
    public void setCleanInterval( int seconds )
    {
        this.cleanInterval = seconds;
    }


    /**
     * Adds a feature to the LateralCacheAddr attribute of the CacheAttributes
     * object
     *
     * @param ipAddr The feature to be added to the LateralCacheAddr attribute
     * @param port The feature to be added to the LateralCacheAddr attribute
     */
    public void addLateralCacheAddr( String ipAddr, int port )
    {
        this.lateralCacheAddrs.add( ipAddr + ":" + port );
    }


    /**
     * Sets the lateralCacheAddrs attribute of the CacheAttributes object
     *
     * @param addrs The new lateralCacheAddrs value
     */
    public void setLateralCacheAddrs( ArrayList addrs )
    {
        this.lateralCacheAddrs = addrs;
    }


    /**
     * Gets the lateralCacheAddrs attribute of the CacheAttributes object
     *
     * @return The lateralCacheAddrs value
     */
    public ArrayList getLateralCacheAddrs()
    {
        return this.lateralCacheAddrs;
    }


    /**
     * Sets the lateralDeleteServlet attribute of the CacheAttributes object
     *
     * @param name The new lateralDeleteServlet value
     */
    public void setLateralDeleteServlet( String name )
    {
        this.lateralDeleteServlet = name;
    }


    /**
     * Gets the lateralDeleteServlet attribute of the CacheAttributes object
     *
     * @return The lateralDeleteServlet value
     */
    public String getLateralDeleteServlet()
    {
        return lateralDeleteServlet;
    }


    /**
     * Sets the lateralReceiveServlet attribute of the CacheAttributes object
     *
     * @param name The new lateralReceiveServlet value
     */
    public void setLateralReceiveServlet( String name )
    {
        this.lateralReceiveServlet = name;
    }


    /**
     * Gets the lateralReceiveServlet attribute of the CacheAttributes object
     *
     * @return The lateralReceiveServlet value
     */
    public String getLateralReceiveServlet()
    {
        return lateralReceiveServlet;
    }


    /**
     * Sets the useRemote attribute of the CacheAttributes object
     *
     * @param useRemote The new useRemote value
     */
    public void setUseRemote( boolean useRemote )
    {
        this.useRemote = useRemote;
    }


    /**
     * Gets the useRemote attribute of the CacheAttributes object
     *
     * @return The useRemote value
     */
    public boolean getUseRemote()
    {
        return this.useRemote;
    }


    /**
     * Sets the remoteHost attribute of the CacheAttributes object
     *
     * @param host The new remoteHost value
     */
    public void setRemoteHost( String host )
    {
        this.remoteHost = host;
    }


    /**
     * Gets the remoteHost attribute of the CacheAttributes object
     *
     * @return The remoteHost value
     */
    public String getRemoteHost()
    {
        return this.remoteHost;
    }


    /**
     * Sets the remotePort attribute of the CacheAttributes object
     *
     * @param port The new remotePort value
     */
    public void setRemotePort( int port )
    {
        this.remotePort = port;
    }


    /**
     * Gets the remotePort attribute of the CacheAttributes object
     *
     * @return The remotePort value
     */
    public int getRemotePort()
    {
        return this.remotePort;
    }


    // add the secondary remote configuration information
    // TO BE IMPLEMENTED

    /**
     * Sets the secondaryRemoteHost attribute of the CacheAttributes object
     *
     * @param host The new secondaryRemoteHost value
     */
    public void setSecondaryRemoteHost( String host )
    {
        this.secondaryRemoteHost = host;
    }


    /**
     * Gets the secondaryRemoteHost attribute of the CacheAttributes object
     *
     * @return The secondaryRemoteHost value
     */
    public String getSecondaryRemoteHost()
    {
        return this.secondaryRemoteHost;
    }


    /**
     * Sets the secondaryRemotePort attribute of the CacheAttributes object
     *
     * @param port The new secondaryRemotePort value
     */
    public void setSecondaryRemotePort( int port )
    {
        this.secondaryRemotePort = port;
    }


    /**
     * Gets the secondaryRemotePort attribute of the CacheAttributes object
     *
     * @return The secondaryRemotePort value
     */
    public int getSecondaryRemotePort()
    {
        return this.secondaryRemotePort;
    }


    /** Description of the Method */
    public String toString()
    {
        StringBuffer info = new StringBuffer();
        info.append( "\n" );
        info.append( "lateralCacheAddrs = " + lateralCacheAddrs + "\n" );
        info.append( "lateralDeleteServlet = " + lateralDeleteServlet + "\n" );
        info.append( "lateralReceiveServlet = " + lateralReceiveServlet + "\n" );
        info.append( "isLocal = " + isLocal + "\n" );
        info.append( "useLateral = " + useLateral + "\n" );
        info.append( "useRemote = " + useRemote + "\n" );
        info.append( "useDisk = " + useDisk + "\n" );
        info.append( "maxObjs = " + maxObjs + "\n" );
        info.append( " = " + maxBytes + "\n" );
        info.append( " = " + diskSize + "\n" );
        info.append( " = " + cleanInterval + "\n" );
        info.append( "diskPath = " + diskPath + "\n" );
        info.append( "accessSeq = " + accessSeq + "\n" );
        info.append( "remoteHost = " + remoteHost + "\n" );
        info.append( "remotePort = " + remotePort + "\n" );
        info.append( "secondaryRemoteHost = " + secondaryRemoteHost + "\n" );
        info.append( "secondaryRemotePort = " + secondaryRemotePort + "\n" );
        return info.toString();
    }


    /** Description of the Method */
    public ICacheAttributes copy()
    {
        try
        {
            return ( CacheAttributes ) this.clone();
        }
        catch ( Exception e )
        {
            return new CacheAttributes();
        }
    }

}
