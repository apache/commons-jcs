package org.apache.commons.jcs.auxiliary.lateral.socket.udp;

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
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class LateralUDPSender
{
    private static final Log log =
        LogFactory.getLog( LateralUDPSender.class );

    private MulticastSocket m_localSocket;
    private InetAddress m_multicastAddress;
    private int m_multicastPort;

    private ILateralCacheAttributes ilca;


    /**
     * Constructor for the LateralUDPSender object
     *
     * @param lca
     * @throws IOException
     */
    public LateralUDPSender( ILateralCacheAttributes lca )
        throws IOException
    {
        this.ilca = lca;

        try
        {

            m_localSocket = new MulticastSocket();

            // Remote address.
            m_multicastAddress =
                InetAddress.getByName( lca.getUdpMulticastAddr() );
        }
        catch ( IOException e )
        {
            log.error( "Could not bind to multicast address " +
                lca.getUdpMulticastAddr(), e );

            throw e;
        }

        m_multicastPort = lca.getUdpMulticastPort();
    }


    /** Description of the Method */
    public void send( LateralElementDescriptor led )
        throws IOException
    {
        log.debug( "sending LateralElementDescriptor" );

        try
        {
            final MyByteArrayOutputStream byteStream = new MyByteArrayOutputStream();

            final ObjectOutputStream objectStream = new ObjectOutputStream( byteStream );

            objectStream.writeObject( led );
            objectStream.flush();

            final byte[] bytes = byteStream.getBytes();

            final DatagramPacket packet = new DatagramPacket( bytes, bytes.length, m_multicastAddress, m_multicastPort );

            m_localSocket.send( packet );
        }
        catch ( IOException e )
        {
            log.error( "Error sending message", e );

            throw e;
        }
    }

    // Service Methods //
    /** Description of the Method */
    public void update( ICacheElement<K, V> item, byte requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = led.UPDATE;
        send( led );
    }


    /** Description of the Method */
    public void remove( String cacheName, K key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }


    /** Description of the Method */
    public void remove( String cacheName, K key, long requesterId )
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


    /** Description of the Method */
    public void dispose( String cache )
        throws IOException
    {
        // nothing needs to be done
    }


    /** Description of the Method */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }


    /** Description of the Method */
    public void removeAll( String cacheName, long requesterId )
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
            LateralUDPSender lur = new LateralUDPSender( new LateralCacheAttributes() );

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

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
class MyByteArrayOutputStream extends ByteArrayOutputStream
{

    /**
     * Gets the bytes attribute of the MyByteArrayOutputStream object
     *
     * @return The bytes value
     */
    public byte[] getBytes()
    {
        return buf;
    }
}
// end class

