package org.apache.jcs.servlet;

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
import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// JCS Session Implementation
import org.apache.jcs.utils.servlet.session.HttpServletRequestFacade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Session example using the cache for session replicaiton. Modifed from Tomcat
 * examples. Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 * @author Aaron Smuts <asmuts@yahoo.com>
 * @created January 18, 2002
 */

public class SessionExampleServlet extends HttpServlet
{
    private final static Log log =
        LogFactory.getLog( SessionExampleServlet.class );

    ResourceBundle rb = ResourceBundle.getBundle( "LocalStrings" );

    /** Description of the Method */
    public void doGet( HttpServletRequest request,
                       HttpServletResponse response )
        throws IOException, ServletException
    {

        try
        {

            // get JCS wrapper
            request = new HttpServletRequestFacade( request, response );

            response.setContentType( "text/html" );

            PrintWriter out = response.getWriter();
            out.println( "<html>" );
            out.println( "<body bgcolor=\"white\">" );
            out.println( "<head>" );

            String title = rb.getString( "sessions.title" );
            out.println( "<title>" + title + "</title>" );
            out.println( "</head>" );
            out.println( "<body>" );

            // img stuff not req'd for source code html showing
            // relative links everywhere!

            // XXX
            // making these absolute till we work out the
            // addition of a PathInfo issue

            /*
             * out.println("<a href=\"/examples/servlets/sessions.html\">");
             * out.println("<img src=\"/examples/images/code.gif\" height=24 " +
             * "width=24 align=right border=0 alt=\"view code\"></a>");
             * out.println("<a href=\"/examples/servlets/index.html\">");
             * out.println("<img src=\"/examples/images/return.gif\" height=24 " +
             * "width=24 align=right border=0 alt=\"return\"></a>");
             */
            out.println( "<h3>" + title + "</h3>" );

            // Get session as normal, but it is coming fomr the JCS
            HttpSession session = null;// = request.getSession();
            try
            {
                session = request.getSession( true );
            }
            catch ( IllegalStateException ise )
            {
                log.warn( ise.toString() );
            }

            out.println( rb.getString( "sessions.id" ) + " " + session.getId() );
            out.println( "<br>" );
            out.println( rb.getString( "sessions.isnew" ) + " " + session.isNew() + "<br>" );
            out.println( rb.getString( "sessions.created" ) + " " );
            out.println( new Date( session.getCreationTime() ) + "<br>" );
            out.println( rb.getString( "sessions.lastaccessed" ) + " " );
            out.println( new Date( session.getLastAccessedTime() ) );
            out.println( "<br>" );
            out.println( rb.getString( "sessions.requestedid" ) + " " + request.getRequestedSessionId() + "<br>" );
            out.println( rb.getString( "sessions.requestedidvalid" ) + " " + request.isRequestedSessionIdValid() + "<br>" );
            //out.println(rb.getString("sessions.fromcookie") + " " + request.isRequestedSessionIdFromCookie() + "<br>");
            //out.println(rb.getString("sessions.fromurl") + " " + request.isRequestedSessionIdFromURL() + "<br>");

            String invalidate = request.getParameter( "INVALIDATE" );

            if ( invalidate != null )
            {
                // Remove From JCS
                session.invalidate();
            }
            else
            {
                String dataName = request.getParameter( "dataname" );
                String dataValue = request.getParameter( "datavalue" );
                if ( dataName != null && dataValue != null )
                {
                    session.setAttribute( dataName, dataValue );
                }

                out.println( "<P>" );
                out.println( rb.getString( "sessions.data" ) + "<br>" );
                Enumeration names = session.getAttributeNames();
                while ( names.hasMoreElements() )
                {
                    String name = ( String ) names.nextElement();
                    String value = session.getAttribute( name ).toString();
                    out.println( name + " = " + value + "<br>" );
                }
            }

            out.println( "<P>" );
            out.print( "<form action=\"" );
            out.print( response.encodeURL( "SessionExample" ) );
            out.print( "\" " );
            out.println( "method=POST>" );
            out.println( rb.getString( "sessions.dataname" ) );
            out.println( "<input type=text size=20 name=dataname>" );
            out.println( "<br>" );
            out.println( rb.getString( "sessions.datavalue" ) );
            out.println( "<input type=text size=20 name=datavalue>" );
            out.println( "<br>" );
            out.println( "<input type=submit>" );
            out.println( "</form>" );

            out.println( "<P>GET based form:<br>" );
            out.print( "<form action=\"" );
            out.print( response.encodeURL( "SessionExample" ) );
            out.print( "\" " );
            out.println( "method=GET>" );
            out.println( rb.getString( "sessions.dataname" ) );
            out.println( "<input type=text size=20 name=dataname>" );
            out.println( "<br>" );
            out.println( rb.getString( "sessions.datavalue" ) );
            out.println( "<input type=text size=20 name=datavalue>" );
            out.println( "<br>" );
            out.println( "<input type=submit>" );
            out.println( "</form>" );

            out.println( "<P>" );
            out.println( "<P>Invalidate session:<br>" );
            out.print( "<form action=\"" );
            out.print( response.encodeURL( "SessionExample" ) );
            out.print( "\" " );
            out.println( "method=POST>" );
            out.println( "<input type=\"hidden\" name=INVALIDATE value=TRUE>" );
            out.println( "<input type=submit value=\"Invalidate session\">" );
            out.println( "</form>" );

//		  out.print("<p><a href=\"");
//        out.print(response.encodeURL("SessionExample?dataname=foo&datavalue=bar"));
//        out.println("\" >URL encoded </a>");

            out.println( "</body>" );
            out.println( "</html>" );

            out.println( "</body>" );
            out.println( "</html>" );

        }
        catch ( Exception e )
        {
            log.error( e );
        }

    }

    /** Description of the Method */
    public void doPost( HttpServletRequest request,
                        HttpServletResponse response )
        throws IOException, ServletException
    {
        doGet( request, response );
    }

}
