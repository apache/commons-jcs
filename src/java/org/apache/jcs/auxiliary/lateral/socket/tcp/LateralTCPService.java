package org.apache.jcs.auxiliary.lateral.socket.tcp;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A lateral cache service implementation. Does not implement getGroupKey
 * 
 * @version $Id$
 */
public class LateralTCPService
    implements ILateralCacheService, ILateralCacheObserver
{
    private final static Log log = LogFactory.getLog( LateralTCPService.class );

    private ILateralCacheAttributes ilca;

    private LateralTCPSender sender;

    /**
     * Constructor for the LateralTCPService object
     * 
     * @param lca
     * @exception IOException
     */
    public LateralTCPService( ILateralCacheAttributes lca )
        throws IOException
    {
        this.ilca = lca;
        try
        {
            log.debug( "creating sender" );

            sender = new LateralTCPSender( lca );

            log.debug( "created sender" );
        }
        catch ( IOException e )
        {
            //log.error( "Could not create sender", e );
            // This gets thrown over and over in recovery mode.
            // The stack trace isn't useful here.
            log.error( "Could not create sender to [" + lca.getTcpServer() + "] -- " + e.getMessage() );

            throw e;
        }
    }

    // -------------------------------------------------------- Service Methods

    /** */
    public void update( ICacheElement item )
        throws IOException
    {
        update( item, LateralCacheInfo.listenerId );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#update(org.apache.jcs.engine.behavior.ICacheElement,
     *      long)
     */
    public void update( ICacheElement item, long requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = LateralElementDescriptor.UPDATE;
        sender.send( led );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheService#remove(java.lang.String,
     *      java.io.Serializable)
     */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }

    /*
     * (non-Javadoc)
     * 
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
     * 
     * @see org.apache.jcs.engine.behavior.ICacheService#release()
     */
    public void release()
        throws IOException
    {
        // nothing needs to be done
    }

    /** Will close the connection. */
    public void dispose( String cache )
        throws IOException
    {
        sender.dispose( cache );
    }

    /*
     *  
     */
    public Serializable get( String key )
        throws IOException
    {
        //p( "junk get" );
        //return get( cattr.cacheName, key, true );
        return null;
        // nothing needs to be done
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheService#get(java.lang.String,
     *      java.io.Serializable)
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        //led.requesterId = requesterId; // later
        led.command = LateralElementDescriptor.GET;
        return sender.sendAndReceive( led );
        //return null;
        // nothing needs to be done
    }

    /**
     * Gets the set of keys of objects currently in the group throws
     * UnsupportedOperationException
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
     * 
     * @see org.apache.jcs.engine.behavior.ICacheService#removeAll(java.lang.String)
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }

    /*
     * (non-Javadoc)
     * 
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
     * 
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            LateralTCPSender sender = new LateralTCPSender( new LateralCacheAttributes() );

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
     * 
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
     * 
     * @see org.apache.jcs.engine.behavior.ICacheObserver#addCacheListener(org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /*
     * (non-Javadoc)
     * 
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
     * 
     * @see org.apache.jcs.engine.behavior.ICacheObserver#removeCacheListener(org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void removeCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

}
