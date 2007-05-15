package org.apache.jcs.auxiliary.disk;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.jcs.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes;

/**
 * This has common attributes that any conceivable disk cache would need.
 * <p>
 * @author aaronsm
 */
public abstract class AbstractDiskCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements IDiskCacheAttributes
{
    /** path to disk */
    protected String diskPath;

    /** if this is false, we will not execute remove all */
    private boolean allowRemoveAll = true;

    /** default to 5000 */
    protected int maxPurgatorySize = MAX_PURGATORY_SIZE_DEFUALT;

    /** Default amount of time to allow for keypersistence on shutdown */
    private static final int DEFAULT_shutdownSpoolTimeLimit = 60;

    /**
     * This default determines how long the shutdown will wait for the key spool and data defrag to
     * finish.
     */
    protected int shutdownSpoolTimeLimit = DEFAULT_shutdownSpoolTimeLimit;

    /**
     * Sets the diskPath attribute of the IJISPCacheAttributes object
     * <p>
     * @param path The new diskPath value
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path.trim();
    }

    /**
     * Gets the diskPath attribute of the attributes object
     * <p>
     * @return The diskPath value
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }

    /**
     * Gets the maxKeySize attribute of the DiskCacheAttributes object
     * <p>
     * @return The maxPurgatorySize value
     */
    public int getMaxPurgatorySize()
    {
        return maxPurgatorySize;
    }

    /**
     * Sets the maxPurgatorySize attribute of the DiskCacheAttributes object
     * <p>
     * @param maxPurgatorySize The new maxPurgatorySize value
     */
    public void setMaxPurgatorySize( int maxPurgatorySize )
    {
        this.maxPurgatorySize = maxPurgatorySize;
    }

    /**
     * Get the amount of time in seconds we will wait for elements to move to disk during shutdown
     * for a particular region.
     * <p>
     * @return the time in seconds.
     */
    public int getShutdownSpoolTimeLimit()
    {
        return this.shutdownSpoolTimeLimit;
    }

    /**
     * Sets the amount of time in seconds we will wait for elements to move to disk during shutdown
     * for a particular region.
     * <p>
     * This is how long we give the event queue to empty.
     * <p>
     * The default is 60 seconds.
     * <p>
     * @param shutdownSpoolTimeLimit the time in seconds
     */
    public void setShutdownSpoolTimeLimit( int shutdownSpoolTimeLimit )
    {
        this.shutdownSpoolTimeLimit = shutdownSpoolTimeLimit;
    }

    /**
     * Simple clone.
     * <p>
     * @return AuxiliaryCacheAttributes
     */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return (AuxiliaryCacheAttributes) this.clone();
        }
        catch ( Exception e )
        {
            // swallow
        }
        return this;
    }

    /**
     * @param allowRemoveAll The allowRemoveAll to set.
     */
    public void setAllowRemoveAll( boolean allowRemoveAll )
    {
        this.allowRemoveAll = allowRemoveAll;
    }

    /**
     * @return Returns the allowRemoveAll.
     */
    public boolean isAllowRemoveAll()
    {
        return allowRemoveAll;
    }

    /**
     * Includes the common attributes for a debug message.
     * <p>
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "AbstractDiskCacheAttributes " );
        str.append( "\n diskPath = " + diskPath );
        str.append( "\n maxPurgatorySize   = " + maxPurgatorySize );
        str.append( "\n allowRemoveAll   = " + allowRemoveAll );
        return str.toString();
    }
}
