package org.apache.jcs.auxiliary.lateral.javagroups.behavior;

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


import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

/**
 * Description of the Interface
 */
public interface ILateralCacheJGAttributes extends ILateralCacheAttributes
{

    /**
     * Gets the {3} attribute of the ILateralCacheJGAttributes object
     *
     * @return The {3} value
     */
    public String getDistProps();

    /**
     * Sets the {3} attribute of the ILateralCacheJGAttributes object
     *
     * @param props The new {3} value
     */
    public void setDistProps( String props );

    /**
     * Gets the {3} attribute of the ILateralCacheJGAttributes object
     *
     * @return The {3} value
     */
    public String getRpcProps();

    /**
     * Sets the {3} attribute of the ILateralCacheJGAttributes object
     *
     * @param props The new {3} value
     */
    public void setRpcProps( String props );

}
