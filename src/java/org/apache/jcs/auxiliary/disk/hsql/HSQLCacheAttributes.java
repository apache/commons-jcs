package org.apache.jcs.auxiliary.disk.hsql;

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
 * Description of the Class
 *  
 */
public class HSQLCacheAttributes
    extends AbstractDiskCacheAttributes
{

    /**
     * 
     */
    private static final long serialVersionUID = -2422326369995086555L;
    private String diskPath;

    /** Constructor for the HSQLCacheAttributes object */
    public HSQLCacheAttributes()
    {
        super();
    }

    /**
     * Sets the diskPath attribute of the HSQLCacheAttributes object
     * 
     * @param path
     *            The new diskPath value
     */
    public void setDiskPath( String path )
    {
        this.diskPath = path;
    }

    /**
     * Gets the diskPath attribute of the HSQLCacheAttributes object
     * 
     * @return The diskPath value
     */
    public String getDiskPath()
    {
        return this.diskPath;
    }

    /** 
     * Clone
     * 
     * @return
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
     * For debugging only
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "diskPath = " + diskPath );
        return str.toString();
    }

}
