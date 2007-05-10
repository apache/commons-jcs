package org.apache.jcs.utils.net;

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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple utility for getting the local host name.
 * <p>
 * @author Aaron Smuts
 */
public class HostNameUtil
{
    private final static Log log = LogFactory.getLog( HostNameUtil.class );

    /**
     * Gets the address for the local machine.
     * <p>
     * @return InetAddress.getLocalHost().getHostAddress(), or unknown if there
     *         is an error.
     */
    public static String getLocalHostAddress()
    {
        String hostAddress = "unknown";
        try
        {
            // todo, you should be able to set this
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            if ( log.isDebugEnabled() )
            {
                log.debug( "hostAddress = [" + hostAddress + "]" );
            }
        }
        catch ( UnknownHostException e1 )
        {
            log.error( "Couldn't get localhost address", e1 );
        }
        return hostAddress;
    }
}
