package org.apache.jcs.access.monitor;

import java.io.IOException;

import org.apache.xmlrpc.WebServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Starts an xml rpc server for cache monitoring.
 *
 * @author asmuts
 * @created February 12, 2002
 */
public class MonitorXMLRPCServer
{
    private final static Log log =
        LogFactory.getLog( MonitorXMLRPCServer.class );

    /**
     * Constructor for the MonitorXMLRPCServer object
     *
     * @param port
     */
    public MonitorXMLRPCServer( int port )
    {
        MonitorAccess mon = new MonitorAccess();

        try
        {
            WebServer server = new WebServer( port );
            server.addHandler( "JCSMonitor", mon );
            server.setParanoid( false );
        }
        catch ( Exception ioe )
        {
            // Older versions of Apache XML-RPC's WebServer threw IOException.
            log.error( ioe );
        }
    }

}
