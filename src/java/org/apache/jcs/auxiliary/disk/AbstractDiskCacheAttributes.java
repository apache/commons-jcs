package org.apache.jcs.auxiliary.disk;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.jcs.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes;

/**
 * This has common attributes that any conceivable disk cache would need.
 * 
 * @author aaronsm
 *  
 */
public abstract class AbstractDiskCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements IDiskCacheAttributes
{

    /** path to disk */
    protected String diskPath;

    /** default to 5000 */
    protected int maxPurgatorySize = MAX_PURGATORY_SIZE_DEFUALT;

    private static final int DEFAULT_shutdownSpoolTimeLimit = 60;
    
    protected int shutdownSpoolTimeLimit = DEFAULT_shutdownSpoolTimeLimit;
        
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#setDiskPath(java.lang.String)
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path.trim();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#getDiskPath()
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#getMaxPurgatorySize()
     */
    public int getMaxPurgatorySize()
    {
        return maxPurgatorySize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#setMaxPurgatorySize(int)
     */
    public void setMaxPurgatorySize( int maxPurgatorySize )
    {
        this.maxPurgatorySize = maxPurgatorySize;
    }

    /* (non-Javadoc)
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#getShutdownSpoolTimeLimit()
     */
    public int getShutdownSpoolTimeLimit()
    {
        return this.shutdownSpoolTimeLimit;
    }

    /* (non-Javadoc)
     * @see org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes#setShutdownSpoolTimeLimit(int)
     */
    public void setShutdownSpoolTimeLimit( int shutdownSpoolTimeLimit )
    {
        this.shutdownSpoolTimeLimit = shutdownSpoolTimeLimit;
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
            return (AuxiliaryCacheAttributes) this.clone();
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
        str.append( "AbstractDiskCacheAttributes " );
        str.append( "\n diskPath = " + diskPath );
        str.append( "\n maxPurgatorySize   = " + maxPurgatorySize );
        return str.toString();
    }

}