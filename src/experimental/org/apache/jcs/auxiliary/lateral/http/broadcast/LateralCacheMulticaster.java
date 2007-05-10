package org.apache.jcs.auxiliary.lateral.http.broadcast;

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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jcs.auxiliary.lateral.http.broadcast.LateralCacheUnicaster;

import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.threads.ThreadPoolManager;

/*
 * Used to multi-cast a key/val pair to the named cache on multiple servers.
 */
/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheMulticaster
{
    private final static Log log =
        LogFactory.getLog( LateralCacheMulticaster.class );

    private final static String servlet = "/cache/cache/LateralCacheServletReceiver";
    private final ICacheElement ice;
    private final ArrayList servers;

    /**
     * Constructor for the LateralCacheMulticaster object
     *
     * @param ice
     * @param servers
     */
    public LateralCacheMulticaster( ICacheElement ice, ArrayList servers )
    {
        this.servers = servers;
        this.ice = ice;

        if ( log.isDebugEnabled() )
        {
            log.debug( "In DistCacheMulticaster" );
        }
    }
    // end constructor

    /** Multi-casts the cache changes to the distributed servers. */
    public void multicast()
    {

        ThreadPoolManager tpm = ThreadPoolManager.getInstance();
        Iterator it = servers.iterator();
        while ( it.hasNext() )
        {
            tpm.runIt( new LateralCacheUnicaster( ice, ( String ) it.next() + servlet ) );
        }
        return;
    }
    // end run

}
// end class


