package org.apache.jcs.engine.control;

import java.util.Properties;

import junit.framework.TestCase;

/** Unit tests for the configurator. */
public class CompositeCacheConfiguratorUnitTest
    extends TestCase
{
    /**
     * Verify that we don't get an error.
     */
    public void testParseCacheEventLogger_Null()
    {
        // SETUP
        Properties props = new Properties();

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( CompositeCacheManager
            .getUnconfiguredInstance() );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) configurator.parseCacheEventLogger( props, "junk" );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }

    /**
     * Verify that we don't get an error.
     */
    public void testParseCacheEventLogger_NullName()
    {
        // SETUP
        Properties props = new Properties();

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( CompositeCacheManager
            .getUnconfiguredInstance() );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) configurator.parseCacheEventLogger( props, null );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }

    /**
     * Verify that we can parse the event logger.
     */
    public void testParseCacheEventLogger_Normal()
    {
        // SETUP
        String auxName = "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockCacheEventLogger.class.getCanonicalName();

        Properties props = new Properties();
        props.put( "jcs.auxiliary." + auxName + CompositeCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        props.put( "jcs.auxiliary." + auxName + CompositeCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX
            + CompositeCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( CompositeCacheManager
            .getUnconfiguredInstance() );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) configurator.parseCacheEventLogger( props, auxName );

        // VERIFY
        assertNotNull( "Should have a logger.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /**
     * Verify that we can parse the ElementSerializer.
     */
    public void testParseElementSerializer_Normal()
    {
        // SETUP
        String auxName = "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockElementSerializer.class.getCanonicalName();

        Properties props = new Properties();
        props.put( "jcs.auxiliary." + auxName + CompositeCacheConfigurator.SERIALIZER_PREFIX, className );
        props.put( "jcs.auxiliary." + auxName + CompositeCacheConfigurator.SERIALIZER_PREFIX
            + CompositeCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( CompositeCacheManager
            .getUnconfiguredInstance() );

        // DO WORK
        MockElementSerializer result = (MockElementSerializer) configurator.parseElementSerializer( props, auxName );

        // VERIFY
        assertNotNull( "Should have a logger.", result );
        assertEquals( "Property should be set.", testPropertyValue, result.getTestProperty() );
    }

    /**
     * Verify that we can parse the ElementSerializer.
     */
    public void testParseElementSerializer_Null()
    {
        // SETUP
        Properties props = new Properties();

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( CompositeCacheManager
            .getUnconfiguredInstance() );

        // DO WORK
        MockElementSerializer result = (MockElementSerializer) configurator.parseElementSerializer( props, "junk" );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }
}
