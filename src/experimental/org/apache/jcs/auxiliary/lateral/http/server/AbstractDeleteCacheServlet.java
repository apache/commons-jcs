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
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SingleThreadModel;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jcs.engine.behavior.ICache;

import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.servlet.BasicHttpAuthenticator;

public abstract class AbstractDeleteCacheServlet
     extends HttpServlet implements SingleThreadModel
{
    private final static Log log =
        LogFactory.getLog( AbstractDeleteCacheServlet.class );

    /** Description of the Field */
    protected CompositeCacheManager cacheMgr;
    private BasicHttpAuthenticator authenticator;


    /** Description of the Method */
    public void init( ServletConfig config )
        throws ServletException
    {
        // subsclass must initialize the cacheMgr before here.
        authenticator = new BasicHttpAuthenticator( "jcs" );
        super.init( config );
    }


    /** Description of the Method */
    public void service( HttpServletRequest req, HttpServletResponse res )
        throws ServletException,
        IOException
    {
        if ( !authenticator.authenticate( req, res ) )
        {
            return;
        }
        Hashtable params = new Hashtable();
        res.setContentType( "text/html" );
        PrintWriter out = res.getWriter();
        try
        {
            String paramName;
            String paramValue;

            // GET PARAMETERS INTO HASHTABLE
            for ( Enumeration e = req.getParameterNames(); e.hasMoreElements();  )
            {
                paramName = ( String ) e.nextElement();
                paramValue = req.getParameter( paramName );
                params.put( paramName, paramValue );
                if ( log.isDebugEnabled() )
                {
                    log.debug( paramName + "=" + paramValue );
                }
            }
            String hashtableName = req.getParameter( "hashtableName" );
            String key = req.getParameter( "key" );

            if ( hashtableName == null )
            {
                hashtableName = req.getParameter( "cacheName" );
            }
            out.println( "<html><body bgcolor=#FFFFFF>" );

            if ( hashtableName != null )
            {

                if ( log.isDebugEnabled() )
                {
                    log.debug( "hashtableName = " + hashtableName );
                }
                out.println( "(Last hashtableName = " + hashtableName + ")" );

                if ( hashtableName.equals( "ALL" ) )
                {

                    // Clear all caches.
                    String[] list = cacheMgr.getCacheNames();
                    Arrays.sort( list );

                    for ( int i = 0; i < list.length; i++ )
                    {
                        String name = list[i];
                        ICache cache = cacheMgr.getCache( name );
                        cache.removeAll();
                    }
                    out.println( "All caches have been cleared!" );
                }
                else
                {

                    ICache cache = cacheMgr.getCache( hashtableName );

                    String task = ( String ) params.get( "task" );
                    if ( task == null )
                    {
                        task = "delete";
                    }

                    if ( task.equalsIgnoreCase( "stats" ) )
                    {

//                        out.println( "<br><br>" );
//                        out.println( "<b>Stats for " + hashtableName + ":</b><br>" );
//                        out.println( cache.getStats() );
//                        out.println( "<br>" );

                    }
                    else
                    {

                        // Remove the specified cache.

                        if ( key != null )
                        {
                            if ( key.toUpperCase().equals( "ALL" ) )
                            {
                                cache.removeAll();

                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Removed all elements from " + hashtableName );
                                }
                                out.println( "key = " + key );
                            }
                            else
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "key = " + key );
                                }
                                out.println( "key = " + key );
                                StringTokenizer toke = new StringTokenizer( key, "_" );

                                while ( toke.hasMoreElements() )
                                {
                                    String temp = ( String ) toke.nextElement();
                                    cache.remove( key );

                                    if ( log.isDebugEnabled() )
                                    {
                                        log.debug( "Removed " + temp + " from " + hashtableName );
                                    }
                                }
                            }
                        }
                        else
                        {
                            out.println( "key is null" );
                        }

                    }
                    // end is task == delete

                }
            }
            else
            {
                out.println( "(No hashTableName specified.)" );
            }

            // PRINT OUT MENU
            out.println( "<br>" );
            int antiCacheRandom = ( int ) ( 10000.0 * Math.random() );
            out.println( "<a href=?antiCacheRandom=" + antiCacheRandom
                 + ">List all caches</a><br>" );
            out.println( "<br>" );
            out.println( "<a href=?hashtableName=ALL&key=ALL&antiCacheRandom="
                 + antiCacheRandom
                 + "><font color=RED>Clear All Cache Regions</font></a><br>" );
            out.println( "<br>" );
            String[] list = cacheMgr.getCacheNames();
            Arrays.sort( list );
            out.println( "<div align=CENTER>" );
            out.println( "<table border=1 width=80%>" );
            out.println( "<tr bgcolor=#eeeeee><td>Cache Region Name</td><td>Size</td><td>Status</td><td>Stats</td>" );
            for ( int i = 0; i < list.length; i++ )
            {
                String name = list[i];
                out.println( "<tr><td><a href=?hashtableName=" + name + "&key=ALL&antiCacheRandom="
                     + antiCacheRandom + ">" + name + "</a></td>" );
                ICache cache = cacheMgr.getCache( name );
                out.println( "<td>" );
                out.print( cache.getSize() );
                out.print( "</td><td>" );
                int status = cache.getStatus();
                out.print( status == CacheConstants.STATUS_ALIVE ? "ALIVE"
                     : status == CacheConstants.STATUS_DISPOSED ? "DISPOSED"
                     : status == CacheConstants.STATUS_ERROR ? "ERROR"
                     : "UNKNOWN" );
                out.print( "</td>" );
                out.println( "<td><a href=?task=stats&hashtableName=" + name + "&key=NONE&antiCacheRandom="
                     + antiCacheRandom + ">stats</a></td>" );
            }
            out.println( "</table>" );
            out.println( "</div>" );
        }
        //CATCH EXCEPTIONS
        catch ( Exception e )
        {
            log.error( e );
            //log.logIt( "hashtableName = " + hashtableName );
            //log.logIt( "key = " + key );
        }
        // end try{
        finally
        {
            String isRedirect = ( String ) params.get( "isRedirect" );
            if ( isRedirect == null )
            {
                isRedirect = "N";
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "isRedirect = " + isRedirect );
            }
            String url;
            if ( isRedirect.equals( "Y" ) )
            {
                url = ( String ) params.get( "url" );
                if ( log.isDebugEnabled() )
                {
                    log.debug( "url = " + url );
                }
                res.sendRedirect( url );
                // will not work if there's a previously sent header
                out.println( "<br>\n" );
                out.println( " <script>" );
                out.println( " location.href='" + url + "'; " );
                out.println( " </script> " );
                out.flush();
            }
            else
            {
                url = "";
            }
            out.println( "</body></html>" );
        }
    }
    //end service()

}
// end class
