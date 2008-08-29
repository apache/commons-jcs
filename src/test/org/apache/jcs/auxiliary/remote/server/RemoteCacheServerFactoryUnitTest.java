package org.apache.jcs.auxiliary.remote.server;

import java.util.Properties;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;

import junit.framework.TestCase;

/** Unit tests for the factory */
public class RemoteCacheServerFactoryUnitTest
    extends TestCase
{
    /** verify that we get the timeout value */
    public void testConfigureRemoteCacheServerAttributes_timeoutPresent()
    {
        // SETUP
        int timeout = 123245;
        Properties props = new Properties();
        props.put( IRemoteCacheConstants.SOCKET_TIMEOUT_MILLIS, String.valueOf( timeout ) );        
                
        // DO WORK
        RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );
        
        // VERIFY
        assertEquals( "Wrong timeout", timeout, result.getRmiSocketFactoryTimeoutMillis() );
    }
    
    /** verify that we get the timeout value */
    public void testConfigureRemoteCacheServerAttributes_timeoutNotPresent()
    {
        // SETUP
        Properties props = new Properties();      
                
        // DO WORK
        RemoteCacheServerAttributes result = RemoteCacheServerFactory.configureRemoteCacheServerAttributes( props );
        
        // VERIFY
        assertEquals( "Wrong timeout", RemoteCacheServerAttributes.DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MS, result.getRmiSocketFactoryTimeoutMillis() );
    }
}
