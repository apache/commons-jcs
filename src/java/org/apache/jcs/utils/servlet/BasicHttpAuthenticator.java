package org.apache.jcs.utils.servlet;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jcs.utils.config.IUtilConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;

/**
 * Used to perform basic http authentication.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class BasicHttpAuthenticator
{
    private final static Log log =
        LogFactory.getLog( BasicHttpAuthenticator.class );

    /** Contains the "WWW-Authenticate" http response header. */
    private final String wwwAuthHeader;


    /**
     * @param jcs the jcs parameter used to specify the "WWW-Authenticate" http
     *      response header.
     */
    public BasicHttpAuthenticator( String jcs )
    {
        this.wwwAuthHeader = "BASIC jcs=\"" + jcs + "\"";
    }


    /**
     * Authenticates the http <code>"Authorization"</code> header information.
     */
    public final boolean authenticate( HttpServletRequest req, HttpServletResponse res )
    {
        try
        {
            if ( !authorized( req.getHeader( "Authorization" ) ) )
            {
                res.setContentType( "text/html" );
                res.setHeader( "WWW-Authenticate", wwwAuthHeader );
                res.sendError( res.SC_UNAUTHORIZED );
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
     *
     * @return true iff the given user id and password is valid.
     */
    protected boolean checkPassword( String userid, String password )
    {
        return userid.equalsIgnoreCase( IUtilConstants.ADMIN_USERID )
             && password.equals( IUtilConstants.ADMIN_PASSWORD );
    }
}
