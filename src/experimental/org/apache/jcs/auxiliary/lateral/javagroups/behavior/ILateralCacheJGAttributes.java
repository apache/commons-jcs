package org.apache.jcs.auxiliary.lateral.javagroups.behavior;

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
