package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to periodically broadcast our location to other caches that might be
 * listening.
 *
 * @author Aaron Smuts
 *
 */
public class UDPDiscoverySenderThread
    implements Runnable
{
    private final static Log log = LogFactory.getLog( UDPDiscoverySenderThread.class );

    // the UDP multicast port
    private String discoveryAddress = "";

    private int discoveryPort = 0;

    // the host and port we listen on for TCP socket connections
    private String myHostName = null;

    private int myPort = 0;

    private ArrayList cacheNames = new ArrayList();

    /**
     * @param cacheNames
     *            The cacheNames to set.
     */
    protected void setCacheNames( ArrayList cacheNames )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Resetting cacheNames = [" + cacheNames + "]" );
        }
        this.cacheNames = cacheNames;
    }

    /**
     * @return Returns the cacheNames.
     */
    protected ArrayList getCacheNames()
    {
        return cacheNames;
    }

    /**
     * Constructs the sender with the port to tell others to connect to.
     * <p>
     * On construction the sender will request that the other caches let it know
     * their addresses.
     *
     * @param discoveryAddress
     *            host to broadcast to
     * @param discoveryPort
     *            port to broadcast to
     * @param myHostName
     *            host name we can be found at
     * @param myPort
     *            port we are listening on
     * @param cacheNames
     *            List of strings of the names of the regiond participating.
     */
    public UDPDiscoverySenderThread( String discoveryAddress, int discoveryPort, String myHostName, int myPort,
                                    ArrayList cacheNames )
    {
        this.discoveryAddress = discoveryAddress;
        this.discoveryPort = discoveryPort;

        this.myHostName = myHostName;
        this.myPort = myPort;

        this.cacheNames = cacheNames;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating sender thread for discoveryAddress = [" + discoveryAddress + "] and discoveryPort = ["
                + discoveryPort + "] myHostName = [" + myHostName + "] and port = [" + myPort + "]" );
        }

        UDPDiscoverySender sender = null;
        try
        {
            // move this to the run method and determine how often to call it.
            sender = new UDPDiscoverySender( discoveryAddress, discoveryPort );
            sender.requestBroadcast();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Sent a request broadcast to the group" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem sending a Request Broadcast", e );
        }
        finally
        {
            try
            {
                if ( sender != null )
                {
                    sender.destroy();
                }
            }
            catch ( Exception e )
            {
                log.error( "Problem closing Request Broadcast sender", e );
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        UDPDiscoverySender sender = null;
        try
        {
            // create this connection each time.
            // more robust
            sender = new UDPDiscoverySender( discoveryAddress, discoveryPort );

            sender.passiveBroadcast( myHostName, myPort, cacheNames );

            // todo we should consider sending a request broadcast every so
            // often.

            if ( log.isDebugEnabled() )
            {
                log.debug( "Called sender to issue a passive broadcast" );
            }

        }
        catch ( Exception e )
        {
            log.error( "Problem calling the UDP Discovery Sender [" + discoveryAddress + ":" + discoveryPort + "]", e );
        }
        finally
        {
            try
            {
                sender.destroy();
            }
            catch ( Exception e )
            {
                log.error( "Problem closing Passive Broadcast sender", e );
            }
        }
    }
}
