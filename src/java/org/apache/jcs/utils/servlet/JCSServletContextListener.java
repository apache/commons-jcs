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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * If you add this to the context listeners section of your web.xml file, this will shutdown JCS
 * gracefully.
 * <p>
 * Add the following to the top of your web.xml file.
 *
 * <pre>
 *  &lt;listener&gt;
 *  &lt;listener-class&gt;
 *  org.apache.jcs.utils.servlet.JCSServletContextListener
 *  &lt;/listener-class&gt;
 *  &lt;/listener&gt;
 * </pre>
 *
 * @author Aaron Smuts
 */
public class JCSServletContextListener
    implements ServletContextListener
{
    private static final Log log = LogFactory.getLog( JCSServletContextListener.class );

    /**
     * This does nothing. We don't want to initialize the cache here.
     * <p>
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized( ServletContextEvent arg0 )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "contextInitialized" );
        }
    }

    /**
     * This gets the singleton instance of the CompositeCacheManager and calls shutdown.
     * <p>
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed( ServletContextEvent arg0 )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "contextDestroyed, shutting down JCS." );
        }
        CompositeCacheManager.getInstance().shutDown();
    }

}
