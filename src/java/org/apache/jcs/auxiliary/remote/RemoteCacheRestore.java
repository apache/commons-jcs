package org.apache.jcs.auxiliary.remote;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.rmi.Naming;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.behavior.ICacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to repair the remote caches managed by the associated instance of
 * RemoteCacheManager. When there is an error the monitor kicks off. The
 * Failover runner starts looks for a manager with a connection to a remote
 * cache that is not in error. If a manager's connection to a remote cache is
 * found to be in error, the restorer kicks off and tries to reconnect. When it
 * is succesful, the status of the manager changes. When the failover runner
 * finds that the primary is in good shape, it will switch back.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheRestore implements ICacheRestore
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheRestore.class );

    private final RemoteCacheManager rcm;
    //private final IAuxiliaryCacheManager rcm;
    private boolean canFix = true;

    private Object remoteObj;


    /**
     * Constructs with the given instance of RemoteCacheManager.
     *
     * @param rcm
     */
    public RemoteCacheRestore( RemoteCacheManager rcm )
    {
        //public RemoteCacheRestore(IAuxiliaryCacheManager rcm) {
        this.rcm = rcm;
    }


    /**
     * Returns true if the connection to the remote host for the corresponding
     * cache manager can be successfully re-established.
     */
    public boolean canFix()
    {
        if ( !canFix )
        {
            return canFix;
        }
        String registry = "//" + rcm.host + ":" + rcm.port + "/" + rcm.service;
        log.info( "looking up server " + registry );
        try
        {
            remoteObj = Naming.lookup( registry );
            log.info( "looking up server " + registry );
        }
        catch ( Exception ex )
        {
            //log.error(ex, "host=" + rcm.host + "; port" + rcm.port + "; service=" + rcm.service );
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
        rcm.fixCaches( ( IRemoteCacheService ) remoteObj, ( IRemoteCacheObserver ) remoteObj );
        String msg = "Remote connection to " + "//" + rcm.host + ":" + rcm.port + "/" + rcm.service + " resumed.";
        log.info( msg );
    }
}
