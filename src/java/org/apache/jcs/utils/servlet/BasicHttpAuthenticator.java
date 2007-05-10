package org.apache.jcs.utils.servlet;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.config.IUtilConstants;

import sun.misc.BASE64Decoder;

/**
 * Used to perform basic http authentication.
 */
public class BasicHttpAuthenticator
{
    private final static Log log = LogFactory.getLog( BasicHttpAuthenticator.class );

    /** Contains the "WWW-Authenticate" http response header. */
    private final String wwwAuthHeader;

    /**
     * @param jcs
     *            the jcs parameter used to specify the "WWW-Authenticate" http
     *            response header.
     */
    public BasicHttpAuthenticator( String jcs )
    {
        this.wwwAuthHeader = "BASIC jcs=\"" + jcs + "\"";
    }

    /**
     * Authenticates the http <code>"Authorization"</code> header information.
     * <p>
     * @param req
     * @param res
     * @return boolean
     */
    public final boolean authenticate( HttpServletRequest req, HttpServletResponse res )
    {
        try
        {
            if ( !authorized( req.getHeader( "Authorization" ) ) )
            {
                res.setContentType( "text/html" );
                res.setHeader( "WWW-Authenticate", wwwAuthHeader );
                res.sendError( HttpServletResponse.SC_UNAUTHORIZED );
                return false;
            }
        }
        catch ( IOException ex )
        {
            log.warn( ex.getMessage() );
            return false;
        }
        return true;
    }

    /**
     * Returns true iff the given "Authorization" http request header contains
     * authorized user id and password.
     * <p>
     * @param authHeader
     * @return
     * @throws IOException
     */
    private boolean authorized( String authHeader )
        throws IOException
    {
        if ( authHeader == null || authHeader.length() < 9 )
        {
            return false;
        }
        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = authHeader.substring( 6 );

        BASE64Decoder dec = new BASE64Decoder();
        String userpassDecoded = new String( dec.decodeBuffer( userpassEncoded ) );
        int idx = userpassDecoded.indexOf( ':' );

        if ( idx == -1 )
        {
            return false;
        }
        String userid = userpassDecoded.substring( 0, idx );
        String password = userpassDecoded.substring( idx + 1 );

        if ( userid.trim().length() <= 0 || password.trim().length() <= 0 )
        {
            return false;
        }
        return checkPassword( userid, password );
    }

    /**
     * Default implementation of checking the password.
     * <p>
     * @param userid
     * @param password
     * @return true iff the given user id and password is valid.
     */
    protected boolean checkPassword( String userid, String password )
    {
        return userid.equalsIgnoreCase( IUtilConstants.ADMIN_USERID )
            && password.equals( IUtilConstants.ADMIN_PASSWORD );
    }
}
