package org.apache.jcs.auxiliary.lateral.behavior;

import java.io.IOException;
import java.util.Map;

import org.apache.jcs.auxiliary.AuxiliaryCacheManager;

/**
 * This helps ensure some common behvior among LateraLCacheManagers
 * for things such as montiors.
 * 
 * @author Aaron Smuts
 *
 */
public interface ILateralCacheManager extends AuxiliaryCacheManager
{

    /**
     * This is a temporary solution that allos the monitor to get the instances of a manager. 
     * 
     * @return
     */
    public abstract Map getInstances();
    
    
    /**
     * This is a temporary solution that allos the monitor to get caches from an instance of a manager. 
     * 
     * @return
     */
    public abstract Map getCaches();
    
    
    /**
     * The restore calls thsi on the manger if a cache if found to be in error.
     * 
     * @return Object is the service if it can be fixed.
     * @throws IOException if the service cannot be fixed.
     */
    public abstract Object fixService() throws IOException;

    /**
     * Sets the corected service.  The restore process will call this
     * if it gets a good service back from fixService.
     * 
     * @param lateralService
     * @param lateralWatch
     */
    public void fixCaches( ILateralCacheService lateralService, ILateralCacheObserver lateralWatch );
    
}
