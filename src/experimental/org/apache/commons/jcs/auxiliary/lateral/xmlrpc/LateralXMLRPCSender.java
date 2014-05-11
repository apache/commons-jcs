package org.apache.commons.jcs.auxiliary.lateral.xmlrpc;

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

import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.commons.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.xmlrpc.behavior.IXMLRPCConstants;
import org.apache.commons.jcs.auxiliary.lateral.xmlrpc.utils.XMLRPCSocketOpener;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClientLite;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;

/**
 * This class is based on the log4j SocketAppender class. I'm using a differnet
 * repair structure, so it is significant;y different.
 *
 * @version $Id$
 */
public class LateralXMLRPCSender implements IXMLRPCConstants
{
    private static final Log log =
        LogFactory.getLog( LateralXMLRPCSender.class );

    private ILateralCacheAttributes ilca;

    private String remoteHost;
    private InetAddress address;
    int port = 1111;

    private XmlRpcClientLite xmlrpc;
    int counter = 0;

    /**
     * Only block for 5 seconds before timing out on startup.
     */
    private static final int openTimeOut = 5000;


    /**
     * Constructor for the LateralXMLRPCSender object
     *
     * @param lca
     * @throws IOException
     */
    public LateralXMLRPCSender( ILateralCacheAttributes lca )
        throws IOException
    {
        String p1 = lca.getHttpServer();
        String h2 = p1.substring( 0, p1.indexOf( ":" ) );
        int po = Integer.parseInt( p1.substring( p1.indexOf( ":" ) + 1 ) );
        log.debug( "h2 = " + h2 );
        init( h2, po );
        this.ilca = lca;
    }


    /**
     * Description of the Method
     *
     * @param host
     * @param port
     * @throws IOException
     */
    protected void init( String host, int port )
        throws IOException
    {
        this.port = port;
        this.address = getAddressByName( host );
        this.remoteHost = host;

        try
        {
            log.debug( "Attempting connection to " + address.getHostName() + ":" + port );

            xmlrpc = XMLRPCSocketOpener.openSocket( host, port, openTimeOut );

            if ( xmlrpc == null )
            {
                throw new IOException( "xmlrpc is null" );
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
     * Gets the addressByName attribute of the LateralXMLRPCSender object
     *
     * @return The addressByName value
     * @param host
     */
    private InetAddress getAddressByName( String host )
    {
        try
        {
            return InetAddress.getByName( host );
        }
        catch ( Exception e )
        {
            log.error( "Could not find address of [" + host + "]", e );
            return null;
        }
    }


    /**
     * Sends commands to the lateral cache listener.
     *
     * @param led
     * @throws IOException
     */
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
            throw new IOException( "No remote host is set for LateralXMLRPCSender." );
            //return;
        }

        try
        {
            Vector params = new Vector();
            params.add( serialize( led ) );
            Object junk = xmlrpc.execute( this.HANDLERNAME + ".execute", params );
        }
        catch ( IOException e )
        {
            //oos = null;
            log.error( "Detected problem with connection: " + e );
            throw e;
        }
        catch ( Exception e )
        {
            log.error( "Detected problem with connection: " + e );
            throw new IOException( e.getMessage() );
        }

    }


    /**
     * Sends commands to the lateral cache listener and gets a response. I'm
     * afraid that we could get into a pretty bad blocking situation here. This
     * needs work. I just wanted to get some form of get working. Will need some
     * sort of timeout.
     *
     * @return
     * @param led
     * @throws IOException
     */
    public ICacheElement<K, V> sendAndReceive( LateralElementDescriptor led )
        throws IOException
    {
        ICacheElement<K, V> ice = null;

        log.debug( "sendAndReceive led" );

        if ( led == null )
        {
            return null;
        }

        if ( address == null )
        {
            throw new IOException( "No remote host is set for LateralXMLRPCSender." );
            //return;
        }

        try
        {

            try
            {

                Vector params = new Vector();
                // this should call another method
                params.add( serialize( led ) );
                Object obj = xmlrpc.execute( IXMLRPCConstants.HANDLERNAME + ".execute", params );
                if ( !obj.equals( IXMLRPCConstants.NO_RESULTS ) )
                {
                    ice = ( ICacheElement<K, V> ) obj;
                    if ( ice == null )
                    {
                        //p( "ice is null" );
                        // TODO: coutn misses
                    }
                }
            }
            catch ( IOException ioe )
            {
                log.error( "Could not xmlrpc exceute " + xmlrpc, ioe );
            }
            catch ( Exception e )
            {
                log.error( e );
            }

        }
        catch ( Exception e )
        {
            log.error( "Detected problem with connection: " + e );
            throw new IOException( e.getMessage() );
        }
//        }
        return ice;
    }// end sendAndReceive

    // Service Methods //
    /**
     * Description of the Method
     *
     * @param item
     * @param requesterId
     * @throws IOException
     */
    public void update( ICacheElement<K, V> item, byte requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = led.UPDATE;
        send( led );
    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void remove( String cacheName, K key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void remove( String cacheName, K key, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVE;
        send( led );
    }


    /**
     * Description of the Method
     *
     * @throws IOException
     */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }


    /**
     * Closes connection used by all LateralXMLRPCSenders for this lateral
     * conneciton. Dispose request should come into the facade and be sent to
     * all lateral cache sevices. The lateral cache service will then call this
     * method.
     *
     * @param cache
     * @throws IOException
     */
    public void dispose( String cache )
        throws IOException
    {
        // WILL CLOSE CONNECTION USED BY ALL
        //oos.close();
        //xmlrpc.
    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @throws IOException
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVEALL;
        send( led );
    }


    /**
     * Description of the Method
     *
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            LateralXMLRPCSender lur = null;
            LateralCacheAttributes lca = new LateralCacheAttributes();
            lca.setHttpServer( "localhost:8181" );
            lur = new LateralXMLRPCSender( lca );

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

    /**
     * To prepare elements for distribution
     *
     * @return
     * @param obj
     * @throws IOException
     */
    static byte[] serialize( Serializable obj )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }

}
// end class
