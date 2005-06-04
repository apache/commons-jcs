package org.apache.jcs.access.monitor;

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

import org.apache.xmlrpc.WebServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Starts an XML-RPC server for cache monitoring.
 * 
 * @since 1.0
 */
public class MonitorXMLRPCServer
{
    private static final Log log = LogFactory.getLog( MonitorXMLRPCServer.class );

    /**
     * Constructor for the MonitorXMLRPCServer object
     * 
     * @param port
     */
    public MonitorXMLRPCServer( int port )
    {
        try
        {
            WebServer server = new WebServer( port );
            server.addHandler( "JCSMonitor", new MonitorAccess() );
            server.setParanoid( false );
        }
        catch ( Exception ioe )
        {
            // Older versions of Apache XML-RPC's WebServer threw IOException.
            log.error( ioe );
        }
    }

}
