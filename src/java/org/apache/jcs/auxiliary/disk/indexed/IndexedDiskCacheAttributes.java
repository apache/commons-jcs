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
import org.apache.jcs.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * Configuration class for the Indexed Disk Cache
 *
 */
public class IndexedDiskCacheAttributes extends AbstractDiskCacheAttributes
{

    private static final int DEFAULT_maxKeySize = 5000;
    private static final int DEFAULT_maxRecycleBinSize = 5000;

    /** -1 mean no limit. */
    private int maxKeySize = DEFAULT_maxKeySize;

    /** Cannot be larger than the max size.  If max is less than 0, this will be 5000 */
    private int maxRecycleBinSize = DEFAULT_maxRecycleBinSize;

    // default to -1, i.e., don't optimize until shutdown
    private int optimizeAtRemoveCount = -1;

    /**
     * Constructor for the DiskCacheAttributes object
     */
    public IndexedDiskCacheAttributes()
    {
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
     * @param maxKeySize The new maxKeySize value
     */
    public void setMaxKeySize( int maxKeySize )
    {
        this.maxKeySize = maxKeySize;

        // make sure the sizes are in accord with our rule.
        setMaxRecycleBinSize( maxRecycleBinSize );
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
     * @param cnt The new optimizeAtRemoveCount value
     */
    public void setOptimizeAtRemoveCount( int cnt)
    {
        this.optimizeAtRemoveCount = cnt;
    }


    /**
     * This cannot be larger than the maxKeySize.  It wouldn't hurt
     * anything, but it makes the config necessary.  The recycle bin
     * entry willbe at least as large as a key.
     *
     * If the maxKeySize
     * is -1 this will be set tot he default, which is 5000.
     *
     * @param maxRecycleBinSize The maxRecycleBinSize to set.
     */
    public void setMaxRecycleBinSize( int maxRecycleBinSize )
    {
      this.maxRecycleBinSize =  maxRecycleBinSize;
    }


    /**
     * @return Returns the maxRecycleBinSize.
     */
    public int getMaxRecycleBinSize()
    {
      return maxRecycleBinSize;
    }


    /**
     * Description of the Method
     *
     * @return AuxiliaryCacheAttributes
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
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "IndexedDiskCacheAttributes " );
        str.append( "\n diskPath = " + diskPath );
        str.append( "\n maxPurgatorySize   = " + maxPurgatorySize );
        str.append( "\n maxKeySize  = " + maxKeySize );
        str.append( "\n maxRecycleBinSize  = " + maxRecycleBinSize );
        str.append( "\n optimizeAtRemoveCount  = " + optimizeAtRemoveCount );
        return str.toString();
    }

}
