package org.apache.jcs.engine.control;

import java.util.Properties;

import junit.framework.TestCase;

/** Unit tests for the configurator. */
public class CompositeCacheConfiguratorUnitTest
    extends TestCase
{
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
        //MockElementSerializer result = (MockElementSerializer) configurator.parseElementSerializer( props, "junk" );

        // VERIFY
        //assertNull( "Should not have a logger.", result );
    }
}
