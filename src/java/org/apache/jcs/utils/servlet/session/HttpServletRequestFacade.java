
package org.apache.jcs.utils.servlet.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.Principal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.jcs.utils.servlet.session.DistSessionTracker;
import org.apache.jcs.utils.servlet.session.MetaHttpSession;

/**
 * Session wrapper, to overide some methods. Servlet 2.3 has an easier way to do
 * this.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @created January 15, 2002
 */
public class HttpServletRequestFacade implements HttpServletRequest
{
    private final static transient DistSessionTracker dst = DistSessionTracker.getInstance();
    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private MetaHttpSession ms;
    private String characterEncoding;


    /**
     * Needs some way to get cookies out, must pass res too!
     *
     * @param req
     * @param res
     */
    public HttpServletRequestFacade( HttpServletRequest req, HttpServletResponse res )
    {
        this.req = req;
        this.res = res;
    }


    /**
     * Gets the sessionId attribute of the HttpServletRequestFacade object
     *
     * @return The sessionId value
     */
    public String getSessionId()
    {
        return ms == null ? null : ms.session_id;
    }


    /**
     * Gets the session attribute of the HttpServletRequestFacade object
     *
     * @return The session value
     */
    public HttpSession getSession()
    {
        return getSession( false );
    }


    /**
     * Gets the session attribute of the HttpServletRequestFacade object
     *
     * @return The session value
     */
    public HttpSession getSession( boolean create )
    {
        if ( !create && ms != null )
        {
            return ms.sess;
        }
        ms = dst.getSession( create, req, res );
        return ms.sess;
    }


    //////////////////////// implements the ServletRequest interface. //////////////////
    /**
     * Gets the attribute attribute of the HttpServletRequestFacade object
     *
     * @return The attribute value
     */
    public Object getAttribute( String name )
    {
        return req.getAttribute( name );
    }


    /**
     * Gets the attributeNames attribute of the HttpServletRequestFacade object
     *
     * @return The attributeNames value
     */
    public Enumeration getAttributeNames()
    {
        return req.getAttributeNames();
    }


    /**
     * Gets the characterEncoding attribute of the HttpServletRequestFacade
     * object
     *
     * @return The characterEncoding value
     */
    public String getCharacterEncoding()
    {
        // FUTURE: Always delegate when we switch to servlet api 2.3
        return ( characterEncoding != null ? characterEncoding :
            req.getCharacterEncoding() );
    }


    /**
     * Overrides the name of the character encoding used in the body of this
     * request. This method must be called prior to reading request parameters
     * or reading input using <code>getReader()</code> . NOTE: This method will
     * not modify the underlying request until Servlet API 2.3 is adopted.
     *
     * @param enc The character encoding to be used
     * @exception UnsupportedEncodingException If the specified encoding is not
     *      supported.
     */
    public void setCharacterEncoding( String enc )
        throws UnsupportedEncodingException
    {
        // FUTURE: Call req.setCharacterEncoding(enc) for servlet api 2.3

        // Ensure that the specified encoding is valid
        byte buffer[] = new byte[1];
        buffer[0] = ( byte ) 'a';
        String dummy = new String( buffer, enc );

        // Save the validated encoding
        this.characterEncoding = enc;
    }


    /**
     * Gets the contentLength attribute of the HttpServletRequestFacade object
     *
     * @return The contentLength value
     */
    public int getContentLength()
    {
        return req.getContentLength();
    }


    /**
     * Gets the contentType attribute of the HttpServletRequestFacade object
     *
     * @return The contentType value
     */
    public String getContentType()
    {
        return req.getContentType();
    }


    /**
     * Gets the inputStream attribute of the HttpServletRequestFacade object
     *
     * @return The inputStream value
     */
    public ServletInputStream getInputStream()
        throws IOException
    {
        return req.getInputStream();
    }


    /**
     * Gets the parameter attribute of the HttpServletRequestFacade object
     *
     * @return The parameter value
     */
    public String getParameter( String name )
    {
        return req.getParameter( name );
    }


    /**
     * Gets the parameterNames attribute of the HttpServletRequestFacade object
     *
     * @return The parameterNames value
     */
    public Enumeration getParameterNames()
    {
        return req.getParameterNames();
    }


    /**
     * Gets the parameterValues attribute of the HttpServletRequestFacade object
     *
     * @return The parameterValues value
     */
    public String[] getParameterValues( String name )
    {
        return req.getParameterValues( name );
    }


    /**
     * The implementation of this method will remain somewhat expensive until
     * Servlet API 2.3 is adopted.
     *
     * @return The {3} value
     */
    public Map getParameterMap()
    {
        // FUTURE: Call req.getParameterMap() for servlet api 2.3

        Map params = new HashMap();
        Enumeration enum = req.getParameterNames();
        String name;
        while ( enum.hasMoreElements() )
        {
            name = ( String ) enum.nextElement();
            params.put( name, req.getParameterValues( name ) );
        }
        return params;
    }


    /**
     * Gets the protocol attribute of the HttpServletRequestFacade object
     *
     * @return The protocol value
     */
    public String getProtocol()
    {
        return req.getProtocol();
    }


    /**
     * Gets the scheme attribute of the HttpServletRequestFacade object
     *
     * @return The scheme value
     */
    public String getScheme()
    {
        return req.getScheme();
    }


    /**
     * Gets the serverName attribute of the HttpServletRequestFacade object
     *
     * @return The serverName value
     */
    public String getServerName()
    {
        return req.getServerName();
    }


    /**
     * Gets the serverPort attribute of the HttpServletRequestFacade object
     *
     * @return The serverPort value
     */
    public int getServerPort()
    {
        return req.getServerPort();
    }


    /**
     * Gets the reader attribute of the HttpServletRequestFacade object
     *
     * @return The reader value
     */
    public BufferedReader getReader()
        throws IOException
    {
        return req.getReader();
    }


    /**
     * Gets the remoteAddr attribute of the HttpServletRequestFacade object
     *
     * @return The remoteAddr value
     */
    public String getRemoteAddr()
    {
        return req.getRemoteAddr();
    }


    /**
     * Gets the remoteHost attribute of the HttpServletRequestFacade object
     *
     * @return The remoteHost value
     */
    public String getRemoteHost()
    {
        return req.getRemoteHost();
    }


    /**
     * Sets the attribute attribute of the HttpServletRequestFacade object
     *
     * @param key The new attribute value
     * @param o The new attribute value
     */
    public void setAttribute( String key, Object o )
    {
        req.setAttribute( key, o );
    }


    /** Description of the Method */
    public void removeAttribute( String name )
    {
        req.removeAttribute( name );
    }


    /**
     * Gets the locale attribute of the HttpServletRequestFacade object
     *
     * @return The locale value
     */
    public Locale getLocale()
    {
        return req.getLocale();
    }


    /**
     * Gets the locales attribute of the HttpServletRequestFacade object
     *
     * @return The locales value
     */
    public Enumeration getLocales()
    {
        return req.getLocales();
    }


    /**
     * Gets the secure attribute of the HttpServletRequestFacade object
     *
     * @return The secure value
     */
    public boolean isSecure()
    {
        return req.isSecure();
    }


    /**
     * Gets the requestDispatcher attribute of the HttpServletRequestFacade
     * object
     *
     * @return The requestDispatcher value
     */
    public RequestDispatcher getRequestDispatcher( String path )
    {
        return req.getRequestDispatcher( path );
    }


    /**
     * Gets the realPath attribute of the HttpServletRequestFacade object
     *
     * @return The realPath value
     */
    public String getRealPath( String path )
    {
        return req.getRealPath( path );
    }


    //////////////////////////// implements the HttpServletRequest interface. /////////////////
    /**
     * Gets the authType attribute of the HttpServletRequestFacade object
     *
     * @return The authType value
     */
    public String getAuthType()
    {
        return req.getAuthType();
    }


    /**
     * Gets the cookies attribute of the HttpServletRequestFacade object
     *
     * @return The cookies value
     */
    public Cookie[] getCookies()
    {
        return req.getCookies();
    }


    /**
     * Gets the dateHeader attribute of the HttpServletRequestFacade object
     *
     * @return The dateHeader value
     */
    public long getDateHeader( String name )
    {
        return req.getDateHeader( name );
    }


    /**
     * Gets the header attribute of the HttpServletRequestFacade object
     *
     * @return The header value
     */
    public String getHeader( String name )
    {
        return req.getHeader( name );
    }


    /**
     * Gets the headers attribute of the HttpServletRequestFacade object
     *
     * @return The headers value
     */
    public Enumeration getHeaders( String name )
    {
        return req.getHeaders( name );
    }


    /**
     * Gets the headerNames attribute of the HttpServletRequestFacade object
     *
     * @return The headerNames value
     */
    public Enumeration getHeaderNames()
    {
        return req.getHeaderNames();
    }


    /**
     * Gets the intHeader attribute of the HttpServletRequestFacade object
     *
     * @return The intHeader value
     */
    public int getIntHeader( String name )
    {
        return req.getIntHeader( name );
    }


    /**
     * Gets the method attribute of the HttpServletRequestFacade object
     *
     * @return The method value
     */
    public String getMethod()
    {
        return req.getMethod();
    }


    /**
     * Gets the pathInfo attribute of the HttpServletRequestFacade object
     *
     * @return The pathInfo value
     */
    public String getPathInfo()
    {
        return req.getPathInfo();
    }


    /**
     * Gets the pathTranslated attribute of the HttpServletRequestFacade object
     *
     * @return The pathTranslated value
     */
    public String getPathTranslated()
    {
        return req.getPathTranslated();
    }


    /**
     * Gets the contextPath attribute of the HttpServletRequestFacade object
     *
     * @return The contextPath value
     */
    public String getContextPath()
    {
        return req.getContextPath();
    }


    /**
     * Gets the queryString attribute of the HttpServletRequestFacade object
     *
     * @return The queryString value
     */
    public String getQueryString()
    {
        return req.getQueryString();
    }


    /**
     * Gets the remoteUser attribute of the HttpServletRequestFacade object
     *
     * @return The remoteUser value
     */
    public String getRemoteUser()
    {
        return req.getRemoteUser();
    }


    /**
     * Gets the userInRole attribute of the HttpServletRequestFacade object
     *
     * @return The userInRole value
     */
    public boolean isUserInRole( String role )
    {
        return req.isUserInRole( role );
    }


    /**
     * Gets the userPrincipal attribute of the HttpServletRequestFacade object
     *
     * @return The userPrincipal value
     */
    public Principal getUserPrincipal()
    {
        return req.getUserPrincipal();
    }


    /**
     * Gets the requestedSessionId attribute of the HttpServletRequestFacade
     * object
     *
     * @return The requestedSessionId value
     */
    public String getRequestedSessionId()
    {
        return dst.getRequestedSessionId( this );
    }


    /**
     * Gets the requestURI attribute of the HttpServletRequestFacade object
     *
     * @return The requestURI value
     */
    public String getRequestURI()
    {
        return req.getRequestURI();
    }


    /**
     * Gets the full request URL.
     *
     * @return A new buffer containing the reconstructed URL.
     */
    public StringBuffer getRequestURL()
    {
        // FUTURE: Delegate to getRequestURL() of wrapped request once
        // support for the 2.2 < servlet API is dropped

        StringBuffer url = new StringBuffer();
        String scheme = req.getScheme();
        int port = req.getServerPort();
        if ( port < 0 )
        {
            // Work around java.net.URL bug
            port = 80;
        }

        url.append( scheme );
        url.append( "://" );
        url.append( req.getServerName() );
        if ( ( scheme.equals( "http" ) && ( port != 80 ) )
             || ( scheme.equals( "https" ) && ( port != 443 ) ) )
        {
            url.append( ':' );
            url.append( port );
        }
        url.append( req.getRequestURI() );
        return url;
    }


    /**
     * Gets the servletPath attribute of the HttpServletRequestFacade object
     *
     * @return The servletPath value
     */
    public String getServletPath()
    {
        return req.getServletPath();
    }


    /**
     * Gets the requestedSessionIdValid attribute of the
     * HttpServletRequestFacade object
     *
     * @return The requestedSessionIdValid value
     */
    public boolean isRequestedSessionIdValid()
    {
        return dst.isRequestedSessionIdValid( this );
    }


    /**
     * Gets the requestedSessionIdFromCookie attribute of the
     * HttpServletRequestFacade object
     *
     * @return The requestedSessionIdFromCookie value
     */
    public boolean isRequestedSessionIdFromCookie()
    {
        return req.isRequestedSessionIdFromCookie();
    }


    /**
     * Gets the requestedSessionIdFromURL attribute of the
     * HttpServletRequestFacade object
     *
     * @return The requestedSessionIdFromURL value
     */
    public boolean isRequestedSessionIdFromURL()
    {
        return req.isRequestedSessionIdFromURL();
    }


    /**
     * Gets the requestedSessionIdFromUrl attribute of the
     * HttpServletRequestFacade object
     *
     * @return The requestedSessionIdFromUrl value
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return req.isRequestedSessionIdFromUrl();
    }
}
// end HttpServletRequestFacade


