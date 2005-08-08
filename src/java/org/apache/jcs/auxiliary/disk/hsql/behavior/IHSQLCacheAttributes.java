package org.apache.jcs.auxiliary.disk.hsql.behavior;

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
 * Title: Description:
 * 
 * @version 1.0
 */

public interface IHSQLCacheAttributes
    extends AuxiliaryCacheAttributes
{

    /**
     * Sets the diskPath attribute of the IHSQLCacheAttributes object
     * 
     * @param path
     *            The new diskPath value
     */
    public void setDiskPath( String path );

    /**
     * Gets the diskPath attribute of the IHSQLCacheAttributes object
     * 
     * @return The diskPath value
     */
    public String getDiskPath();

}
// end interface
