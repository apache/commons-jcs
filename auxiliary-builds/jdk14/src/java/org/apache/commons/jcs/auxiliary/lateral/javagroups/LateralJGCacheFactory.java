package org.apache.commons.jcs.auxiliary.lateral.javagroups;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAbstractFactory;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.commons.jcs.engine.behavior.ICache;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * canl have multiple caches. The remote relationships are consolidated and
 * restored via these managers. The facade provides a front to the composite
 * cache so the implmenetation is transparent.
 *
 * @deprecated use the new TYPE specific lateral factories.
 */
public class LateralJGCacheFactory
    extends LateralCacheAbstractFactory
{
    private static final Log log = LogFactory.getLog( LateralJGCacheFactory.class );

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes,
     *      org.apache.commons.jcs.engine.behavior.ICompositeCacheManager)
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr )
    {

        LateralCacheAttributes lac = (LateralCacheAttributes) iaca;
        ArrayList noWaits = new ArrayList();

        LateralJGCacheManager lcm = LateralJGCacheManager.getInstance( lac, cacheMgr );
        ICache ic = lcm.getCache( lac.getCacheName() );
        if ( ic != null )
        {
            noWaits.add( ic );
        }

        createListener( lac, cacheMgr );

        // create the no wait facade.
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), iaca.getCacheName() );

        return lcnwf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.lateral.LateralCacheAbstractFactory#createListener(org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes,
     *      org.apache.commons.jcs.engine.behavior.ICompositeCacheManager)
     */
    public void createListener( LateralCacheAttributes lac, ICompositeCacheManager cacheMgr )
    {
        // don't create a listener if we are not receiving.
        if ( lac.isReceive() )
        {

            if ( log.isInfoEnabled() )
            {
                log.info( "Creating listener for " + lac );
            }

            try
            {

                LateralCacheJGListener.getInstance( lac, cacheMgr );

            }
            catch ( Exception e )
            {
                log.error( "Problem creating lateral listener", e );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Not creating a listener since we are not receiving." );
            }
        }
    }

}
