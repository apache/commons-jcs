package org.apache.jcs.auxiliary.lateral.javagroups;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.javagroups.behavior.IJGConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/**
 * This class is based on the log4j SocketAppender class. I'm using a differnet
 * repair structure, so it is significant;y different.
 *
 * @version $Id$
 */
public class LateralJGSender implements IJGConstants
{
    private final static Log log =
        LogFactory.getLog( LateralJGSender.class );

    private ILateralCacheAttributes ilca;

    private String remoteHost;
    private InetAddress address;
    int port = 1111;

    private Channel javagroups;
    private RpcDispatcher disp;

    private JGConnectionHolder holder;

    //private ObjectOutputStream oos;
    //private Socket socket;
    int counter = 0;

    /**
     * Only block for 5 seconds before timing out on startup.
     */
    private final static int openTimeOut = 5000;


    /**
     * Constructor for the LateralJGSender object
     *
     * @param lca
     * @exception IOException
     */
    public LateralJGSender( ILateralCacheAttributes lca )
        throws IOException
    {
        this.ilca = lca;
        init();
    }


    /**
     * Create holder.
     *
     * @exception IOException
     */
    protected void init()
        throws IOException
    {

        try
        {
            log.debug( "Attempting ccreate channel."  );

            holder = JGConnectionHolder.getInstance(ilca);
            javagroups = holder.getChannel();

            if ( javagroups == null )
            {
                throw new IOException( "javagroups is null" );
            }

        }
        catch ( java.net.ConnectException e )
        {
            log.debug( "Remote host refused connection." );
            throw e;
        }
        catch ( Exception e )
        {
            log.debug( "Could not connect to channel" +
                ". Exception is " + e );
            throw new IOException(e.getMessage());
        }

    }
    // end constructor


    /**
     * Sends commands to the lateral cache listener.
     *
     * @param led
     * @exception IOException
     */
    public void send( LateralElementDescriptor led )
        throws IOException
    {
        log.debug( "sending LateralElementDescriptor" );

        if ( led == null )
        {
            return;
        }

        try
        {

            Message send_msg = new Message( null, null, led );

            javagroups.send( send_msg );

        }
        catch ( Exception e )
        {
            log.error( "Detected problem with connection: " + e );
            throw new IOException( e.getMessage() );
        }

    }


    /**
     * Sends commands to the lateral cache listener and gets a response.
     *
     * @return
     * @param led
     * @exception IOException
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

        try
        {

            try
            {

                disp = holder.getDispatcher();

                RspList rsp_list = disp.callRemoteMethods( null, "handleGet", led.ce.getCacheName(), led.ce.getKey(),
                    GroupRequest.GET_ALL, 1000 );

                log.debug( "rsp_list = " + rsp_list );
                Vector vec = rsp_list.getResults();
                log.debug( "rsp_list size = " + vec.size() );
                Iterator it = vec.iterator();

                while ( it.hasNext() )
                {
                    ice = ( ICacheElement ) it.next();
                    if ( ice != null )
                    {
                        break;
                    }
                }

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
     * @exception IOException
     */
    public void update( ICacheElement item, long requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.UPDATE;
        send( led );
    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @param key
     * @exception IOException
     */
    public void remove( String cacheName, Serializable key )
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
     * @exception IOException
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.REMOVE;
        send( led );
    }


    /**
     * Description of the Method
     *
     * @exception IOException
     */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }


    /**
     * Closes connection used by all LateralJGSenders for this lateral
     * conneciton. Dispose request should come into the facade and be sent to
     * all lateral cache sevices. The lateral cache service will then call this
     * method.
     *
     * @param cache
     * @exception IOException
     */
    public void dispose( String cache )
        throws IOException
    {

    }


    /**
     * Description of the Method
     *
     * @param cacheName
     * @exception IOException
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
     * @exception IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.REMOVEALL;
        send( led );
    }

}
// end class
