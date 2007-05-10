package org.apache.jcs.auxiliary.lateral.http.remove;

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

import org.apache.jcs.auxiliary.lateral.http.remove.DeleteLateralCacheUnicaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.threads.ThreadPoolManager;

/*
 * Used to multi-cast a key/val pair to the named cache on multiple servers.
 */
public class DeleteLateralCacheMulticaster
{
    private final static Log log =
        LogFactory.getLog( DeleteLateralCacheMulticaster.class );

    // must get servletName from the props file
    private final String servlet;

    private final String hashtableName;
    private final String key;

    private final ArrayList servers;

    /**
     * Constructor for the DeleteLateralCacheMulticaster object
     *
     * @param hashtableName
     * @param key
     * @param servers
     * @param servlet
     */
    public DeleteLateralCacheMulticaster( String hashtableName, String key, ArrayList servers, String servlet )
    {
        this.hashtableName = hashtableName;
        this.key = key;
        this.servers = servers;
        this.servlet = servlet;

        if ( log.isDebugEnabled() )
        {
            log.debug( "In DistCacheMulticaster" );
        }
    }
    // end constructor

    /** Multi-casts the deltes to the distributed servers. */
    public void multicast()
    {

        ThreadPoolManager tpm = ThreadPoolManager.getInstance();
        Iterator it = servers.iterator();
        //p( "iterating through servers" );
        while ( it.hasNext() )
        {
            String url = ( String ) it.next() + servlet;
            //p( "url = " + url );
            tpm.runIt( new DeleteLateralCacheUnicaster( hashtableName, key, url ) );
        }
        return;
    }
}

