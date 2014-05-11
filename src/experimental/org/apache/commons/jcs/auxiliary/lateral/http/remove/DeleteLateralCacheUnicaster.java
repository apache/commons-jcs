package org.apache.commons.jcs.auxiliary.lateral.http.remove;

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

import org.apache.commons.jcs.utils.threads.IThreadPoolRunnable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Used to uni-cast a ICacheItem to the named cache on the target server.
 */
public class DeleteLateralCacheUnicaster implements IThreadPoolRunnable
{
    private static final Log log =
        LogFactory.getLog( DeleteLateralCacheUnicaster.class );

    private final String hashtableName;
    private final String key;

    private String urlStr;

    private URLConnection conn;

    /**
     * Constructs with the given ICacheItem and the url of the target server.
     *
     * @param hashtableName
     * @param key
     * @param urlStr
     */
    public DeleteLateralCacheUnicaster( String hashtableName, String key, String urlStr )
    {

        this.hashtableName = hashtableName;
        this.key = key;

        this.urlStr = urlStr;

        log.debug( "In DeleteLateralCacheUnicaster, " + Thread.currentThread().getName() );
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

            if ( !urlStr.startsWith( "http://" ) )
            {
                urlStr = "http://" + urlStr;
            }
            urlStr = urlStr + "?hashtableName=" + hashtableName + "&key=" + key;
            log.debug( "urlStr = " + urlStr );

            callClearCache( urlStr );

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "delete took " + String.valueOf( System.currentTimeMillis() - start ) + " millis\n" );
        }

        return;
    }

    /** Description of the Method */
    public void callClearCache( String ccServletURL )
    {
        URL clear;
        URLConnection clearCon;
        InputStream in;
        OutputStream out;
        try
        {
            clear = new URL( ccServletURL );
            clearCon = clear.openConnection();
            clearCon.setDoOutput( true );
            clearCon.setDoInput( true );
            //clearCon.connect();
            out = clearCon.getOutputStream();
            in = clearCon.getInputStream();
            int cur = in.read();
            while ( cur != -1 )
            {
                cur = in.read();
            }
            out.close();
            in.close();
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        finally
        {
            //in.close();
            out = null;
            in = null;
            clearCon = null;
            clear = null;
            log.info( "called clear cache for " + ccServletURL );
            return;
        }
    }
}
