package org.apache.jcs.auxiliary.disk.indexed;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Configuration class for the Indexed Disk Cache
 *
 */
public class IndexedDiskCacheAttributes implements AuxiliaryCacheAttributes
{

    private String cacheName;
    private String name;

    private String diskPath;

    // default to 5000
    private int maxKeySize = 5000;

    // default to -1, i.e., don't optimize until shutdown
    private int optimizeAtRemoveCount = -1;

    /**
     * Constructor for the DiskCacheAttributes object
     */
    public IndexedDiskCacheAttributes()
    {
    }


    /**
     * Sets the diskPath attribute of theputm 2000 DiskCacheAttributes object
     *
     * @param path The new diskPath value
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path.trim();
    }


    /**
     * Gets the diskPath attribute of the DiskCacheAttributes object
     *
     * @return The diskPath value
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }


    /**
     * Sets the cacheName attribute of the DiskCacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the DiskCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Gets the name attribute of the DiskCacheAttributes object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the DiskCacheAttributes object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Gets the maxKeySize attribute of the DiskCacheAttributes object
     *
     * @return The maxKeySize value
     */
    public int getMaxKeySize()
    {
        return this.maxKeySize;
    }


    /**
     * Sets the maxKeySize attribute of the DiskCacheAttributes object
     *
     * @param name The new maxKeySize value
     */
    public void setMaxKeySize( int maxKeySize )
    {
        this.maxKeySize = maxKeySize;
    }

    /**
     * Gets the optimizeAtRemoveCount attribute of the DiskCacheAttributes object
     *
     * @return The optimizeAtRemoveCount value
     */
    public int getOptimizeAtRemoveCount()
    {
        return this.optimizeAtRemoveCount;
    }


    /**
     * Sets the optimizeAtRemoveCount attribute of the DiskCacheAttributes object
     * This number determines how often the disk cache should run real time
     * optimizations.
     *
     * @param name The new optimizeAtRemoveCount value
     */
    public void setOptimizeAtRemoveCount( int cnt)
    {
        this.optimizeAtRemoveCount = cnt;
    }


    /**
     * Description of the Method
     *
     * @return
     */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return ( AuxiliaryCacheAttributes ) this.clone();
        }
        catch ( Exception e )
        {
        }
        return this;
    }


    /**
     * Description of the Method
     *
     * @return
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "diskPath = " + diskPath );
        return str.toString();
    }

}
