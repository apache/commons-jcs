/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes;

import org.apache.jcs.auxiliary.disk.jisp.behavior.IJISPCacheAttributes;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class JISPCacheAttributes implements IJISPCacheAttributes
{

    private String cacheName;
    private String name;

    private String diskPath;

    private boolean clearOnStart;


    /** Constructor for the JISPCacheAttributes object */
    public JISPCacheAttributes()
    {
        clearOnStart = false;
    }


    /**
     * Sets the diskPath attribute of the JISPCacheAttributes object
     *
     * @param path The new diskPath value
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path;
    }


    /**
     * Gets the diskPath attribute of the JISPCacheAttributes object
     *
     * @return The diskPath value
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }


    // whether the disk cache should clear the old files
    // so there are no lingering elements.
    /**
     * Sets the clearOnStart attribute of the JISPCacheAttributes object
     *
     * @param clear The new clearOnStart value
     */
    public void setClearOnStart( boolean clear )
    {
        clearOnStart = clear;
    }


    /**
     * Gets the clearOnStart attribute of the JISPCacheAttributes object
     *
     * @return The clearOnStart value
     */
    public boolean getClearOnStart()
    {
        return clearOnStart;
    }


    /**
     * Sets the cacheName attribute of the JISPCacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the JISPCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Gets the name attribute of the JISPCacheAttributes object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the JISPCacheAttributes object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /** Description of the Method */
    public IAuxiliaryCacheAttributes copy()
    {
        try
        {
            return ( IAuxiliaryCacheAttributes ) this.clone();
        }
        catch ( Exception e )
        {
        }
        return ( IAuxiliaryCacheAttributes ) this;
    }


    /** Description of the Method */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "diskPath = " + diskPath );
        return str.toString();
    }

}
