package org.apache.jcs.auxiliary.lateral.http.broadcast;

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

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.utils.threads.IThreadPoolRunnable;

/**
 * Used to uni-cast a ICacheItem to the named cache on the target server.
 */
public class LateralCacheUnicaster
     implements IThreadPoolRunnable
{
    private final static Log log =
        LogFactory.getLog( LateralCacheUnicaster.class );

    private final ICacheElement item;
    private final String urlStr;

    private URLConnection conn;

    /**
     * Constructs with the given ICacheItem and the url of the target server.
     *
     * @param item
     * @param urlStr
     */
    public LateralCacheUnicaster( ICacheElement item, String urlStr )
    {
        this.item = item;
        this.urlStr = urlStr;

        log.debug( "In LateralCacheUnicaster2, " + Thread.currentThread().getName() );
    }


    /**
     * Called when this object is first loaded in the thread pool. Important:
     * all workers in a pool must be of the same type, otherwise the mechanism
     * becomes more complex.
     *
     * @return The initData value
     */
    public Object[] getInitData()
    {
        return null;
    }


    /**
     * Sends a ICacheItem to the target server. This method will be executed in
     * one of the pool's threads. The thread will be returned to the pool.
     */
    // Todo: error recovery and retry.
    public void runIt( Object thData[] )
    {
        long start = System.currentTimeMillis();

        try
        {
            log.debug( "url = " + urlStr );

            // create our URL
            URL tmpURL = new URL( urlStr );
            URL url = new URL( tmpURL.toExternalForm() );

            log.debug( "tmpURL = " + tmpURL );

            // Open our URLConnection

            log.debug( "Opening Connection." );

            conn = url.openConnection();

            if ( log.isDebugEnabled() )
            {
                log.debug( "conn = " + conn );
            }

            String response = sendCacheItem();

            if ( log.isDebugEnabled() )
            {
                log.debug( "response = " + response );
            }

            conn = null;
        }
        catch ( MalformedURLException mue )
        {
            log.warn( "mue - Unicaster couldn't connect to bad url " + urlStr );
        }
        catch ( ConnectException ce )
        {
            log.warn( "ce - Unicaster couldn't connect to " + urlStr );
        }
        catch ( IOException ioe )
        {
            log.warn( "ioe - Unicaster couldn't connect to " + urlStr );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "transfer took " + String.valueOf( System.currentTimeMillis() - start ) + " millis\n" );
        }

        return;
    }

    /**
     * Sends the ICacheItem to the current URLConnection.
     *
     * @return the response from the current URLConnection.
     */
    private String sendCacheItem()
    {
        try
        {
            conn.setUseCaches( false );
            conn.setRequestProperty( "CONTENT_TYPE", "application/octet-stream" );
            conn.setDoOutput( true );
            conn.setDoInput( true );
            ObjectOutputStream os = new ObjectOutputStream( conn.getOutputStream() );
            try
            {
                log.debug( "os = " + os );

                // Write the ICacheItem to the ObjectOutputStream

                log.debug( "Writing ICacheItem." );

                os.writeObject( item );
            }
            finally
            {
                os.close();
            }
            return getResponse();
        }
        catch ( IOException e )
        {
            log.warn( "ie - couldn't send item " + urlStr );
        }

        return "";
    }

    /**
     * Gets the response attribute of the LateralCacheUnicaster object
     *
     * @return The response value
     */
    private String getResponse()
        throws IOException
    {
        String result = "";
        try
        {
            ObjectInputStream is = new ObjectInputStream( conn.getInputStream() );
            try
            {
                result = ( String ) is.readObject();
            }
            finally
            {
                is.close();
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "got result = " + result );
            }
        }
        catch ( ClassNotFoundException ce )
        {
            log.error( ce );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        return result;
    }
}
