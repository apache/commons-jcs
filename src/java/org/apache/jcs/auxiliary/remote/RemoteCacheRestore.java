package org.apache.jcs.auxiliary.remote;

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

import java.rmi.Naming;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.behavior.ICacheRestore;

/**
 * Used to repair the remote caches managed by the associated instance of RemoteCacheManager.
 * <p>
 * When there is an error the monitor kicks off. The Failover runner starts looks for a manager with
 * a connection to a remote cache that is not in error. If a manager's connection to a remote cache
 * is found to be in error, the restorer kicks off and tries to reconnect. When it is succesful, the
 * status of the manager changes.
 * <p>
 * When the failover runner finds that the primary is in good shape, it will switch back. Switching
 * back invovles setting the first no wait on the no wait facade.
 */
public class RemoteCacheRestore
    implements ICacheRestore
{
    private final static Log log = LogFactory.getLog( RemoteCacheRestore.class );

    private final RemoteCacheManager rcm;

    // private final AuxiliaryCacheManager rcm;
    private boolean canFix = true;

    private Object remoteObj;

    /**
     * Constructs with the given instance of RemoteCacheManager.
     * @param rcm
     */
    public RemoteCacheRestore( RemoteCacheManager rcm )
    {
        // public RemoteCacheRestore(AuxiliaryCacheManager rcm) {
        this.rcm = rcm;
    }

    /**
     * Returns true if the connection to the remote host for the corresponding cache manager can be
     * successfully re-established.
     * <p>
     * @return true if we found a failover server
     */
    public boolean canFix()
    {
        if ( !canFix )
        {
            return canFix;
        }
        String registry = "//" + rcm.host + ":" + rcm.port + "/" + rcm.service;
        if ( log.isInfoEnabled() )
        {
            log.info( "looking up server " + registry );
        }
        try
        {
            remoteObj = Naming.lookup( registry );
            if ( log.isInfoEnabled() )
            {
                log.info( "looking up server " + registry );
            }
        }
        catch ( Exception ex )
        {
            log.error( "host=" + rcm.host + "; port" + rcm.port + "; service=" + rcm.service );
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
        rcm.fixCaches( (IRemoteCacheService) remoteObj, (IRemoteCacheObserver) remoteObj );

        if ( log.isInfoEnabled() )
        {
            String msg = "Remote connection to " + "//" + rcm.host + ":" + rcm.port + "/" + rcm.service + " resumed.";
            log.info( msg );
        }
    }
}
