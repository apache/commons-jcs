package org.apache.commons.jcs.auxiliary.remote.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/** For testing the custom socket factory configuration */
public class MockRMISocketFactory
    extends RMISocketFactory
    implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = 1056199478581218676L;

    /** for testing automatic property configuration. */
    private String testStringProperty;

    /**
     * @param host
     * @param port
     * @return Socket
     * @throws IOException
     */
    @Override
    public Socket createSocket( String host, int port )
        throws IOException
    {
        System.out.println( "Creating socket" );
        
        Socket socket = new Socket();
        socket.setSoTimeout( 1000 );
        socket.setSoLinger( false, 0 );
        socket.connect( new InetSocketAddress( host, port ), 1000 );
        return socket;
    }

    /**
     * @param port
     * @return ServerSocket
     * @throws IOException
     */
    @Override
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
        System.out.println( "Creating server socket" );

        return new ServerSocket( port );
    }

    /**
     * @param testStringProperty the testStringProperty to set
     */
    public void setTestStringProperty( String testStringProperty )
    {
        this.testStringProperty = testStringProperty;
    }

    /**
     * @return the testStringProperty
     */
    public String getTestStringProperty()
    {
        return testStringProperty;
    }
}
