package org.apache.jcs.auxiliary.remote.http.server;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.jcs.engine.control.MockCompositeCacheManager;
import org.apache.jcs.engine.logging.MockCacheEventLogger;

/** Unit tests for the factory */
public class RemoteHttpCacheSeviceFactoryUnitTest
    extends TestCase
{
    /** verify that we get the CacheEventLogger value */
    public void testCreateRemoteHttpCacheService_WithLogger()
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        String className = MockCacheEventLogger.class.getName();

        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        
        boolean allowClusterGet = false;
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".allowClusterGet", String
            .valueOf( allowClusterGet ) );
        
        manager.setConfigurationProperties( props );

        // DO WORK
        RemoteHttpCacheService result = RemoteHttpCacheSeviceFactory
            .createRemoteHttpCacheService( manager );

        // VERIFY
        assertNotNull( "Should have a service.", result );
    }
    
    /** verify that we get the CacheEventLogger value */
    public void testConfigureCacheEventLogger_Present()
    {
        // SETUP
        String testPropertyValue = "This is the value";
        String className = MockCacheEventLogger.class.getName();

        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX
            + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX
            + ".testProperty", testPropertyValue );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) RemoteHttpCacheSeviceFactory
            .configureCacheEventLogger( props );

        // VERIFY
        assertNotNull( "Should have a logger.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /** verify that we get the allowClusterGet value */
    public void testConfigureRemoteCacheServerAttributes_allowClusterGetPresent()
    {
        // SETUP
        boolean allowClusterGet = false;
        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".allowClusterGet", String
            .valueOf( allowClusterGet ) );

        // DO WORK
        RemoteHttpCacheServerAttributes result = RemoteHttpCacheSeviceFactory
            .configureRemoteHttpCacheServerAttributes( props );

        // VERIFY
        assertEquals( "Wrong allowClusterGet", allowClusterGet, result.isAllowClusterGet() );
    }

    /** verify that we get the startRegistry value */
    public void testConfigureRemoteCacheServerAttributes_localClusterConsistencyPresent()
    {
        // SETUP
        boolean localClusterConsistency = false;
        Properties props = new Properties();
        props.put( IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + ".localClusterConsistency",
                   String.valueOf( localClusterConsistency ) );

        // DO WORK
        RemoteHttpCacheServerAttributes result = RemoteHttpCacheSeviceFactory
            .configureRemoteHttpCacheServerAttributes( props );

        // VERIFY
        assertEquals( "Wrong localClusterConsistency", localClusterConsistency, result.isLocalClusterConsistency() );
    }
}
