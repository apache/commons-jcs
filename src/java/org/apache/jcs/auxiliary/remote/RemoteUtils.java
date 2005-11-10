package org.apache.jcs.auxiliary.remote;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.IOException;

import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *  
 */
public class RemoteUtils
{
    private final static Log log = LogFactory.getLog( RemoteUtils.class );

    /** Constructor for the RemoteUtils object */
    private RemoteUtils()
    {
    }

    /**
     * Creates and exports a registry on the specified port of the local host.
     * @param port
     * @return
     * @throws RemoteException
     */
    public static int createRegistry( int port )
        throws RemoteException
    {
        log.debug( "createRegistry> setting security manager" );
        System.setSecurityManager( new RMISecurityManager() );
        if ( port < 1024 )
        {
            port = Registry.REGISTRY_PORT;
        }
        log.debug( "createRegistry> creating registry" );
        LocateRegistry.createRegistry( port );
        return port;
    }

    /** 
     * Loads properties for the named props file.
     * @param propFile
     * @return
     * @throws IOException*/
    public static Properties loadProps( String propFile )
        throws IOException
    {
        InputStream is = RemoteUtils.class.getResourceAsStream( propFile );
        Properties props = new Properties();
        try
        {
            props.load( is );
            if ( log.isDebugEnabled() )
            {
                log.debug( "props.size=" + props.size() );
            }

            if ( log.isDebugEnabled() )
            {
                if ( props != null )
                {
                    Enumeration en = props.keys();
                    StringBuffer buf = new StringBuffer();
                    while ( en.hasMoreElements() )
                    {
                        String key = (String) en.nextElement();
                        buf.append( "\n" + key + " = " + props.getProperty( key ) );
                    }
                    log.debug( buf.toString() );
                }
                else
                {
                    log.debug( "props is null" );
                }
            }

        }
        catch ( Exception ex )
        {

            log.error( "Error loading remote properties, for file name [" + propFile + "]", ex );
            // ignore;
        }
        finally
        {
            if ( is != null )
            {
                is.close();
            }
        }
        return props;
    }
}
