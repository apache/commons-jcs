package org.apache.jcs.auxiliary.lateral.behavior;

import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * Listens for lateral cache event notification.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ILateralCacheListener extends ICacheListener
{

    /** Description of the Method */
    public void init();

}
