package org.apache.jcs.auxiliary.remote.http.server;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * This servlet simply reads and writes objects. The requests are packaged in a general wrapper. The
 * processor works on the wrapper object and returns a response wrapper.
 */
public class RemoteHttpCacheServlet
    extends HttpServlet
{
    /** Don't change. */
    private static final long serialVersionUID = 8752849397531933346L;

    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteHttpCacheServlet.class );

    /** The cache manager */
    private static CompositeCacheManager cacheMgr;

    /** Processes requests */
    private RemoteCacheServiceAdaptor remoteHttpCacheServiceAdaptor;

    /** This needs to be standard, since the other side is standard */
    private StandardSerializer serializer = new StandardSerializer();

    /**
     * Initializes the cache.
     * <p>
     * This provides an easy extension point. Simply extend this servlet and override the init
     * method to change the way the properties are loaded.
     * @param config
     * @throws ServletException
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        ensureCacheManager();

        setRemoteHttpCacheServiceAdaptor( new RemoteCacheServiceAdaptor( cacheMgr ) );

        super.init( config );
    }

    /**
     * Read the request, call the processor, write the response.
     * <p>
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Servicing a request." );
        }

        RemoteCacheRequest remoteRequest = readRequest( request );

        RemoteCacheResponse cacheResponse = getRemoteHttpCacheServiceAdaptor().processRequest( remoteRequest );

        writeResponse( response, cacheResponse );
    }

    /**
     * Read the request from the input stream.
     * <p>
     * @param request
     * @return RemoteHttpCacheRequest
     */
    protected RemoteCacheRequest readRequest( HttpServletRequest request )
    {
        RemoteCacheRequest remoteRequest = null;
        try
        {
            InputStream inputStream = request.getInputStream();
            if ( log.isDebugEnabled() )
            {
                log.debug( "after getting input stream and before reading it" );
            }

            remoteRequest = readRequestFromStream( inputStream );
        }
        catch ( Exception e )
        {
            log.error( "Could not get a RemoteHttpCacheRequest object from the input stream.", e );
        }
        return remoteRequest;
    }

    /**
     * Reads the response from the stream and then closes it.
     * <p>
     * @param inputStream
     * @return RemoteHttpCacheRequest
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected RemoteCacheRequest readRequestFromStream( InputStream inputStream )
        throws IOException, ClassNotFoundException
    {
        RemoteCacheRequest remoteRequest;
        ObjectInputStream ois = new ObjectInputStream( inputStream );

        remoteRequest = (RemoteCacheRequest) ois.readObject();
        ois.close();
        return remoteRequest;
    }

    /**
     * Write the response to the output stream.
     * <p>
     * @param response
     * @param cacheResponse
     */
    protected void writeResponse( HttpServletResponse response, RemoteCacheResponse cacheResponse )
    {
        try
        {
            response.setContentType( "application/octet-stream" );

            byte[] responseAsByteAray = serializer.serialize( cacheResponse );
            response.setContentLength( responseAsByteAray.length );

            OutputStream outputStream = response.getOutputStream();
            if ( log.isDebugEnabled() )
            {
                log.debug( "Opened output stream.  Response size: " + responseAsByteAray.length );
            }
            // WRITE
            outputStream.write( responseAsByteAray );
            outputStream.flush();
            outputStream.close();
        }
        catch ( Exception e )
        {
            log.error( "Problem writing response. " + cacheResponse, e );
        }
    }

    /**
     * Make sure we have a cache manager. This should have happened in the init method.
     */
    protected synchronized void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();
        }
    }

    /** Release the cache manager. */
    public void destroy()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Servlet Destroyed, shutting down JCS." );
        }
        cacheMgr.shutDown();
    }

    /**
     * Get servlet information
     * <p>
     * @return basic info
     */
    public String getServletInfo()
    {
        return "RemoteHttpCacheServlet";
    }

    /**
     * @param remoteHttpCacheProcessor the remoteHttpCacheProcessor to set
     */
    public void setRemoteHttpCacheServiceAdaptor( RemoteCacheServiceAdaptor remoteHttpCacheProcessor )
    {
        this.remoteHttpCacheServiceAdaptor = remoteHttpCacheProcessor;
    }

    /**
     * @return the remoteHttpCacheProcessor
     */
    public RemoteCacheServiceAdaptor getRemoteHttpCacheServiceAdaptor()
    {
        return remoteHttpCacheServiceAdaptor;
    }
}
