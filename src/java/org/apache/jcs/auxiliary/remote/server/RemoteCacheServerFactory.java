package org.apache.jcs.auxiliary.remote.server;

/* ====================================================================
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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.IOException;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;

import java.util.Properties;

import org.apache.jcs.auxiliary.remote.RemoteUtils;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;

import org.apache.jcs.engine.behavior.ICacheServiceAdmin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides remote cache services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheServerFactory
     implements IRemoteCacheConstants
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheServerFactory.class );

    /** The single instance of the RemoteCacheServer object. */
    private static RemoteCacheServer instance;
    private static String serviceName;

    /** Constructor for the RemoteCacheServerFactory object */
    private RemoteCacheServerFactory() { }


    /////////////////////// Statup/shutdown methods. //////////////////
    /**
     * Starts up the remote cache server on this JVM, and binds it to the
     * registry on the given host and port.
     */
    public static void startup( String host, int port, String propFile )
        throws IOException,
        NotBoundException
    {
        if ( instance != null )
        {
            throw new IllegalArgumentException( "Server already started." );
        }
        synchronized ( RemoteCacheServer.class )
        {
            if ( instance != null )
            {
                return;
            }

            // TODO: make automatic
            RemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();
            rcsa.setConfigFileName( propFile );

            Properties prop = RemoteUtils.loadProps( propFile );

            String servicePortStr = prop.getProperty( REMOTE_CACHE_SERVICE_PORT );
            int servicePort = -1;
            try
            {
                servicePort = Integer.parseInt( servicePortStr );

                rcsa.setServicePort( servicePort );
                log.debug( "Remote cache service uses port number " + servicePort + "." );
            }
            catch ( NumberFormatException ignore )
            {
                log.debug( "Remote cache service port property " + REMOTE_CACHE_SERVICE_PORT + " not specified.  An anonymous port will be used." );
            }

            String lccStr = prop.getProperty( REMOTE_LOCAL_CLUSTER_CONSISTENCY );
            if ( lccStr == null )
            {
                lccStr = "true";
            }
            boolean lcc = Boolean.valueOf( lccStr ).booleanValue();
            rcsa.setLocalClusterConsistency( lcc );

            String acgStr = prop.getProperty( REMOTE_ALLOW_CLUSTER_GET );
            if ( acgStr == null )
            {
                acgStr = "true";
            }
            boolean acg = Boolean.valueOf( acgStr ).booleanValue();
            rcsa.setAllowClusterGet( acg );

            // CREATE SERVER
            instance = new RemoteCacheServer( rcsa );

            if ( host == null )
            {
                host = "";
            }
            // Register the RemoteCacheServer remote object in the registry.
            serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
            log.debug( "main> binding server to " + host + ":" + port + " with the name " +
                serviceName );
            try
            {
                Naming.rebind( "//" + host + ":" + port + "/" + serviceName, instance );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host
                     + ", port=" + port );
            }
        }
    }


    /**
     * put your documentation comment here
     *
     * @param host
     * @param port
     * @exception IOException
     */
    static void shutdownImpl( String host, int port )
        throws IOException
    {
        if ( instance == null )
        {
            return;
        }
        synchronized ( RemoteCacheServer.class )
        {
            if ( instance == null )
            {
                return;
            }
            log.debug( "Unbinding host=" + host + ", port=" + port + ", serviceName=" + serviceName );
            try
            {
                Naming.unbind( "//" + host + ":" + port + "/" + serviceName );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host
                     + ", port=" + port + ", serviceName=" + serviceName );
            }
            catch ( NotBoundException ex )
            {
                //ignore.
            }
            instance.release();
            instance = null;
            // TODO: safer exit ?
            try
            {
                Thread.currentThread().sleep( 2000 );
            }
            catch ( InterruptedException ex )
            {
            }
            System.exit( 0 );
        }
    }


    /**
     * Creates an local RMI registry on the default port, starts up the remote
     * cache server, and binds it to the registry.
     *
     * @param args The command line arguments
     */
    public static void main( String[] args )
        throws Exception
    {
        Properties prop = args.length > 0 ? RemoteUtils.loadProps( args[0] ) : new Properties();

        // shutdown
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-shutdown" ) != -1 )
        {
            String serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
            String registry = "//:" + Registry.REGISTRY_PORT + "/" + serviceName;

            if ( log.isDebugEnabled() )
            {
                log.debug( "looking up server " + registry );
            }
            Object obj = Naming.lookup( registry );
            if ( log.isDebugEnabled() )
            {
                log.debug( "server found" );
            }
            ICacheServiceAdmin admin = ( ICacheServiceAdmin ) obj;
            try
            {
                admin.shutdown();
            }
            catch ( Exception ex )
            {
                log.error( ex );
            }
            log.debug( "done." );
            System.exit( 0 );
        }

        // startup.
        int port;
        try
        {
            port = Integer.parseInt( prop.getProperty( "registry.port" ) );
        }
        catch ( NumberFormatException ex )
        {
            port = Registry.REGISTRY_PORT;
        }
        String host = prop.getProperty( "registry.host" );

        if ( host == null || host.trim().equals( "" ) || host.trim().equals( "localhost" ) )
        {
            log.debug( "main> creating registry on the localhost" );
            port = RemoteUtils.createRegistry( port );
        }
        log.debug( "main> starting up RemoteCacheServer" );
        RemoteCacheServerFactory.startup( host, port, args.length > 0 ? args[0] : null );
        log.debug( "main> done" );
    }
}

