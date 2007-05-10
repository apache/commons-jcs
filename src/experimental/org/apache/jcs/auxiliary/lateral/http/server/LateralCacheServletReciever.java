package org.apache.jcs.auxiliary.lateral.http.server;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Aaron Smuts
 * @created January 15, 2002
 * @version 1.0
 */
public class LateralCacheServletReciever
    extends HttpServlet
{
    private final static Log log = LogFactory.getLog( LateralCacheServletReciever.class );

    private static CompositeCacheManager cacheMgr;

    /** Initializes the cache. */
    public void init( ServletConfig config )
        throws ServletException
    {
        cacheMgr = CompositeCacheManager.getInstance();

        super.init( config );
    }

    /** SERVICE THE REQUEST */
    public void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "The LateralCacheServlet has been called.\n" );
        }

        ICacheElement item = null;

        try
        {

            // Create the ObjectInputStream with
            // the Request InputStream.
            ObjectInputStream ois = new ObjectInputStream( request.getInputStream() );

            if ( log.isDebugEnabled() )
            {
                log.debug( "after getting input stream and before reading it" );
            }

            // READ POLLOBJ
            item = (ICacheElement) ois.readObject();
            ois.close();

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( item == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "item is null in LateralCacheServlet" );
            }
        }
        else
        {
            String hashtableName = item.getCacheName();
            Serializable key = item.getKey();
            Serializable val = item.getVal();

            log.debug( "item read in = " + item );
            log.debug( "item.getKey = " + item.getKey() );

            CompositeCache cache = (CompositeCache) cacheMgr.getCache( hashtableName );
            try
            {
                // need to set as from lateral
                cache.localUpdate( item );
            }
            catch ( Exception e )
            {
                log.error( "Problem putting item in cache " + item, e );
            }
        }

        try
        {

            // BEGIN RESPONSE
            response.setContentType( "application/octet-stream" );

            ObjectOutputStream oos = new ObjectOutputStream( response.getOutputStream() );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Opened output stream.\n" );
            }

            String result = "Completed transfer";

            // echo a message to the client
            oos.writeObject( result );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Wrote object to output stream" );
            }

            oos.flush();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Flushed output stream.\n" );
            }

            oos.close();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Closed output stream.\n" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem writing response.", e );
        }
    }

    /**
     * Make sure we have a cache manager. This should have happened in the init
     * method.
     *
     */
    protected synchronized void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();
            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr was null in LateralCacheServlet" );
            }
        }
    }

    /** Release the cache manager. */
    public void destroy()
    {
        cacheMgr.release();
    }

    /** Get servlet information */
    public String getServletInfo()
    {
        return "LateralCacheServlet v1";
    }
}
