package org.apache.jcs.auxiliary.disk.behavior;

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
 * Common disk cache attributes.
 *  
 */
public interface IDiskCacheAttributes extends AuxiliaryCacheAttributes
{
  
  public static final int MAX_PURGATORY_SIZE_DEFUALT = 5000;

  /**
   * Sets the diskPath attribute of the IJISPCacheAttributes object
   * 
   * @param path
   *          The new diskPath value
   */
  public void setDiskPath( String path );

  /**
   * Gets the diskPath attribute of the IJISPCacheAttributes object
   * 
   * @return The diskPath value
   */
  public String getDiskPath();


  
  /**
   * Gets the maxKeySize attribute of the DiskCacheAttributes object
   *
   * @return The maxPurgatorySize value
   */
  public int getMaxPurgatorySize();


  /**
   * Sets the maxPurgatorySize attribute of the DiskCacheAttributes object
   *
   * @param name The new maxPurgatorySize value
   */
  public void setMaxPurgatorySize( int maxPurgatorySize );

}
//   end interface
