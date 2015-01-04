package org.apache.commons.jcs.auxiliary.lateral.http.broadcast;

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

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class LateralCacheThread extends Thread
{
    private static final Log log =
        LogFactory.getLog( LateralCacheThread.class );

    private static final String servlet = "/rcash/ramraf/DistCacheServlet";

    /** Description of the Field */
    protected String hashtableName;

    /** Description of the Field */
    protected String key;

    /** Description of the Field */
    protected Serializable val;

    /** Description of the Field */
    protected boolean running = true;

    /** Description of the Field */
    protected String[] servers;

    /**
     * Constructor for the LateralCacheThread object
     *
     * @param hashtableName
     * @param key
     * @param val
     * @param servers
     */
    public LateralCacheThread( String hashtableName, String key, Serializable val, String[] servers )
    {
        this.hashtableName = hashtableName;
        this.key = key;
        this.val = val;
        this.servers = servers;
    }


    /** Main processing method for the LateralCacheThread object */
    public void run()
    {
        try
        {
            long start = System.currentTimeMillis();
            //if ( running ) {
            ICacheElement<K, V> cb = new CacheElement( hashtableName, key, val );
            log.debug( "key = " + key );
            String result = sendCache( cb );
            sleep( 100 );
            running = false;
            long end = System.currentTimeMillis();
            log.info( "transfer took " + String.valueOf( end - start ) + " millis" );

            return;
            //}
        }
        catch ( InterruptedException e )
        {
            running = false;
            return;
        }

    }
    // end run

    /** Description of the Method */
    public String sendCache( ICacheElement<K, V> cb )
    {
        String response = "";
        try
        {
            for ( int i = 0; i < servers.length; i++ )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "servers[i] + servlet = " + servers[i] + servlet );
                }
                // create our URL
                URL tmpURL = new URL( servers[i] + servlet );
                URL url = new URL( tmpURL.toExternalForm() );
                if ( log.isDebugEnabled() )
                {
                    log.debug( "tmpURL = " + tmpURL );
                }

                // Open our URLConnection

                log.debug( "Opening Connection." );

                URLConnection con = url.openConnection();

                if ( log.isDebugEnabled() )
                {
                    log.debug( "con = " + con );
                }

                writeObj( con, cb );
                response = read( con );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "response = " + response );
                }
            }
            // end for
        }
        catch ( MalformedURLException mue )
        {
            log.error( mue );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        // end catch

        // suggest gc
        cb = null;
        running = false;
        return response;
    }
    // end send cache

    // Write the Answer to the Connection
    /** Description of the Method */
    public void writeObj( URLConnection connection, ICacheElement<K, V> cb )
    {
        try
        {
            connection.setUseCaches( false );
            connection.setRequestProperty( "CONTENT_TYPE", "application/octet-stream" );
            connection.setDoOutput( true );
            connection.setDoInput( true );
            ObjectOutputStream os = new ObjectOutputStream( connection.getOutputStream() );
            log.debug( "os = " + os );

            // Write the ICacheItem to the ObjectOutputStream
            log.debug( "Writing  ICacheItem." );

            os.writeObject( cb );
            os.flush();

            log.debug( "closing output stream" );

            os.close();
        }
        catch ( IOException e )
        {
            log.error( e );
        }
        // end catch
    }


    /** Description of the Method */
    public String read( URLConnection connection )
    {
        String result = "";
        try
        {
            ObjectInputStream is = new ObjectInputStreamClassLoaderAware( connection.getInputStream(), null );
            result = ( String ) is.readObject();
            is.close();

            log.debug( "got result = " + result );
        }
        catch ( IOException e )
        {
            log.error( e );
        }
        catch ( ClassNotFoundException ce )
        {
            log.error( ce );
        }
        return result;
    }
}
