/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp.behavior;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IJISPCacheAttributes extends IAuxiliaryCacheAttributes
{

    /**
     * Sets the diskPath attribute of the IJISPCacheAttributes object
     *
     * @param path The new diskPath value
     */
    public void setDiskPath( String path );


    /**
     * Gets the diskPath attribute of the IJISPCacheAttributes object
     *
     * @return The diskPath value
     */
    public String getDiskPath();


    // whether the disk cache should clear the old files
    // so there are no lingering elements.
    /**
     * Sets the clearOnStart attribute of the IJISPCacheAttributes object
     *
     * @param clear The new clearOnStart value
     */
    public void setClearOnStart( boolean clear );


    /**
     * Gets the clearOnStart attribute of the IJISPCacheAttributes object
     *
     * @return The clearOnStart value
     */
    public boolean getClearOnStart();

}
// end interface
