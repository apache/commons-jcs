package org.apache.jcs.auxiliary.lateral;

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
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPService;

import org.apache.jcs.engine.behavior.ICacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to repair the lateral caches managed by the associated instance of
 * LateralCacheManager.
 *  
 */
public class LateralCacheRestore
    implements ICacheRestore
{
    private final static Log log = LogFactory.getLog( LateralCacheRestore.class );

    private final LateralCacheManager lcm;

    private boolean canFix = true;

    private Object lateralObj;

    /**
     * Constructs with the given instance of LateralCacheManager.
     * 
     * @param lcm
     */
    public LateralCacheRestore( LateralCacheManager lcm )
    {
        this.lcm = lcm;
    }

    /**
     * Returns true iff the connection to the lateral host for the corresponding
     * cache manager can be successfully re-established.
     * 
     * @return whether or not the cache can be fixed.
     */
    public boolean canFix()
    {
        if ( !canFix )
        {
            return canFix;
        }

        try
        {
            if ( lcm.lca.getTransmissionType() == ILateralCacheAttributes.TCP )
            {
                lateralObj = new LateralTCPService( lcm.lca );
            }
        }
        catch ( Exception ex )
        {
            log.error( "Can't fix " + ex.getMessage() );
            canFix = false;
        }

        return canFix;
    }

    /**
     * Fixes up all the caches managed by the associated cache manager.
     */
    public void fix()
    {
        if ( !canFix )
        {
            return;
        }
        lcm.fixCaches( (ILateralCacheService) lateralObj, (ILateralCacheObserver) lateralObj );
        String msg = "Lateral connection resumed.";
        log.info( msg );
        log.debug( msg );
    }
}
