package org.apache.jcs.auxiliary.lateral.http.server;

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

import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Aaron Smuts
 * @created January 15, 2002
 * @version 1.0
 */
public class LateralCacheServletReciever extends HttpServlet
{
    private final static Log log =
        LogFactory.getLog( LateralCacheServletReciever.class );

    private static CacheHub cacheMgr;

    /** Description of the Method */
    public void init( ServletConfig config )
        throws ServletException
    {
        cacheMgr = CacheHub.getInstance();

        super.init( config );
    }

    /** SERVICE THE REQUEST */
    public void service( HttpServletRequest request,
                         HttpServletResponse response )
        throws ServletException, IOException
    {

        log.debug( "The LateralCacheServlet has been called.\n" );

        if ( cacheMgr == null )
        {
            cacheMgr = CacheHub.getInstance();
            log.debug( "cacheMgr was null in LateralCacheServlet" );
        }

        ICacheElement item = null;

        try
        {

            // Create the ObjectInputStream with
            // the Request InputStream.
            ObjectInputStream ois =
                new ObjectInputStream( request.getInputStream() );

            log.debug( "after getting input stream and before reading it" );

            // READ POLLOBJ
            item = ( ICacheElement ) ois.readObject();
            ois.close();

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( item == null )
        {
            log.debug( "item is null in LateralCacheServlet" );
        }
        else
        {
            String hashtableName = item.getCacheName();
            Serializable key = item.getKey();
            Serializable val = item.getVal();

            log.debug( "item read in = " + item );
            log.debug( "item.getKey = " + item.getKey() );

            Cache cache = ( Cache ) cacheMgr.getCache( hashtableName );
            try
            {
                // need to set as from lateral
                cache.add( item );
            }
            catch ( Exception e )
            {
                // Ignored -- log it?
            }
        }

        try
        {

            // BEGIN RESPONSE
            response.setContentType( "application/octet-stream" );

            ObjectOutputStream oos =
                new ObjectOutputStream( response.getOutputStream() );

            log.debug( "Opened output stream.\n" );

            String result = "Completed transfer";

            // ECHO THE OBJECT TO THE RESPONSE
            oos.writeObject( result );

            log.debug( "Wrote object to output stream" );

            oos.flush();

            log.debug( "Flushed output stream.\n" );

            oos.close();

            log.debug( "Closed output stream.\n" );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    /** */
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

