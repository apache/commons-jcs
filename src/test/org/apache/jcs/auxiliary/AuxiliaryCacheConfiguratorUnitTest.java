package org.apache.jcs.auxiliary;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.engine.control.MockCacheEventLogger;
import org.apache.jcs.engine.control.MockElementSerializer;

/** Unit tests for the auxiliary cache configurator. */
public class AuxiliaryCacheConfiguratorUnitTest
    extends TestCase
{
    /**
     * Verify that we don't get an error.
     */
    public void testParseCacheEventLogger_Null()
    {
        // SETUP
        Properties props = new Properties();

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator.parseCacheEventLogger( props,
                                                                                                               "junk" );

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

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator.parseCacheEventLogger( props,
                                                                                                               null );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }

    /**
     * Verify that we can parse the event logger.
     */
    public void testParseCacheEventLogger_Normal()
    {
        // SETUP
        String auxPrefix = "jcs.auxiliary." + "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockCacheEventLogger.class.getCanonicalName();

        Properties props = new Properties();
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, className );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX
            + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        // DO WORK
        MockCacheEventLogger result = (MockCacheEventLogger) AuxiliaryCacheConfigurator
            .parseCacheEventLogger( props, auxPrefix );

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
        String auxPrefix = "jcs.auxiliary." + "MYAux";
        String testPropertyValue = "This is the value";
        String className = MockElementSerializer.class.getCanonicalName();

        Properties props = new Properties();
        props.put( auxPrefix + AuxiliaryCacheConfigurator.SERIALIZER_PREFIX, className );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.SERIALIZER_PREFIX
            + AuxiliaryCacheConfigurator.ATTRIBUTE_PREFIX + ".testProperty", testPropertyValue );

        // DO WORK
        MockElementSerializer result = (MockElementSerializer) AuxiliaryCacheConfigurator
            .parseElementSerializer( props, auxPrefix );

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

        // DO WORK
        MockElementSerializer result = (MockElementSerializer) AuxiliaryCacheConfigurator
            .parseElementSerializer( props, "junk" );

        // VERIFY
        assertNull( "Should not have a logger.", result );
    }
}
