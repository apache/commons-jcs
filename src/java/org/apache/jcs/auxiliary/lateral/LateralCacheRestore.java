package org.apache.jcs.auxiliary.lateral;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheManager;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.engine.behavior.ICacheRestore;

/**
 * Used to repair the lateral caches managed by the associated instance of
 * LateralCacheManager.
 *
 */
public class LateralCacheRestore
    implements ICacheRestore
{
    private final static Log log = LogFactory.getLog( LateralCacheRestore.class );

    private final ILateralCacheManager lcm;

    private boolean canFix = true;

    private Object lateralObj;

    /**
     * Constructs with the given instance of LateralCacheManager.
     *
     * @param lcm
     */
    public LateralCacheRestore( ILateralCacheManager lcm )
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
            lateralObj = lcm.fixService();
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
