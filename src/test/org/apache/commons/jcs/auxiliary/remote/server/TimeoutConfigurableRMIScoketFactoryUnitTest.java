package org.apache.commons.jcs.auxiliary.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.jcs.auxiliary.remote.server.TimeoutConfigurableRMIScoketFactory;

import junit.framework.TestCase;

/** Unit tests for the custom factory */
public class TimeoutConfigurableRMIScoketFactoryUnitTest
    extends TestCase
{
    /**
     * Simple test to see that we can create a server socket and connect.
     * <p>
     * @throws IOException
     */
    public void testCreateAndConnect() throws IOException
    {
        // SETUP
        int port = 3455;
        String host = "localhost";
        TimeoutConfigurableRMIScoketFactory factory = new TimeoutConfigurableRMIScoketFactory();

        // DO WORK
        ServerSocket serverSocket = factory.createServerSocket( port );
        Socket socket = factory.createSocket( host, port );
        socket.close();
        serverSocket.close();

        // VERIFY
        // passive, no errors
    }
}
