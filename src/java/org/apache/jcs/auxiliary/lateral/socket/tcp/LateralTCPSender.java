package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;

import java.net.InetAddress;
import java.net.Socket;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

import org.apache.jcs.auxiliary.lateral.socket.tcp.utils.SocketOpener;

import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is based on the log4j SocketAppender class. I'm using a differnet
 * repair structure, so it is significant;y different.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id$
 */
public class LateralTCPSender
{
    private final static Log log =
        LogFactory.getLog( LateralTCPSender.class );

    private ILateralCacheAttributes ilca;

    private String remoteHost;
    private InetAddress address;
    int port = 1111;
    private ObjectOutputStream oos;
    private Socket socket;
    int counter = 0;

    // reset the ObjectOutputStream every 70 calls
    //private static final int RESET_FREQUENCY = 70;
    private final static int RESET_FREQUENCY = 70;

    /**
     * Only block for 10 seconds before timing out on a read. TODO: make
     * configurable. The default 10 it way too long.
     */
    private final static int timeOut = 10000;

    /** Only block for 5 seconds before timing out on startup. */
    private final static int openTimeOut = 5000;


    /**
     * Constructor for the LateralTCPSender object
     *
     * @param lca
     * @exception IOException
     */
    public LateralTCPSender( ILateralCacheAttributes lca )
        throws IOException
    {
        String p1 = lca.getTcpServer();
        String h2 = p1.substring( 0, p1.indexOf( ":" ) );
        int po = Integer.parseInt( p1.substring( p1.indexOf( ":" ) + 1 ) );
        if ( log.isDebugEnabled() )
        {
          log.debug( "h2 = " + h2 );
          log.debug( "po = " + po );
        }

        if ( h2 == null )
        {
          throw new IOException( "Cannot connect to invalid address " + h2 + ":" + po );
        }

        init( h2, po );
        this.ilca = lca;
    }


    /** Description of the Method */
    protected void init( String host, int port )
        throws IOException
    {
        this.port = port;
        this.address = getAddressByName( host );
        this.remoteHost = host;

        try
        {
            log.debug( "Attempting connection to " + address.getHostName() );
            //socket = new Socket( address, port );

            //  have time out socket open do this for us
            socket = SocketOpener.openSocket( host, port, openTimeOut );

            if ( socket == null )
            {
                throw new IOException( "Socket is null" );
            }

            socket.setSoTimeout( this.timeOut );
            synchronized ( this )
            {
                oos = new ObjectOutputStream( socket.getOutputStream() );
            }
        }
        catch ( java.net.ConnectException e )
        {
            log.debug( "Remote host " + address.getHostName() + " refused connection." );
            throw e;
        }
        catch ( IOException e )
        {
            log.debug( "Could not connect to " + address.getHostName() +
                ". Exception is " + e );
            throw e;
        }

    }
    // end constructor

    /**
     * Gets the addressByName attribute of the LateralTCPSender object
     *
     * @return The addressByName value
     */
    private InetAddress getAddressByName( String host )
      throws IOException
    {
        try
        {
            return InetAddress.getByName( host );
        }
        catch ( Exception e )
        {
            log.error( "Could not find address of [" + host + "]", e );
            throw new IOException( "Could not find address of [" + host + "]" + e.getMessage() );
            //return null;
        }
    }


    /** Sends commands to the lateral cache listener. */
    public void send( LateralElementDescriptor led )
        throws IOException
    {
        log.debug( "sending LateralElementDescriptor" );

        if ( led == null )
        {
            return;
        }

        if ( address == null )
        {
            throw new IOException( "No remote host is set for LateralTCPSender." );
            //return;
        }

        if ( oos != null )
        {
            try
            {
                oos.writeObject( led );
                oos.flush();
                if ( ++counter >= RESET_FREQUENCY )
                {
                    counter = 0;
                    // Failing to reset the object output stream every now and
                    // then creates a serious memory leak.
                    log.info( "Doing oos.reset()" );
                    oos.reset();
                }
            }
            catch ( IOException e )
            {
                oos = null;
                log.error( "Detected problem with connection: " + e );
                throw e;
            }
        }
    }


    /**
     * Sends commands to the lateral cache listener and gets a response. I'm
     * afraid that we could get into a pretty bad blocking situation here. This
     * needs work. I just wanted to get some form of get working. Will need some
     * sort of timeout.
     */
    public ICacheElement sendAndReceive( LateralElementDescriptor led )
        throws IOException
    {
        ICacheElement ice = null;

        log.debug( "sendAndReceive led" );

        if ( led == null )
        {
            return null;
        }

        if ( address == null )
        {
            throw new IOException( "No remote host is set for LateralTCPSender." );
            //return;
        }

        if ( oos != null )
        {
            try
            {
                oos.writeObject( led );
                oos.flush();

                try
                {
                    ObjectInputStream ois = new ObjectInputStream( socket.getInputStream() );
                    Object obj = ois.readObject();
                    ice = ( ICacheElement ) obj;
                    if ( ice == null )
                    {
                        //p( "ice is null" );
                        // TODO: coutn misses
                    }

                }
                catch ( IOException ioe )
                {
                    log.error( "Could not open ObjectInputStream to " + socket, ioe );
                }
                catch ( Exception e )
                {
                    log.error( e );
                }

                if ( ++counter >= RESET_FREQUENCY )
                {
                    counter = 0;
                    // Failing to reset the object output stream every now and
                    // then creates a serious memory leak.
                    log.info( "Doing oos.reset()" );
                    oos.reset();
                }
            }
            catch ( IOException e )
            {
                oos = null;
                log.error( "Detected problem with connection: " + e );
                throw e;
            }
        }
        return ice;
    }// end sendAndReceive

    // Service Methods //
    /** Description of the Method */
    public void update( ICacheElement item, byte requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = led.UPDATE;
        send( led );
    }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVE;
        send( led );
    }


    /** Description of the Method */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }


    /**
     * Closes connection used by all LateralTCPSenders for this lateral
     * conneciton. Dispose request should come into the facade and be sent to
     * all lateral cache sevices. The lateral cache service will then call this
     * method.
     */
    public void dispose( String cache )
        throws IOException
    {
        // WILL CLOSE CONNECTION USED BY ALL
        oos.close();
    }


    /** Description of the Method */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }


    /** Description of the Method */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVEALL;
        send( led );
    }


    /** Description of the Method */
    public static void main( String args[] )
    {
        try
        {
            LateralTCPSender lur = null;
            //new LateralTCPSender( "localhost", 1111 );

            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

            while ( notDone )
            {
                System.out.println( "enter mesage:" );
                message = br.readLine();
                CacheElement ce = new CacheElement( "test", "test", message );
                LateralElementDescriptor led = new LateralElementDescriptor( ce );
                lur.send( led );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
    }

}
// end class
