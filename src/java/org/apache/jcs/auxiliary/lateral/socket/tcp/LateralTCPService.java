package org.apache.jcs.auxiliary.lateral.socket.tcp;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * A lateral cache service implementation.
 * Does not implement getGroupKey
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created January 15, 2002
 * @version $Id$
 */
public class LateralTCPService
     implements ILateralCacheService, ILateralCacheObserver
{
    private final static Log log =
        LogFactory.getLog( LateralTCPService.class );

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

    /** */
    public void update( ICacheElement item, byte requesterId )
        throws IOException
    {
        LateralElementDescriptor led = new LateralElementDescriptor( item );
        led.requesterId = requesterId;
        led.command = led.UPDATE;
        sender.send( led );
    }

    /** */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, LateralCacheInfo.listenerId );
    }

    /** */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVE;
        sender.send( led );
    }

    /** */
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

    /** */
    public Serializable get( String key )
        throws IOException
    {
        //p( "junk get" );
        //return get( cattr.cacheName, key, true );
        return null;
        // nothing needs to be done
    }

    /** */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        //led.requesterId = requesterId; // later
        led.command = led.GET;
        return sender.sendAndReceive( led );
        //return null;
        // nothing needs to be done
    }

    /**
     * Gets the set of keys of objects currently in the group
     * throws UnsupportedOperationException
     */
    public Set getGroupKeys(String cacheName, String group)
    {
        if (true) 
        {
            throw new UnsupportedOperationException("Groups not implemented.");
        }        
        return null;
    }

    /** */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, LateralCacheInfo.listenerId );
    }

    /** */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, "ALL", null );
        LateralElementDescriptor led = new LateralElementDescriptor( ce );
        led.requesterId = requesterId;
        led.command = led.REMOVEALL;
        sender.send( led );
    }

    /** */
    public static void main( String args[] )
    {
        try
        {
            LateralTCPSender sender =
                new LateralTCPSender( new LateralCacheAttributes() );

            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            BufferedReader br =
                new BufferedReader( new InputStreamReader( System.in ) );

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

    /** */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /** */
    public void addCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }


    /** */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Empty
    }

    /** */
    public void removeCacheListener( ICacheListener obj )
        throws IOException
    {
        // Empty
    }

}

