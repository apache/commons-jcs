package org.apache.jcs.auxiliary.remote.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * This can be injected into the the remote cache server as follows:
 * 
 * <pre>
 * jcs.remotecache.customrmisocketfactory=org.apache.jcs.auxiliary.remote.server.TimeoutConfigurableRMIScoketFactory
 * jcs.remotecache.customrmisocketfactory.readTimeout=5000
 * jcs.remotecache.customrmisocketfactory.openTimeout=5000
 * </pre>
 */
public class TimeoutConfigurableRMIScoketFactory
    extends RMISocketFactory
    implements Serializable
{
    /** Don't change. */
    private static final long serialVersionUID = 1489909775271203334L;

    /** The socket read timeout */
    private int readTimeout = 5000;

    /** The socket open timeout */
    private int openTimeout = 5000;

    /**
     * @param port
     * @return ServerSocket
     * @throws IOException
     */
    @Override
    public ServerSocket createServerSocket( int port )
        throws IOException
    {
        return new ServerSocket( port );
    }

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
        Socket socket = new Socket();
        socket.setSoTimeout( readTimeout );
        socket.setSoLinger( false, 0 );
        socket.connect( new InetSocketAddress( host, port ), openTimeout );
        return socket;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout( int readTimeout )
    {
        this.readTimeout = readTimeout;
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout()
    {
        return readTimeout;
    }

    /**
     * @param openTimeout the openTimeout to set
     */
    public void setOpenTimeout( int openTimeout )
    {
        this.openTimeout = openTimeout;
    }

    /**
     * @return the openTimeout
     */
    public int getOpenTimeout()
    {
        return openTimeout;
    }
}
