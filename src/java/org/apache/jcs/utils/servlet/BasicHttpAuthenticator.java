package org.apache.jcs.utils.servlet;

import java.io.IOException;

import javax.servlet.ServletResponse;

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
