package org.apache.jcs.auxiliary.lateral.socket.tcp;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache License, Version
 * 2.0 (the "License") you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * A lateral cache service implementation. Does not implement getGroupKey
 * <p>
 * @version $Id$
 */
public class LateralTCPService
    implements ILateralCacheService, ILateralCacheObserver
{
    private final static Log log = LogFactory.getLog( LateralTCPService.class );

    private ITCPLateralCacheAttributes tcpLateralCacheAttributes;

    private LateralTCPSender sender;

    /**
     * use the vmid by default
     */
    private long listenerId = LateralCacheInfo.listenerId;

    /**
     * Constructor for the LateralTCPService object
     * <p>
     * @param lca ITCPLateralCacheAttributes
     * @exception IOException
     */
    public LateralTCPService( ITCPLateralCacheAttributes lca )
        throws IOException
    {
        this.setTcpLateralCacheAttributes( lca );
        try
        {
            log.debug( "creating sender, attributes = " + getTcpLateralCacheAttributes() );

            sender = new LateralTCPSender( lca );

            if ( log.isInfoEnabled() )
            {
                log.debug( "Created sender to [" + lca.getTcpServer() + "]" );
            }
        }
        catch ( IOException e )
        {
            // log.error( "Could not create sender", e );
            // This gets thrown over and over in recovery mode.
            // The stack trace isn't useful here.
            log.error( "Could not create sender to [" + lca.getTcpServer() + "] -- " + e.getMessage() );

            throw e;
        }
    }

    /**
     * @param item
     * @throws IOException
     */
    public void update( ICacheElement item )
        throws IOException
    {
        update( item, getListenerId() );
    }

    /**
     * If put is allowed, we will issue a put. If issue put on remove is configured, we will issue a
     * remove. Either way, we create a lateral element descriptor, which is essentially a JCS TCP
     * packet. It describes what operation the receiver should take when it gets the packet.
     * <p>
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#update(org.apache.jcs.engine.behavior.ICacheElement,
     *      long)
     */
    public void update( ICacheElement item, long requesterId )
        throws IOException
    {
        // if we don't allow put, see if we should remove on put
        if ( !this.getTcpLateralCacheAttributes().isAllowPut() )
        {
            // if we can't remove on put, and we can't put then return
            if ( !this.getTcpLateralCacheAttributes().isIssueRemoveOnPut() )
            {
                return;
            }
        }

        // if we shouldn't remove on put, then put
        if ( !this.getTcpLateralCacheAttributes().isIssueRemoveOnPut() )
        {
            LateralElementDescriptor led = new LateralElementDescriptor( item );
            led.requesterId = requesterId;
            led.command = LateralElementDescriptor.UPDATE;
            sender.send( led );
        }
        // else issue a remove with the hashcode for remove check on
        // on the other end, this will be a server config option
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Issuing a remove for a put" );
            }
            // set the value to null so we don't send the item
            CacheElement ce = new CacheElement( item.getCacheName(), item.getKey(), null );
            LateralElementDescriptor led = new LateralElementDescriptor( ce );
            led.requesterId = requesterId;
            led.command = LateralElementDescriptor.REMOVE;
            led.valHashCode = item.getVal().hashCode();
            sender.send( led );
        }
    }

    /**
     * Uses the default listener id and calls the next remove method.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheService#remove(java.lang.String,
     *      java.io.Serializable)
     */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, getListenerId() );
    }

    /**
     * Wraps the key in a LateralElementDescriptor.
     * <p>
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#remove(java.lang.String,
     *      java.io.Serializable, long)
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.REMOVE;
        sender.send( led );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#release()
     */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }

    /**
     * Will close the connection.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    public void dispose( String cacheName )
        throws IOException
    {
        sender.dispose( cacheName );
    }

    /**
     * The service does not get via this method, so this return null.
     * <p>
     * @param key
     * @return always null.
     * @throws IOException
     */
    public Serializable get( String key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "balking at get for key [" + key + "]" );
        }
        // p( "junk get" );
        // return get( cattr.cacheName, key, true );
        return null;
        // nothing needs to be done
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#get(java.lang.String, java.io.Serializable)
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        // if get is not allowed return
        if ( this.getTcpLateralCacheAttributes().isAllowGet() )
        {
            CacheElement ce = new CacheElement( cacheName, key, null );
            LateralElementDescriptor led = new LateralElementDescriptor( ce );
            // led.requesterId = requesterId; // later
            led.command = LateralElementDescriptor.GET;
            return sender.sendAndReceive( led );
        }
        else
        {
            // nothing needs to be done
            return null;
        }
    }

    /**
     * Gets the set of keys of objects currently in the group throws UnsupportedOperationException
     * <p>
     * @param cacheName
     * @param group
     * @return Set
     */
    public Set getGroupKeys( String cacheName, String group )
    {
        if ( true )
        {
            throw new UnsupportedOperationException( "Groups not implemented." );
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#removeAll(java.lang.String)
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, getListenerId() );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#removeAll(java.lang.String,
     *      long)
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.REMOVEALL;
        sender.send( led );
    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            LateralTCPSender sender = new LateralTCPSender( new TCPLateralCacheAttributes() );

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
                sender.send( led );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
    }

    // ILateralCacheObserver methods, do nothing here since
    // the connection is not registered, the udp service is
    // is not registered.

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheObserver#addCacheListener(java.lang.String,
     *      org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheObserver#addCacheListener(org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheObserver#removeCacheListener(java.lang.String,
     *      org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheObserver#removeCacheListener(org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void removeCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /**
     * @param listernId The listernId to set.
     */
    protected void setListenerId( long listernId )
    {
        this.listenerId = listernId;
    }

    /**
     * @return Returns the listernId.
     */
    protected long getListenerId()
    {
        return listenerId;
    }

    /**
     * @param tcpLateralCacheAttributes The tcpLateralCacheAttributes to set.
     */
    public void setTcpLateralCacheAttributes( ITCPLateralCacheAttributes tcpLateralCacheAttributes )
    {
        this.tcpLateralCacheAttributes = tcpLateralCacheAttributes;
    }

    /**
     * @return Returns the tcpLateralCacheAttributes.
     */
    public ITCPLateralCacheAttributes getTcpLateralCacheAttributes()
    {
        return tcpLateralCacheAttributes;
    }

}
